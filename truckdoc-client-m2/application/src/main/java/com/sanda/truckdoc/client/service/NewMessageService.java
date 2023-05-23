package com.sanda.truckdoc.client.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.BuildConfig;
import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.model.MessageFileRecord;
import com.sanda.truckdoc.client.data.model.file.ConversionType;
import com.sanda.truckdoc.client.data.model.file.DocType;
import com.sanda.truckdoc.client.data.model.file.FileType;
import com.sanda.truckdoc.client.receivers.NotificationReceiver_;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.ui.utils.SoundUtils;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.timber.L;
import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.api.AuthorizedNetworkModule;
import com.sanda.truckdoc.network.api.ProgressRequestBody;
import com.sanda.truckdoc.network.api.UserKey;

import net.tribe7.common.collect.FluentIterable;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.Trace;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Response;
import rx.Observable;
import timber.log.Timber;

import static com.sanda.truckdoc.client.receivers.ServiceResultReceiver.NOTIFICATION_MESSAGE;

@EIntentService
public class NewMessageService extends AbstractIntentService {
    public static final int RE_CHECK_UPLOAD_ALARM = 101;

    // Timeout to close image creation session.
    public static final long AUTO_FINISH_SESSION_TIMEOUT = BuildConfig.DEBUG ? TimeUnit.SECONDS.toMillis(5) : TimeUnit.MINUTES.toMillis(30);
    public static final long DELAY_OF_FILE_UPLOAD_ON_ERROR = TimeUnit.MINUTES.toMillis(5);

    private AuthorizedBackend authorizedBackend;
    @Inject
    MessagesDatabaseService db;
    @Inject
    Prefs prefs;
    @Inject
    NotificationHelper notificationHelper;

    public NewMessageService() {
        super(NewMessageService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TruckDocApp.get(this).appComponent().inject(this);
        AppSettings settings = new AppSettings(this);
        UserKey userKey = settings.getUserKey();
        if (userKey != null) {
            authorizedBackend = TruckDocApp.get(this)
                    .appComponent()
                    .plus(new AuthorizedNetworkModule(userKey))
                    .authorizedBackend();
        }
    }

    /**
     * Try to upload all files.
     */
    @ServiceAction
    @Trace
    void uploadFiles(boolean triggerMessageSendingOnCompletion) {
        if (!checkAuth()) {
            return;
        }
        //TODO: sort by local photo session id (not null id go first) and local file id
        //group by session id
        //after session group upload finished - >
        db.getNotUploadedMessageFiles()
                .flatMap(this::uploadFileInternal)
                .subscribe(
                        this::onFileUploadFinished,
                        (e) -> onFileUploadError(e, triggerMessageSendingOnCompletion),
                        () -> checkIncompleteFileUploads(triggerMessageSendingOnCompletion)
                );
    }

    private void onFileUploadFinished(MessageFileRecord record) {
        notificationHelper.uploadFinished(record.getId(), record.getName(), false);
    }

    private void onFileUploadError(Throwable e, boolean triggerMessageSendingOnCompletion) {
        Timber.e(e, "File upload failed");
        setUploadReAttemptAlarm(this, triggerMessageSendingOnCompletion);
    }

    /**
     * Check if there are incomplete file uploads. If there are then try to finish them.
     * If no and {@code triggerMessageSendingOnCompletion} is set then try to send message with those files.
     */
    void checkIncompleteFileUploads(boolean triggerMessageSendingOnCompletion) {
        db.getNotUploadedMessageFiles().exists(f -> f.getServerId() == null).subscribe(exists -> {
            if (exists) {
                // Some incomplete files present
                Timber.i("Some not uploaded files remain. Try to upload them.");
                NewMessageService_.intent(this).uploadFiles(triggerMessageSendingOnCompletion).start();
            } else {
                Timber.i("All files were uploaded.");
                if (triggerMessageSendingOnCompletion) {
                    Timber.i("Send message with uploaded files");
                    NewMessageService_.intent(this).messageSend().start();
                } else {
                    Timber.i("No messages is planned");
                }
                //NewMessageService_.intent(this).photoSessionFinished().start();
            }
        });
    }

    private static void setUploadReAttemptAlarm(Context context, boolean triggerMessageSendingOnCompletion) {
        Intent intent = NewMessageService_.intent(context).uploadFiles(triggerMessageSendingOnCompletion).get();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, RE_CHECK_UPLOAD_ALARM, intent, PendingIntent.FLAG_IMMUTABLE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DELAY_OF_FILE_UPLOAD_ON_ERROR, pi);
    }

