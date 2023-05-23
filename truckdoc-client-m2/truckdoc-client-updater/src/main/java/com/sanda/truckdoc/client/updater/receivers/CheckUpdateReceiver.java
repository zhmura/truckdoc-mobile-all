package com.sanda.truckdoc.client.updater.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.sanda.truckdoc.client.updater.Prefs;
import com.sanda.truckdoc.client.updater.UpdaterApp;
import com.sanda.truckdoc.client.updater.service.UpdaterIntentService;
import com.sanda.truckdoc.client.updater.utils.L;

import timber.log.Timber;


public class CheckUpdateReceiver extends BroadcastReceiver {

    public static final String INTENT_ACTION = "com.sanda.truckdoc.client.updater.receivers.ACTION_CHECK_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        L.v("Received event");
        if (!TextUtils.isEmpty(intent.getAction())) {
            if (isUpdateTimeGood(context)) {
                Toast.makeText(context, "Проверяем обновления апк", Toast.LENGTH_SHORT).show();
                UpdaterIntentService.startUpdateCheck(context);
            } else {
                Toast.makeText(context, "Еще не пришло время проверять обновления", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean isUpdateTimeGood(Context context) {
        Prefs prefs = UpdaterApp.get(context).appComponent().prefs();
        boolean hasFreshUpdate = prefs.contains(Prefs.LAST_SUCCESSFUL_CHECK_DATE) &&
                (System.currentTimeMillis() - prefs.lastSuccessfulCheck() < ReceiverConfig.MIN_INTERVAL_BETWEEN_UPDATE_CHECK);
        boolean tooEarlyUpdateCheck = prefs.contains(Prefs.LAST_CHECK_DATE) &&
                (System.currentTimeMillis() - prefs.lastCheck() < ReceiverConfig.MIN_INTERVAL_AFTER_UPDATE_CHECK_FAILURE);
        return !hasFreshUpdate && !tooEarlyUpdateCheck;
    }

    public static void restartCheckUpdateReceiver(Context context, boolean delayedStart) {
        stopCheckUpdateReceiver(context);
        startCheckUpdateReceiver(context, delayedStart);
    }

    public static void startCheckUpdateReceiver(Context context, boolean delayedStart) {
        Timber.i("Restarting CheckUpdateReceiver");
        Intent alarmIntent = new Intent(context, CheckUpdateReceiver.class);
        boolean isAlarmUp = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;
        if (!isAlarmUp) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmIntent.setAction(INTENT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setInexactRepeating(AlarmManager.RTC,
                    delayedStart
                            ? System.currentTimeMillis() + ReceiverConfig.UPDATE_DELAY
                            : System.currentTimeMillis(),
                    ReceiverConfig.UPDATE_DELAY, pendingIntent);
        }
    }

    public static void stopCheckUpdateReceiver(Context context) {
        Timber.i("Stopping CheckUpdateReceiver");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, CheckUpdateReceiver.class);
        alarmIntent.setAction(INTENT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
