package com.sanda.truckdoc.client.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sanda.truckdoc.client.BuildConfig;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.HiltEntryPoint;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.data.MessagesDatabaseServiceJavaCompat;
import com.sanda.truckdoc.client.data.model.MessageFileRecord;
import com.sanda.truckdoc.client.data.model.file.FileType;
import com.sanda.truckdoc.client.service.CustomToast;
import com.sanda.truckdoc.client.service.NewMessageService;
import com.sanda.truckdoc.client.to.data.Model;
import com.sanda.truckdoc.client.to.data.db.MaintenanceFileRecord;
import com.sanda.truckdoc.client.to.data.db.MntDbService;
import com.sanda.truckdoc.client.to.service.NewMntService;
import com.sanda.truckdoc.client.util.timber.L;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import app.camera.tdoc.camera_library.PreferenceKeys;

/**
 * TruckDoc mobile client class
 *
 * @author Siarhei Zhmura
 */
public class FileActionIntentReceiver extends BroadcastReceiver {

    public static final long SESSION_FINISH_TIMEOUT = BuildConfig.DEBUG ? TimeUnit.SECONDS.toMillis(5) : TimeUnit.SECONDS.toMillis(5);

    private MessagesDatabaseService db;
    private MntDbService mntDb;

    protected void checkDBInit(Context context) {
        try {
            HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(context);
            db = entryPoint.messagesDatabaseService();
            // Note: mntdb() is not available in the entry point, we'll need to add it or use a different approach
            // mntDb = entryPoint.mntdb();
        } catch (Exception e) {
            L.e("Failed to initialize database", e);
        }
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        switch (Objects.requireNonNull(intent.getAction())) {
            case "com.sanda.truckdoc.client.receivers.FileSaveReceiverIntent": {
                checkDBInit(context);
                Bundle bundle = intent.getExtras();
                String fileName = bundle.getString(PreferenceKeys.getFileNameKey());
                Boolean isForDoc = bundle.getBoolean(PreferenceKeys.getImageTypeKey());
                Long recipientId = bundle.getLong(PreferenceKeys.getRecipientKey());
                MessageFileRecord r = new MessageFileRecord();
                r.setPath(fileName);
                assert fileName != null;
                r.setName(new File(fileName).getName());
                r.setRecipientId(recipientId);
                r.setType(isForDoc ? FileType.DOC : FileType.SCENERY);
                MessagesDatabaseServiceJavaCompat.createMessageFileRecordBlocking(db, r);
                startNewMessageService(context, false);
                break;
            }
            case "com.sanda.truckdoc.client.receivers.MntFileSaveReceiverIntent": {
                checkDBInit(context);
                Bundle bundle = intent.getExtras();
                String fileName = bundle.getString(PreferenceKeys.getFileNameKey());
                Model model = Model.getInstance(context);
                model.updateAttachment(model.getCurrentNode(), 0L);
                MaintenanceFileRecord r = new MaintenanceFileRecord();
                r.setPath(fileName);
                r.setReadyForSend(true);
                r.setNodeName(Model.getInstance(context).getCurrentNode().getTitleText());
                mntDb.createMessageFileRecord(r);
                startNewMntService(context);
                break;
            }
            case "com.sanda.truckdoc.client.receivers.FileSendReceiverIntent":
                checkDBInit(context);
                addPendingSessionFinish(context);
                break;
            case "com.sanda.truckdoc.client.receivers.FilesDeleteReceiverIntent":
                checkDBInit(context);
                MessagesDatabaseServiceJavaCompat.deleteAllMessageFileRecordsBlocking(db);
                CustomToast.showToast(context, context.getResources().getText(R.string.all_files_deleted).toString());
                break;
        }
    }

    public static void addPendingSessionFinish(Context context) {
        L.v();
        Intent serviceIntent = new Intent(context, NewMessageService.class);
        serviceIntent.setAction(NewMessageService.ACTION_PHOTO_SESSION_FINISHED);
        PendingIntent pi = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + SESSION_FINISH_TIMEOUT, pi);
    }

    private void startNewMessageService(Context context, boolean uploadFiles) {
        Intent intent = new Intent(context, NewMessageService.class);
        intent.setAction(NewMessageService.ACTION_UPLOAD_FILES);
        intent.putExtra("uploadFiles", uploadFiles);
        context.startService(intent);
    }

    private void startNewMntService(Context context) {
        Intent intent = new Intent(context, NewMntService.class);
        intent.setAction(NewMntService.ACTION_UPLOAD_FILE);
        context.startService(intent);
    }
}
