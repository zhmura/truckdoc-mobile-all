package com.sanda.truckdoc.client.updater.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.sanda.truckdoc.client.updater.R;
import com.sanda.truckdoc.client.updater.service.UpdaterIntentService;

import javax.inject.Inject;

import androidx.core.app.NotificationCompat;

/**
 * Created by astra on 07.07.2015.
 */
public class NotificationHelper {

    public static final int NOTIFY_ID = 1;
    private final Context context;

    @Inject
    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void notifyAppDownloaded() {
        Notification notification = new NotificationCompat.Builder(context, "appdownloadchannel").setSmallIcon(R.drawable.ic_downloaded)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_new_version_available))
                .setContentIntent(getPendingIntent(context))
                .setChannelId("appdownloadchannel")
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
        soundNotification(context);
    }

    public void hide() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, UpdaterIntentService.class);
        intent.setAction(UpdaterIntentService.ACTION_CHECK_INSTALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }
    }

    public static void soundNotification(Context context) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(context, notification);
        mp.setLooping(false);
        mp.start();
    }
}


