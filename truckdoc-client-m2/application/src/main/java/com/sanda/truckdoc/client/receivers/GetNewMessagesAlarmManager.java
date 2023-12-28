package com.sanda.truckdoc.client.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.SyncReason;
import com.sanda.truckdoc.client.util.PrefUtil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static com.sanda.truckdoc.client.receivers.IncomeMessagesAlarmManager.NOTIFY_INTERVAL_BUSY;


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
        PendingIntent pi = PendingIntent.getBroadcast(context, INTENT_GET_NEW_MESSAGE_ALARM, intent, PendingIntent.FLAG_IMMUTABLE);
        assert am != null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmActivation, pi);
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + NOTIFY_INTERVAL_BUSY, pi);
        }

        timeServiceStatusNotifier(context);
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
        PendingIntent sender = PendingIntent.getBroadcast(context, INTENT_GET_NEW_MESSAGE_ALARM, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sender.cancel();
        alarmManager.cancel(sender);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected static void timeServiceStatusNotifier(Context context) {
        //Notification message ID
        //Counter
        int count = 0;
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String id = "getnewmess_channel_01";
        CharSequence name = "getnewmess.channel";
        String description = "Description";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;


        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        // mChannel.enableLights(true);
        //mChannel.setLightColor(Color.RED);
        //mChannel.enableVibration(true);
        //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notifyMgr.createNotificationChannel(mChannel);
        String CHANNEL_ID = id;
        //Create NotificationManager  object
        //Instantiate notification with icon and ticker message

        //PendingIntent to launch our activity if the user selects it
        PendingIntent i = PendingIntent.getService(context, 0, new Intent(context, MessageCheckService.class), PendingIntent.FLAG_IMMUTABLE);
        //Set the info that show in the notification panel
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        builder.setContentTitle(context.getResources().getString(R.string.autoloader));
        builder.setContentText(context.getResources().getString(R.string.autoloader_service_on));
        builder.setContentIntent(i);
        builder.setSmallIcon(R.drawable.clock_history);
        builder.setChannelId(CHANNEL_ID);
        builder.setTicker(context.getResources().getString(R.string.autoloader));
        builder.setNumber(++count);
        Notification notifyObj = builder.build();
        //Value indicates the current number of events represented by the notification
        notifyObj.number = ++count;
        //Set default notification sound
        notifyObj.defaults |= Notification.DEFAULT_LIGHTS;
        //Clear the status notification when the user selects it
        notifyObj.flags |= Notification.FLAG_NO_CLEAR;
        notifyObj.flags |= Notification.FLAG_ONGOING_EVENT;
        //Send notification
        notifyMgr.notify(AUTOSYNC_NOTIFICATION_ID, notifyObj);
    }


    protected static void timeServiceStatusOldNotifier(Context context) {
        //Notification message ID
        //Counter
        int count = 0;
        //Create NotificationManager  object
        NotificationManager notifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Instantiate notification with icon and ticker message

        //PendingIntent to launch our activity if the user selects it
        PendingIntent i = PendingIntent.getService(context, 0, new Intent(context, MessageCheckService.class), PendingIntent.FLAG_IMMUTABLE);
        //Set the info that show in the notification panel
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle(context.getResources().getString(R.string.autoloader));
        builder.setContentText(context.getResources().getString(R.string.autoloader_service_on));
        builder.setContentIntent(i);
        builder.setSmallIcon(R.drawable.clock_history);
        builder.setTicker(context.getResources().getString(R.string.autoloader));
        builder.setNumber(++count);
        Notification notifyObj = builder.build();
        //Value indicates the current number of events represented by the notification
        notifyObj.number = ++count;
        //Set default notification sound
        notifyObj.defaults |= Notification.DEFAULT_LIGHTS;
        //Clear the status notification when the user selects it
        notifyObj.flags |= Notification.FLAG_NO_CLEAR;
        notifyObj.flags |= Notification.FLAG_ONGOING_EVENT;
        //Send notification
        notifyMgr.notify(AUTOSYNC_NOTIFICATION_ID, notifyObj);
    }
}
