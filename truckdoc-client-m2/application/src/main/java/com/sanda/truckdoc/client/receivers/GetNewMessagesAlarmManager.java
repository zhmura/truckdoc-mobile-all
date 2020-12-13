package com.sanda.truckdoc.client.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.SyncReason;
import com.sanda.truckdoc.client.util.PrefUtil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


/**
 * TruckDoc mobile client class
 *
 * @author Siarhei Zhmura
 */
public class GetNewMessagesAlarmManager extends BroadcastReceiver {

    public static String ACTION = "com.sanda.truckdoc.client.receivers.GetNewMessagesAlarm";
    private Prefs prefs;

    public static final int AUTOSYNC_NOTIFICATION_ID = 2;
    public static final int INTENT_GET_NEW_MESSAGE_ALARM = 43;
    public static final String GET_NEW_MESSAGES_ALARM_INTENT_EXTRA_KEY = "get_new_messages";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        prefs = TruckDocApp.get(context).appComponent().prefs();
        if (intent.getBooleanExtra(GET_NEW_MESSAGES_ALARM_INTENT_EXTRA_KEY, false)) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TRUCKDOC_TAG:");
            wl.acquire(); // TODO: Investigate if this lock makes any sense because service execution is asynchronous.
            MessageCheckService.executeGetNewMessagesAction(context, true, false, SyncReason.PERIODIC_CHECK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                long lastSyncAttemptTs = prefs.lastSyncAttemptTs();
                long lastSuccessfulSyncTs = prefs.lastSuccessfulSyncTs();
                final long configuredSyncInterval = PrefUtil.getSyncIntervalMs(prefs);
                final long lastSyncOrAttempt = Math.max(lastSyncAttemptTs, lastSuccessfulSyncTs);
                long nextTriggerTs = lastSyncOrAttempt + configuredSyncInterval;
                GetNewMessagesAlarmManager.setMessageAlarmStartingAtTime(context, nextTriggerTs);
            }
            wl.release();
        }
    }

    /**
     * Resets alarm according to current settings.
     *
     * @param triggerSyncAttempt trigger immediate update
     */
    public static void setGetMessagesAlarm(Context context, boolean triggerSyncAttempt) {
        Prefs prefs = TruckDocApp.get(context).appComponent().prefs();
        long syncIntervalMs = PrefUtil.getSyncIntervalMs(prefs);
        if (triggerSyncAttempt) {
            // Schedule an attempt after full interval
            setMessageAlarmStartingAt(context, syncIntervalMs, System.currentTimeMillis() + syncIntervalMs);
            // And trigger one attempt right now
        } else {
            // Schedule new attempt according to recent activation info
            long nextAlarmActivation = getNextAlarmActivation(prefs, syncIntervalMs);
            setMessageAlarmStartingAt(context, syncIntervalMs, nextAlarmActivation);
        }
    }

    /**
     * @param nextAlarmActivation timestamp when the first activation of alarm is supposed to happen
     */
    public static void setMessageAlarmStartingAtTime(Context context, long nextAlarmActivation) {
        Prefs prefs = TruckDocApp.get(context).appComponent().prefs();
        long syncIntervalMs = PrefUtil.getSyncIntervalMs(prefs);
        setMessageAlarmStartingAt(context, syncIntervalMs, nextAlarmActivation);
    }

    private static void setMessageAlarmStartingAt(Context context, long syncIntervalMs, long nextAlarmActivation) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, GetNewMessagesAlarmManager.class);
        intent.setAction(ACTION);
        intent.putExtra(GET_NEW_MESSAGES_ALARM_INTENT_EXTRA_KEY, true);
        PendingIntent pi = PendingIntent.getBroadcast(context, INTENT_GET_NEW_MESSAGE_ALARM, intent, 0);
        assert am != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmActivation, pi);
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP, nextAlarmActivation, syncIntervalMs, pi);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timeServiceStatusNotifier(context);
        } else {
            timeServiceStatusOldNotifier(context);
        }
    }

    /**
     * @return timestamp of the moment when we should fire alarm next time
     */
    private static long getNextAlarmActivation(Prefs prefs, long syncIntervalMs) {
        long nextActivation = System.currentTimeMillis();

        long lastSyncAttemptTs = prefs.lastSyncAttemptTs();
        if (lastSyncAttemptTs > 0) {
            nextActivation = Math.max(nextActivation, lastSyncAttemptTs + syncIntervalMs);
        }

        long lastSuccessfulSyncTs = prefs.lastSuccessfulSyncTs();
        if (lastSuccessfulSyncTs > 0) {
            nextActivation = Math.max(nextActivation, lastSuccessfulSyncTs + syncIntervalMs);
        }

        return nextActivation;
    }

    public static void cancelGetMessagesAlarm(Context context) {
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyMgr.cancel(AUTOSYNC_NOTIFICATION_ID);
        Intent intent = new Intent(context, GetNewMessagesAlarmManager.class);
        intent.putExtra(GET_NEW_MESSAGES_ALARM_INTENT_EXTRA_KEY, true);
        PendingIntent sender = PendingIntent.getBroadcast(context, INTENT_GET_NEW_MESSAGE_ALARM, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sender.cancel();
        alarmManager.cancel(sender);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected static void timeServiceStatusNotifier(Context context) {
        //Notification message ID
        //Counter
    }


    protected static void timeServiceStatusOldNotifier(Context context) {
        //Notification message ID
        //Counter
    }
}
