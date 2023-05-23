package com.sanda.truckdoc.client.updater.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.updater.service.UpdaterIntentService;
import com.sanda.truckdoc.client.updater.utils.L;

import timber.log.Timber;

/**
 * Created by astra on 05.06.2015.
 */
public class CheckInstallReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION = "com.sanda.truckdoc.client.updater.receivers.ACTION_CHECK_INSTALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        L.v("Received event");
        UpdaterIntentService.startInstallCheck(context);
    }

    public static void restartCheckInstallReceiver(Context context) {
        stopCheckInstallReceiver(context);
        startCheckInstallReceiver(context);
    }

    public static void startCheckInstallReceiver(Context context) {
        Intent alarmIntent = new Intent(context, CheckInstallReceiver.class);
//        alarmIntent.setAction(INTENT_ACTION);
        boolean isAlarmUp = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;
        if (!isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setInexactRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(),
                    ReceiverConfig.INSTALL_DELAY, pendingIntent);
        }
    }

    public static void stopCheckInstallReceiver(Context context) {
        Timber.i("Stopping CheckInstallReceiver");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, CheckInstallReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
