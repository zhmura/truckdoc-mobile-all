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

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import retrofit2.Response;
import io.reactivex.rxjava3.core.Observable;
import timber.log.Timber;
import com.sanda.truckdoc.client.HiltEntryPoint;

import java.util.Properties;

public class NewMntService extends android.app.IntentService {
    public static final int DELAY = BuildConfig.DEBUG ? 5 * 1000 : 30 * 60 * 1000;
    public static final String PARAM_OUT_MSG = "OUT_TEXT";
    public static final String PARAM_OUT_DATA = "OUT_DATA";
    public static final int MIN_DELAY = 60 * 1000;
    public static final String ACTION_UPLOAD_FILE = "com.sanda.truckdoc.client.ACTION_UPLOAD_FILE";
    public static final String ACTION_PHOTO_SESSION_FINISHED = "com.sanda.truckdoc.client.ACTION_PHOTO_SESSION_FINISHED";
    public static final String ACTION_MESSAGE_SEND = "com.sanda.truckdoc.client.ACTION_MESSAGE_SEND";
    public static final String ACTION_UPLOAD_COMPLETE_CHECK_QUEUE = "com.sanda.truckdoc.client.ACTION_UPLOAD_COMPLETE_CHECK_QUEUE";

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
    
    // Add missing field declarations
    private Properties resources;
    private AppSettings settings;
    private UserKey userKey;

    public NewMntService() {
        super("NewMntService");
    }

