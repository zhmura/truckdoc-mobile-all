package com.sanda.truckdoc.client.ui.floating;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.BuildConfig;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class ApnWaiterReceiver extends BroadcastReceiver {

    private static final long INTERVAL = BuildConfig.DEV ? TimeUnit.SECONDS.toMillis(5) : TimeUnit.MINUTES.toMillis(5);
    private static final String INTENT_ACTION = "com.sanda.truckdoc.client.ui.floating.ApnWaiterReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ApnHelpWindow.startConfirmation(context);
    }

    public static void startApnWaiterReceiver(Context context) {
        Timber.i("Starting ApnWaiterReceiver");
        Intent alarmIntent = new Intent(context, ApnWaiterReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0,
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, pendingIntent);
    }

    public static void stopCheckInstallReceiver(Context context) {
        Timber.i("Stopping CheckInstallReceiver");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, ApnWaiterReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
