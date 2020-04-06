package com.sanda.truckdoc.client.to.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PowerManager;
import android.util.Log;

import com.sanda.truckdoc.client.BuildConfig;
import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.api.v3.sync.maintenance.api.AddMaintenanceReportRequest;
import com.sanda.truckdoc.client.data.model.file.DocType;
import com.sanda.truckdoc.client.data.model.file.FileType;
import com.sanda.truckdoc.client.receivers.ServiceResultReceiver;
import com.sanda.truckdoc.client.service.AppSettings;
import com.sanda.truckdoc.client.service.NotificationHelper;
import com.sanda.truckdoc.client.service.ResponseCheckHelper;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.data.db.MaintenanceFileRecord;
import com.sanda.truckdoc.client.to.data.db.MntDbService;
import com.sanda.truckdoc.client.to.utils.LocalStorage;
import com.sanda.truckdoc.client.ui.utils.SoundUtils;
import com.sanda.truckdoc.client.util.ConnectionUtils;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import retrofit2.Response;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by k.natallie on 08.08.2016.
 */
@EIntentService
public class NewMntService extends AbstractIntentService {
    public static final int DELAY = BuildConfig.DEBUG ? 5 * 1000 : 30 * 60 * 1000;
    public static final String PARAM_OUT_MSG = "OUT_TEXT";
    public static final String PARAM_OUT_DATA = "OUT_DATA";
    public static final int MIN_DELAY = 60 * 1000;

    public enum Status {
        SEND_ATTEMTP, SEND_FINISHED, SEND_ERROR, UPLOAD_FILES
    }


    private AuthorizedBackend authorizedBackend;
    @Inject
    MntDbService db;
    @Inject
    Prefs prefs;
    @Inject
    NotificationHelper notificationHelper;

    private LocalStorage storage;