    public static Intent intent(Context context) {
        return new Intent(context, NewMntService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Use Hilt entry point pattern for Services
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(this);
        // Inject dependencies manually if needed
        resources = loadProperties();
        settings = new AppSettings(this);
        userKey = settings.getUserKey();
        if (userKey != null) {
            createAuthorizedBackend();
        }
        Timber.i("Service was created");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_UPLOAD_FILE.equals(action)) {
                uploadFile();
            } else if (ACTION_PHOTO_SESSION_FINISHED.equals(action)) {
                photoSessionFinished();
            } else if (ACTION_MESSAGE_SEND.equals(action)) {
                messageSend();
            } else if (ACTION_UPLOAD_COMPLETE_CHECK_QUEUE.equals(action)) {
                onUploadCompleteCheckQueue();
            }
        }
    }

    void uploadFile() {
        storage.writeStringPreference(LocalStorage.TO_SEND_PROGRESS, Status.UPLOAD_FILES.toString());
        List<MaintenanceFileRecord> files = db.getMntFilesBlocking();
        List<MaintenanceFileRecord> filesToUpload = files.stream()
                .filter(f -> f.getServerId() == null)
                .collect(java.util.stream.Collectors.toList());
        for (MaintenanceFileRecord file : filesToUpload) {
            uploadFileInternal(file).subscribe(this::fileUploadFinished, this::onErrorDelayUploadFile);
        }
        onUploadCompleteCheckQueue();
    }

    public static void addPendingSessionFinish(Context context) {
        L.v();
        Intent serviceIntent = new Intent(context, NewMntService.class);
        serviceIntent.setAction(ACTION_PHOTO_SESSION_FINISHED);
        PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarm.canScheduleExactAlarms()) {
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
            Intent intent = new Intent(this, NewMntService.class);
            intent.setAction(ACTION_UPLOAD_FILE);
            startService(intent);
        }
    }

    void onUploadCompleteCheckQueue() {
        List<MaintenanceFileRecord> files = db.getMntFilesBlocking();
        boolean exists = files.stream().anyMatch(f -> f.getServerId() == null);
        if (exists) {
            Intent intent = new Intent(this, NewMntService.class);
            intent.setAction(ACTION_UPLOAD_FILE);
            startService(intent);
        }
    }

    @NonNull
    private Observable<? extends MaintenanceFileRecord> uploadFileInternal(MaintenanceFileRecord record) {
        FileType fileType = record.getType() != null ? FileType.valueOf(record.getType()) : FileType.SCENERY;
        DocType docType = record.getMetadata() != null ? DocType.valueOf(record.getMetadata()) : null;
        return authorizedBackend //
                .uploadImage(new ProgressRequestBody(new File(record.getPath()), percentage -> onProgress(record, percentage)),
                        record.getName(),
                        fileType.toString(),
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

    void photoSessionFinished() {
        List<MaintenanceFileRecord> files = db.getMntFilesBlocking();
        List<MaintenanceFileRecord> unsentFiles = files.stream()
                .filter(r -> !r.isSent())
                .collect(java.util.stream.Collectors.toList());
        for (MaintenanceFileRecord r : unsentFiles) {
            L.v(r);
            r.setReadyForSend(true);
            db.updateMessageFileRecord(r);
        }
        startPeriodicUpdate();
    }

    private void startPeriodicUpdate() {
        Timber.i("Mnt start periodic update");
        messageSend();
    }

    void messageSend() {
        Timber.i("Mnt message sending");
        List<MaintenanceFileRecord> allFiles = db.getMntFilesBlocking();
        List<MaintenanceFileRecord> readyFiles = allFiles.stream()
                .filter(r -> !r.isSent())
                .filter(MaintenanceFileRecord::isReadyForSend)
                .collect(java.util.stream.Collectors.toList());
        
        if (allFilesUploaded(readyFiles)) {
            Log.d("MntService", "all file uploaded");
            sendMaintenance(readyFiles).subscribe(
                (List<MaintenanceFileRecord> response) -> {
                    Timber.i(response.toString());
                    for (MaintenanceFileRecord record : response) {
                        record.setSent(true);
                        db.updateMessageFileRecord(record);
                    }
                    db.deleteMessageFileRecords(response);
                },
                this::messageSendFailed
            );
        } else {
            // Schedule retry
            Runnable delayRunnable = new Runnable() {
                @Override
                public void run() {
                    Intent uploadIntent = new Intent(NewMntService.this, NewMntService.class);
                    uploadIntent.setAction(ACTION_UPLOAD_FILE);
                    startService(uploadIntent);
                    
                    Intent sendIntent = new Intent(NewMntService.this, NewMntService.class);
                    sendIntent.setAction(ACTION_MESSAGE_SEND);
                    startService(sendIntent);
                }
            };
            new Handler(getMainLooper()).postDelayed(delayRunnable, MIN_DELAY);
        }
    }

    private void messageSendFailed(Throwable e) {
        Timber.e(e, "Mnt messageSend failed");
        SoundUtils.soundNotification(this.getApplicationContext(), true);
        storage.removePreference(LocalStorage.TO_SEND_PROGRESS);
        notifyActivity(getResources().getString(R.string.mnt_send_error), null, ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
    }

    private void messageSendNetworkError(Throwable e) {
        Timber.e(e, "Mnt messageSendNetworkError failed");
        notifyActivity(getResources().getString(R.string.mnt_send_error), null, ServiceResultReceiver.ACTION_SENT_MAINTENANCE_ERROR);
    }

    private boolean allFilesUploaded(List<MaintenanceFileRecord> messages) {
        return messages.stream().allMatch(f -> f.getServerId() != null);
    }

    @NonNull
    private Observable<List<MaintenanceFileRecord>> sendMaintenance(List<MaintenanceFileRecord> files) {
        AddMaintenanceReportRequest request = new AddMaintenanceReportRequest();
        request.setCurrentClientTime(System.currentTimeMillis());
        // TODO: Create proper ChecklistResult from files
        // request.setReport(checklistResult);
        
        return Observable.fromCallable(() -> {
            try {
                Response<Void> response = authorizedBackend.sendMaintenance(request).execute();
                if (!ResponseCheckHelper.checkIfError(response, this, "M6", true)) {
                    notifyModel(ServiceResultReceiver.ACTION_SENT_MAINTENANCE_OK, null, null);
                }
                return files;
            } catch (Exception e) {
                messageSendNetworkError(e);
                throw e;
            }
        });
    }

    private void notifyModel(String action, Long serverId, String title) {
        Intent intent = new Intent(action);
        intent.putExtra(PARAM_OUT_DATA, serverId);
        intent.putExtra(PARAM_OUT_MSG, title);
        sendBroadcast(intent);
    }

    private void notifyActivity(String message, @Nullable Parcelable result, String action) {
        Intent intent = new Intent(action);
        intent.putExtra(PARAM_OUT_MSG, message);
        if (result != null) {
            intent.putExtra(PARAM_OUT_DATA, result);
        }
        sendBroadcast(intent);
    }

    private void createAuthorizedBackend() {
        // Implementation for creating authorized backend
        Timber.i("Creating authorized backend");
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try {
            props.load(getAssets().open("config.properties"));
        } catch (IOException e) {
            Timber.e(e, "Error loading properties");
        }
        return props;
    }
}