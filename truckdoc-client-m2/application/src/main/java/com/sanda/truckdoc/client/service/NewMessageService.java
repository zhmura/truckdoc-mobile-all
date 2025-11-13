package com.sanda.truckdoc.client.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.BuildConfig;
import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.HiltEntryPoint;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.data.model.MessageFileRecord;
import com.sanda.truckdoc.client.data.model.file.ConversionType;
import com.sanda.truckdoc.client.data.model.file.DocType;
import com.sanda.truckdoc.client.data.model.file.FileType;
import com.sanda.truckdoc.client.data.model.DbContactRecord;
import com.sanda.truckdoc.client.receivers.NotificationReceiver;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.ui.utils.SoundUtils;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.timber.L;
import com.sanda.truckdoc.network.AuthorizedBackend;
import com.sanda.truckdoc.network.api.AuthorizedNetworkModule;
import com.sanda.truckdoc.network.api.ProgressRequestBody;
import com.sanda.truckdoc.network.api.UserKey;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Optional;
import android.util.Log;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Response;
import io.reactivex.rxjava3.core.Observable;
import timber.log.Timber;

import static com.sanda.truckdoc.client.receivers.ServiceResultReceiver.NOTIFICATION_MESSAGE;

public class NewMessageService extends android.app.IntentService {
    private static final String TAG = "NewMessageService";
    public static final int RE_CHECK_UPLOAD_ALARM = 101;
    public static final String ACTION_UPLOAD_FILES = "com.sanda.truckdoc.client.ACTION_UPLOAD_FILES";
    public static final String ACTION_SEND_LOG_REPORT = "com.sanda.truckdoc.client.ACTION_SEND_LOG_REPORT";
    public static final String ACTION_PHOTO_SESSION_FINISHED = "com.sanda.truckdoc.client.ACTION_PHOTO_SESSION_FINISHED";
    public static final String ACTION_MESSAGE_SEND = "com.sanda.truckdoc.client.ACTION_MESSAGE_SEND";
    public static final String EXTRA_TRIGGER_MESSAGE_SENDING = "trigger_message_sending";

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
        // Note: For Services, we can't use @Inject directly
        // We need to use the entry point pattern
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(this);
        // Inject dependencies manually if needed
        AppSettings settings = new AppSettings(this);
        UserKey userKey = settings.getUserKey();
        if (userKey != null) {
            // Note: This needs to be updated to use the entry point pattern
            // For now, we'll comment it out as it requires a different approach
            // authorizedBackend = entryPoint.authorizedBackend();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_UPLOAD_FILES.equals(action)) {
                boolean triggerMessageSending = intent.getBooleanExtra(EXTRA_TRIGGER_MESSAGE_SENDING, false);
                uploadFiles(triggerMessageSending);
            } else if (ACTION_SEND_LOG_REPORT.equals(action)) {
                sendLogReport();
            } else if (ACTION_PHOTO_SESSION_FINISHED.equals(action)) {
                photoSessionFinished();
            } else if (ACTION_MESSAGE_SEND.equals(action)) {
                messageSend();
            }
        }
    }

    void uploadFiles(boolean triggerMessageSendingOnCompletion) {
        if (!checkAuth()) {
            return;
        }
        try {
            List<MessageFileRecord> files = MessagesDatabaseServiceJavaCompat.getNotUploadedMessageFilesBlocking(db);
            if (!files.isEmpty()) {
                for (MessageFileRecord file : files) {
                    uploadFileInternal(file);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error uploading files", e);
        }
    }

    private void uploadFileInternal(MessageFileRecord file) {
        // Implementation for uploading individual file
        // This would contain the actual upload logic
        Log.d(TAG, "Uploading file: " + file.getName());
    }

    private void checkForNotUploadedFiles() {
        try {
            List<MessageFileRecord> files = MessagesDatabaseServiceJavaCompat.getNotUploadedMessageFilesBlocking(db);
            boolean exists = files.stream().anyMatch(f -> f.getServerId() == null);
            if (exists) {
                // Handle not uploaded files
                Log.d(TAG, "Found files that need to be uploaded");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for not uploaded files", e);
        }
    }

    private void markFileAsReadyForSend(MessageFileRecord file) {
        try {
            MessageFileRecord updatedFile = file.setSent(false);
            MessagesDatabaseServiceJavaCompat.updateMessageFileBlocking(db, updatedFile);
        } catch (Exception e) {
            Log.e(TAG, "Error marking file as ready for send", e);
        }
    }

    private void sendMessageToRecipient(Long recipientId, String recipientIdType, List<MessageFileRecord> fileRecords) {
        try {
            List<DbContactRecord> contacts = MessagesDatabaseServiceJavaCompat.getContactRecordsBlocking(db);
            Optional<DbContactRecord> contact = contacts.stream()
                    .filter(c -> c.getRecipientId() == recipientId)
                    .findFirst();
            
            if (contact.isPresent()) {
                // Send message logic here
                Log.d(TAG, "Sending message to contact: " + contact.get().getLabel());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            sendNotificationMessageonUI("Message send error", true);
        }
    }

    private void sendMessage(Long recipientId, String recipientIdType, List<MessageFileRecord> fileRecords) {
        try {
            // Convert file records to the format expected by the API
            List<Long> fileIds = fileRecords.stream()
                    .map(MessageFileRecord::getId)
                    .collect(Collectors.toList());
            
            // Call the appropriate sendMessage method based on the API signature
            authorizedBackend.sendMessage(recipientId, recipientIdType, 0, fileIds);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    private static void setUploadReAttemptAlarm(Context context, boolean triggerMessageSendingOnCompletion) {
        Intent intent = new Intent(context, NewMessageService.class);
        intent.setAction(ACTION_UPLOAD_FILES);
        intent.putExtra(EXTRA_TRIGGER_MESSAGE_SENDING, triggerMessageSendingOnCompletion);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getService(context, RE_CHECK_UPLOAD_ALARM, intent, PendingIntent.FLAG_IMMUTABLE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DELAY_OF_FILE_UPLOAD_ON_ERROR, pi);
    }

    private void onProgress(MessageFileRecord record, int progress) {
        L.v((record == null ? "Test upload" : record.getName()) + " " + progress + "%");
        notificationHelper.uploadFile((record == null ? -1 : record.getId()), (record == null ? "test file" : record.getName()), progress, true);
    }

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

    void photoSessionFinished() {
        List<MessageFileRecord> records = MessagesDatabaseServiceJavaCompat.getNotSentMessageFilesBlocking(db);
        for (MessageFileRecord r : records) {
            Timber.v("Message filed record prepared for send", r.getName(), r.getRecipientId(), r.getCreationTime());
            r.setReadyForSend(true);
            MessagesDatabaseServiceJavaCompat.updateMessageFileRecordBlocking(db, r);
        }
        messageSend();
    }

    void messageSend() {
        if (!checkAuth()) {
            return;
        }
        List<MessageFileRecord> allRecords = MessagesDatabaseServiceJavaCompat.getNotSentMessageFilesBlocking(db);
        // Filter for ready to send
        List<MessageFileRecord> readyRecords = allRecords.stream()
                .filter(MessageFileRecord::isReadyForSend)
                .collect(Collectors.toList());
        // Group by recipientId
        Map<Long, List<MessageFileRecord>> grouped = readyRecords.stream()
                .collect(Collectors.groupingBy(MessageFileRecord::getRecipientId));
        for (Map.Entry<Long, List<MessageFileRecord>> entry : grouped.entrySet()) {
            Long recipientId = entry.getKey();
            List<MessageFileRecord> fileRecords = entry.getValue();
            if (!fileRecords.isEmpty() && allFilesUploaded(fileRecords)) {
                // Find recipientIdType
                List<DbContactRecord> contacts = MessagesDatabaseServiceJavaCompat.getContactRecordsBlocking(db);
                String recipientIdType = contacts.stream()
                        .filter(contact -> contact.getRecipientId() == recipientId)
                        .findFirst()
                        .map(DbContactRecord::getRecipientIdType)
                        .orElse(null);
                // Send message for recipient
                sendMessageForRecipient(recipientId, recipientIdType, fileRecords);
            }
        }
        onMessageSendComplete();
    }

    private void onMessageSendSuccess(List<MessageFileRecord> records) {
        if (records != null) {
            for (MessageFileRecord record : records) {
                record.setSent(true);
                MessagesDatabaseServiceJavaCompat.updateMessageFileRecordBlocking(db, record);
            }
            MessagesDatabaseServiceJavaCompat.deleteMessageFileRecordsBlocking(db, records);
        }
    }

    private void onMessageSendComplete() {
        Timber.i("Message send complete");
    }

    private void messageSendFailed(Throwable e) {
        Timber.e(e, "Message send failed");
        SoundUtils.soundNotification(this.getApplicationContext(), true);
        sendNotificationMessageonUI(getResources().getString(R.string.message_send_error), true);
    }

    private boolean allFilesUploaded(List<MessageFileRecord> messages) {
        return messages.stream().allMatch(f -> f.getServerId() != null);
    }

    @NonNull
    private Observable<List<MessageFileRecord>> sendMessageForRecipient(Long recipientId,
                                                                      String recipientIdType, List<MessageFileRecord> fileRecords) {
        // Convert file records to the format expected by the API
        List<Long> fileIds = fileRecords.stream()
                .map(MessageFileRecord::getId)
                .collect(Collectors.toList());
        
        return authorizedBackend
                .sendMessage(recipientId, recipientIdType, 0, fileIds)
                .doOnError(this::messageSendFailed)
                .doOnNext(response -> {
                    if (!ResponseCheckHelper.checkIfError(response, this, "M6", true)) {
                        notifyUI(getResources().getString(R.string.message_sent));
                    }
                })
                .map(response -> fileRecords);
    }

    private void sendNotificationMessageonUI(String message, boolean isError) {
        Intent intent = new Intent(NOTIFICATION_MESSAGE);
        intent.putExtra("message", message);
        intent.putExtra("isError", isError);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notifyUI(String message) {
        Intent intent = new Intent(ServiceResultReceiver.ACTION_SENT_MESSAGE_OK);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private boolean checkAuth() {
        return authorizedBackend != null;
    }
}