    public NewMntService() {
        super(NewMntService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TruckDocApp.get(this).appComponent().inject(this);
        AppSettings settings = new AppSettings(this);
        UserKey userKey = settings.getUserKey();
        storage = LocalStorage.getInstance(getApplicationContext());
        authorizedBackend = TruckDocApp.get(this)
                .appComponent()
                .plus(new AuthorizedNetworkModule(userKey))
                .authorizedBackend();
    }

    @ServiceAction
    @Trace
    void uploadFile() {
        storage.writeStringPreference(LocalStorage.TO_SEND_PROGRESS, Status.UPLOAD_FILES.toString());
        db.getMntFiles()
                .filter(f -> f.getServerId() == null)
                .flatMap(this::uploadFileInternal)
                .subscribe(this::fileUploadFinished, this::onErrorDelayUploadFile, this::onUploadCompleteCheckQueue);
    }


    public static void addPendingSessionFinish(Context context) {
        L.v();
        Intent serviceIntent = NewMntService_.intent(context).photoSessionFinished().get();
        PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DELAY, pi);
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DELAY, pi);
        }
    }


    private void fileUploadFinished(MaintenanceFileRecord record) {
        notificationHelper.uploadFinished(record.getId(), record.getName(), false);
    }


    private void onErrorDelayUploadFile(Throwable e) {
        Timber.e(e, "File upload delay issue");
        if (!ConnectionUtils.checkIfHaveInternetConnection(this)) {
            notifyActivity(getResources().getString(R.string.mnt_send_error), null, ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
        } else {
            NewMntService_.intent(NewMntService.this).uploadFile().start();

        }
        //else run once more
    }

    //we will call it from receiver?
    @ServiceAction
    void onUploadCompleteCheckQueue() {
        db.getMntFiles().exists(f -> f.getServerId() == null).subscribe(exists -> {
            if (exists) {
                //with delay?
                NewMntService_.intent(this).uploadFile().start();
            }
        });
    }

    @NonNull
    private Observable<? extends MaintenanceFileRecord> uploadFileInternal(MaintenanceFileRecord record) {
        FileType fileType = record.getType() != null ? FileType.valueOf(record.getType()) : FileType.SCENERY;
        DocType docType = record.getMetadata() != null ? DocType.valueOf(record.getMetadata()) : null;
        return authorizedBackend //
                .uploadImage(new ProgressRequestBody(new File(record.getPath()), percentage -> onProgress(record, percentage)),
                        record.getName(),
                        fileType.toString(),
                        //TODO: enum value can not be null??
                        record.getMetadata() != null
                                ? record.getMetadata() : DocType.COM_DESCR.toString(),
                        docType != null ? docType.toString() : "",
                        1, "MAINTENANCE_REPORT"
                ) //
                .doOnError(Throwable::printStackTrace)
                .doOnNext(response -> {
                    if (!ResponseCheckHelper.checkIfError(response, this, "M5", true)) {
                        record.setServerId(response.body());
                        db.updateMessageFileRecord(record);
                        notifyModel(ServiceResultReceiver.ACTION_UPLOAD_MNT_ATTACHMENT_OK, response.body(), record.getNodeName());
                    }
                }).map((Response response) -> record);
    }

    private void onProgress(MaintenanceFileRecord record, int progress) {
        L.v(record.getName() + " " + progress + "%");
        notificationHelper.uploadFile(record.getId(), record.getName(), progress, true);
    }

    /**
     * Вызываем по кнопке или по таймауту.Помечаем
     */
    @ServiceAction
    @Trace
    void photoSessionFinished() {
        db.getMntFiles().filter(r -> !r.isSent()).subscribe(r -> {
            L.v(r);
            r.setReadyForSend(true);
            db.updateMessageFileRecord(r);
        }, L::e, this::startPeriodicUpdate);
    }

    private void startPeriodicUpdate() {
        Timber.i("Mnt start periodic update");
        messageSend();
    }

    @ServiceAction
    @Trace
    void messageSend() {
        Timber.i("Mnt message sending");
        //достаём все незакачанные файлы помеченные на отправку
        db.getMntFiles()
                .filter(r -> !r.isSent())
                .filter(MaintenanceFileRecord::isReadyForSend)
                .toList().flatMap(messages -> {
            //если все файлы для получателя загружены - отправляем
            if (allFilesUploaded(messages)) {
                Log.d("MntService", "all file uploadaed");
                return sendMaintenance(messages);
            } else {
                //иначе откладываем
                return Observable.just(null);
            }
        }).subscribe((List<MaintenanceFileRecord> response) -> {
                    if (response == null) {
                        Runnable delayRunnable = new Runnable() {
                            @Override
                            public void run() {
                                //Ещё добавляем в очередь аплоад
                                NewMntService_.intent(NewMntService.this).uploadFile().start();
                                //и пробуем отправку ещё раз
                                NewMntService_.intent(NewMntService.this).messageSend().start();
                            }
                        };
                        new Handler(getMainLooper()).postDelayed(delayRunnable, MIN_DELAY);
                    } else {
                        Timber.i(response.toString());
                        for (MaintenanceFileRecord record : response) {
                            record.setSent(true);
                            db.updateMessageFileRecord(record);
                        }
                        db.deleteMessageFileRecords(response);
                    }
                }, this::messageSendFailed
        );
    }

    private void messageSendFailed(Throwable e) {
        Timber.e(e, "Mnt messageSend failed");
        SoundUtils.soundNotification(this.getApplicationContext(), true);
        storage.removePreference(LocalStorage.TO_SEND_PROGRESS); //do not retry, critical error
        notifyActivity(getResources().getString(R.string.mnt_send_error), null, ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
    }

    private void messageSendNetworkError(Throwable e) {
        Timber.e(e, "Mnt messageSendNetworkError failed");
        notifyActivity(getResources().getString(R.string.mnt_send_error), null, ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
    }

    private boolean allFilesUploaded(List<MaintenanceFileRecord> messages) {
        return FluentIterable.from(messages).allMatch(record -> record.getServerId() != null);
    }

    @NonNull
    private Observable<List<MaintenanceFileRecord>> sendMaintenance(List<MaintenanceFileRecord> files) {
        storage.writeStringPreference(LocalStorage.TO_SEND_PROGRESS, Status.SEND_ATTEMTP.toString());
        Model model = Model.getInstance(getApplicationContext());
        final AddMaintenanceReportRequest request = new AddMaintenanceReportRequest();
        request.setReport(model.getResult());
        try {
            Response response = authorizedBackend.sendMaintenance(request).executeUnchecked();
            if (ResponseCheckHelper.checkIfError(response, this, "M7", true)) {
                SoundUtils.soundNotification(this.getApplicationContext(), true);
            } else {
                SoundUtils.soundNotification(this.getApplicationContext(), false);
                LocalStorage.getInstance(getApplicationContext()).removePreference(LocalStorage.TO_PROGRESS);
                db.deleteAllMntFileRecords();
                storage.writeStringPreference(LocalStorage.TO_SEND_PROGRESS, Status.SEND_FINISHED.toString());
                Date currentTime = Calendar.getInstance().getTime();
                storage.writeLongPreference(LocalStorage.LAST_MNT_REPORT, currentTime.getTime());
                notifyActivity(getResources().getString(R.string.mnt_send_ok), null, ServiceResultReceiver.ACTION_SENT_MAINTENANCE_OK);
            }
        } catch (Exception ex) {
            Timber.e(ex, "Mnt sending failed");
            notifyActivity(getResources().getString(R.string.mnt_send_error), null, ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
            storage.writeStringPreference(LocalStorage.TO_SEND_PROGRESS, Status.SEND_ERROR.toString());
            Runnable delayRunnable = new Runnable() {
                @Override
                public void run() {
                    NewMntService_.intent(NewMntService.this).messageSend().start();
                }
            };
            new Handler(getMainLooper()).postDelayed(delayRunnable, MIN_DELAY);
        }
        return Observable.just(files);
    }


    private void notifyModel(String action, Long serverId, String title) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        broadcastIntent.putExtra("SERVER_ID", serverId);
        broadcastIntent.putExtra("NODE_NAME", title);
        sendBroadcast(broadcastIntent);
    }

    private void notifyActivity(String message, @Nullable Parcelable result, String action) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null && powerManager.isScreenOn()) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(action);

            broadcastIntent.putExtra(PARAM_OUT_MSG, message);
            broadcastIntent.putExtra(PARAM_OUT_DATA, result);
            sendBroadcast(broadcastIntent);
        }
    }

}