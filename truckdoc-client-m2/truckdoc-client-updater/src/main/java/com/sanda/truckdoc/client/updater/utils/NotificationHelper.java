package com.sanda.truckdoc.client.updater.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.sanda.truckdoc.client.updater.R;
import com.sanda.truckdoc.client.updater.work.WorkManagerService;

import javax.inject.Inject;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Created by astra on 07.07.2015.
 */
public class NotificationHelper {

    public static final int     NOTIFY_ID = 1;
    private final       Context context;

    @Inject
    public NotificationHelper(Context context) {
        this.context = context;
    }

    public void notifyAppDownloaded(String apk) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        Notification notification = new NotificationCompat.Builder(context, "appdownloadchannel").setSmallIcon(R.drawable.ic_downloaded)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_new_version_available))
                .setContentIntent(getPendingIntent(context, apk))
                .setChannelId("appdownloadchannel")
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
        soundNotification(context);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("appdownloadchannel",
                "appdownloadchannel",
                NotificationManager.IMPORTANCE_LOW);
        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }

    public void hide() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);
    }

    public void toast(String s) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, s, Toast.LENGTH_SHORT).show());
    }

    private PendingIntent getPendingIntent(Context context, String apk) {
        Intent intent = new Intent(context, WorkManagerService.class).putExtra("apk", apk);
//        intent.setAction(UpdaterIntentService.ACTION_CHECK_INSTALL);
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, 0, intent, 0);
        } else {*/
        return PendingIntent.getService(context, 0, intent, 0);
        //      }
    }

    public static void soundNotification(Context context) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(context, notification);
        mp.setLooping(false);
        mp.start();
    }
}