    @NonNull
    private Observable<? extends MessageFileRecord> uploadFileInternal(MessageFileRecord record) {
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(new File(record.getPath()), percentage -> onProgress(record, percentage));
        FileType fileType = record.getType() != null ? FileType.valueOf(record.getType()) : FileType.SCENERY;
        DocType docType = record.getMetadata() != null ? DocType.valueOf(record.getMetadata()) : null;
        ConversionType conversionType = FileType.valueOf(record.getType()).equals(FileType.DOC) ? ConversionType.BLACK_WHITE_DOC : null;

        return authorizedBackend //
                .uploadImage(progressRequestBody,
                        record.getName(),
                        fileType.toString(),
                        docType != null ? docType.toString() : "",
                        conversionType != null ? conversionType.toString() : "",
                        1, "MESSAGE"
                ) //
                .doOnError(throwable -> {
                    Timber.e(throwable, "Upload failure");
                    notificationHelper.uploadFinished(record.getId(), record.getName(), true);
                    sendNotificationMessageonUI(getApplicationContext().getString(R.string.file_upload_failed, record.getName()), true);
                })
                .doOnNext(response -> {
                    if (!ResponseCheckHelper.checkIfError(response, this, "M5", true)) {
                        notificationHelper.uploadFinished(record.getId(), record.getName(), false);
                        record.setServerId(response.body());
                        db.updateMessageFileRecord(record);
                    }
                }).map((Response<Long> response) -> record);
    }

    private void onProgress(MessageFileRecord record, int progress) {
        L.v((record == null ? "Test upload" : record.getName()) + " " + progress + "%");
        notificationHelper.uploadFile((record == null ? -1 : record.getId()), (record == null ? "test file" : record.getName()), progress, true);
    }

    @ServiceAction
    @Trace
    public void sendLogReport() {
        File logReport = FileHelper.archiveLogFiles();
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(logReport, percentage -> onProgressLogUpload(logReport.getName(), percentage));
        try {
            Response response = authorizedBackend.sendLog(progressRequestBody, logReport.getName(), 1).execute();
            notificationHelper.uploadFinished(-1, logReport.getName(), false);
            if (!ResponseCheckHelper.checkIfError(response, this, "M12", true)) {
                sendNotificationMessageonUI(getResources().getString(R.string.log_sent), false);
            }
        } catch (Exception e) {
            Timber.e(e, "Произошла ошибка при загрузке файла");
            notificationHelper.uploadFinished(-1, logReport.getName(), true);
            sendNotificationMessageonUI("Произошла ошибка при загрузке файла", true);
        } finally {
            try {
                if (logReport != null && logReport.exists()) {
                    logReport.delete();
                }
            } catch (Exception e) {
                L.e(e);
            }
        }
    }

    private void onProgressLogUpload(String logName, int progress) {
        L.v("Log archive upload" + logName + " " + progress + "%");
        notificationHelper.uploadFile(-1, logName, progress, true);
    }

    /**
     * Вызываем по кнопке или по таймауту.Помечаем
     */
    @ServiceAction
    @Trace
    void photoSessionFinished() {
        //TODO: load all files with session id = null group by recipient id and recipient type
        //TODO: sort by local file id asc
        db.getNotSentMessageFiles().subscribe(r -> {
            Timber.v("Message filed record prepared for send", r.getName(), r.getRecipientId(), r.getCreationTime());
            r.setReadyForSend(true);
            db.updateMessageFileRecord(r);
        }, L::e, this::messageSend);
    }

