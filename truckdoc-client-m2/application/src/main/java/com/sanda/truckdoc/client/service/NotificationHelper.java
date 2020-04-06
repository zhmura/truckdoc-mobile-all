package com.sanda.truckdoc.client.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sanda.truckdoc.client.ui.utils.SoundUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.MessageFormat;

import javax.inject.Inject;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import app.camera.tdoc.camera_library.CustomToast;

/**
 * Created by astra on 20.05.2015.
 */
public class NotificationHelper {

    public static final String PARAM_MSG = "com.sanda.truckdoc.client.intent.action.PARAM_MSG";
    public static final String PARAM_IS_ERROR = "com.sanda.truckdoc.client.intent.action.PARAM_IS_ERROR";

    private Context context;
    private NotificationManager notificationManager;
    private String title;

    @Inject
    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void updateProgress(int percent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify(1,
                    createDefaultBuilder(false).setProgress(100, percent, false)
                            .setContentTitle(title)
                            .setContentText(percent + " %" + "/ 100 %")
                            .build());
        } else {
            notificationManager.notify(1,
                    createOldDefaultBuilder().setProgress(100, percent, false)
                            .setContentTitle(title)
                            .setContentText(percent + " %" + "/ 100 %")
                            .build());
        }
    }

    @NotNull
    private Notification.Builder createOldDefaultBuilder() {
        return new Notification.Builder(context).setSmallIcon(app.camera.tdoc.camera_library.R.drawable.upload)
                .setContentTitle(context.getResources().getString(app.camera.tdoc.camera_library.R.string.loading))
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationCompat.Builder createDefaultBuilder(boolean needSound) {
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "notification_channel_01";
        CharSequence name = "notification.channel";
        String description = "truckdoc notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;


        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.setImportance(needSound ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_MIN);
        // mChannel.enableLights(true);
        //mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(false);
        //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notifyMgr.createNotificationChannel(mChannel);
        return new NotificationCompat.Builder(context, id)
                .setSmallIcon(app.camera.tdoc.camera_library.R.drawable.upload)
                .setContentTitle(context.getResources().getString(app.camera.tdoc.camera_library.R.string.loading))
                .setContentIntent(getPendingIntent())
                .setAutoCancel(true);
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getActivity(context, 0, new Intent(), 0);
    }

    public void uploadFile(long id, String filename, int progress, boolean autoCancel) {
        Notification n = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            n = createDefaultBuilder(false).setProgress(100, progress, false)
                    .setContentText(filename)
                    .setOngoing(true)
                    .setAutoCancel(autoCancel)
                    .build();
        } else {
            n = createOldDefaultBuilder().setProgress(100, progress, false)
                    .setContentText(filename)
                    .setOngoing(true)
                    .setAutoCancel(autoCancel)
                    .build();
        }
        notificationManager.notify((int) id, n);
    }

    public void uploadFinished(long id, String filename, boolean error) {
        notificationManager.cancel((int) id);
        Notification n = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            n = createDefaultBuilder(true).setContentText(error ? context.getString(app.camera.tdoc.camera_library.R.string.file_upload_failed, filename) : context.getString(app.camera.tdoc.camera_library.R.string.file_upload_finished, filename)).build();
        } else {
            n = createOldDefaultBuilder().setContentText(error ? context.getString(app.camera.tdoc.camera_library.R.string.file_upload_failed, filename) : context.getString(app.camera.tdoc.camera_library.R.string.file_upload_finished, filename)).build();
        }
        notificationManager.notify((int) id, n);
        SoundUtils.soundNotification(context, error);
    }

    public static void showErrorMessage(Context context, int stringMessageResourceId, String methodCode) {
        CustomToast.showRed(context, MessageFormat.format(context.getResources().getString(stringMessageResourceId), methodCode));
    }

    public static String getErrorMessage(Throwable e, Context context, String methodCode) {
        if (e instanceof IOException) {
            return MessageFormat.format(context.getResources().getString(app.camera.tdoc.camera_library.R.string.commonIOException), methodCode);
        } else {
            return MessageFormat.format(context.getResources().getString(app.camera.tdoc.camera_library.R.string.commonException), methodCode);
        }
    }

    public static void showErrorMessage(String errorMessage, Context context) {
        CustomToast.showRed(context, errorMessage);
    }

    public static void showNotificationMessage(String message, Context context) {
        CustomToast.showBlue(context, message);
    }
}