    @ServiceAction
    @Trace
    void messageSend() {
        if (!checkAuth()) {
            return;
        }

        //достаём все незакачанные файлы помеченные на отправку
        db.getNotSentMessageFiles()
                .filter(MessageFileRecord::isReadyForSend)
                // TODO: Wrong! Should: Grouping by session id and session id <> null
                .groupBy(MessageFileRecord::getRecipientId)
                .flatMap(recordsByRecipient -> {
                    //группируем по получателям
                    Long recipientId = recordsByRecipient.getKey();
                    return recordsByRecipient.toList().flatMap(fileRecords -> {
                        assert !fileRecords.isEmpty();
                        //если все файлы для получателя загружены - отправляем
                        if (allFilesUploaded(fileRecords)) {
                            // TODO: This is wrong! We should take recipientIdType from MessageFileRecord
                            String recipientIdType = db.getContactRecords().filter(contact -> contact.getRecipientId().equals(recipientId))
                                    .toBlocking().single().getRecipientIdType();
                            return sendMessageForRecipient(recipientId, recipientIdType, fileRecords);
                        } else {
                            //иначе откладываем
                            // TODO: Implement actual re-attempt of message send
                            return Observable.just(null);
                        }
                    });
                })
                .subscribe((List<MessageFileRecord> response) -> {
                    if (response == null) {
                        Timber.i("Not all messages uploaded");
                        // Для получателя не все файлы залиты
                        // TODO: Actual behavior here should depend on failure cause. See https://goo.gl/tWSV4G
                        //Ещё добавляем в очередь аплоад
                        NewMessageService_.intent(this).uploadFiles(true).start();
                    } else {
                        //сообщение загружено
                        L.d(response.toString());
                        for (MessageFileRecord record : response) {
                            record.setSent(true);
                            db.updateMessageFileRecord(record);
                        }
                        notifyUI("Сообщение успешно отправлено");
                        SoundUtils.soundNotification(this, false);
                        //TODO
                        db.deleteMessageFileRecords(response);
                    }
                }, this::messageSendFailed);
    }

    private void messageSendFailed(Throwable e) {
        Timber.e(e, "Message sending failed");
        sendNotificationMessageonUI("Сбой при отправке сообщения", true);
        SoundUtils.soundNotification(this, true);
    }

    private boolean allFilesUploaded(List<MessageFileRecord> messages) {
        return FluentIterable.from(messages).allMatch(record -> record.getServerId() != null);
    }

    @NonNull
    private Observable<List<MessageFileRecord>> sendMessageForRecipient(Long recipientId,
                                                                        String recipientIdType, List<MessageFileRecord> fileRecords) {
        return Observable.just(fileRecords)
                .doOnNext(records -> Timber.i("Files to send: " + records.size()))
                .filter(it -> !it.isEmpty())
                .flatMap((List<MessageFileRecord> messageFileRecords) -> Observable.from(messageFileRecords)
                        .map(MessageFileRecord::getServerId)
                        .toList()
                        .flatMap((List<Long> serverFileIds) -> authorizedBackend.sendMessage(recipientId,
                                recipientIdType,
                                2,
                                serverFileIds))
                        .doOnNext(response -> {
                            ResponseCheckHelper.checkIfError(response, this, "M10", true);
                        })
                        .map(__ -> messageFileRecords))
                .doOnError(e -> {
                    sendNotificationMessageonUI(NotificationHelper.getErrorMessage(e, this, "M10"), true);
                })
                .onErrorResumeNext(Observable.empty());
    }

    private void sendNotificationMessageonUI(String message, boolean isError) {
        Intent broadcastIntent = new Intent(this, NotificationReceiver_.class);
        broadcastIntent.setAction(NOTIFICATION_MESSAGE);
        broadcastIntent.putExtra(NotificationHelper.PARAM_IS_ERROR, isError);
        broadcastIntent.putExtra(NotificationHelper.PARAM_MSG, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private void notifyUI(String message) {
        Intent intent = new Intent(ServiceResultReceiver.ACTION_PROCESS_FINISHED) //
                .putExtra(MessageCheckService.PARAM_OUT_MSG, message);
        sendBroadcast(intent);
    }

    private boolean checkAuth() {
        boolean ok = authorizedBackend != null;
        if (!ok) {
            L.w("Skipping action because authorizedBackend is null");
        }
        return ok;
    }
}
