package com.sanda.truckdoc.client.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import com.sanda.truckdoc.client.BuildConfig;
import com.sanda.truckdoc.client.R;
import com.sanda.truckdoc.client.ui.DialogActivity_;

import java.util.concurrent.TimeUnit;

public class IncomeMessagesAlarmManager extends BroadcastReceiver {

    public static final long NOTIFY_INTERVAL = BuildConfig.DEBUG ? TimeUnit.SECONDS.toMillis(5) : TimeUnit.SECONDS.toMillis(15);
    public static final long NOTIFY_INTERVAL_BUSY = TimeUnit.MINUTES.toMillis(30);
    public static final String ACTION = "com.sanda.truckdoc.client.receivers.IncomeMessageAlarmAction";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean receiveAll = intent.getBooleanExtra("receiveAll", true);
        Integer senderRoleId = intent.getIntExtra("senderRoleId", 1);
        boolean skippedRead = intent.getBooleanExtra("skippedRead", true);
        if (skippedRead) {
            DialogActivity_.intent(context)
                    .connectionProblem(false)
                    .reminderMessage("Есть непрочитанные сообщения! Нажмите желтую кнопку для просмотра.")
                    .senderRoleId(senderRoleId)
                    .quickReply(false)
                    .repeatReminder(true)
                    .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .start();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setAlarm(context, receiveAll, IncomeMessagesAlarmManager.NOTIFY_INTERVAL);
        }

        soundNotification(context, receiveAll);
    }

    public static void setAlarm(Context context, boolean receiveAll, long notifyInterval) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, IncomeMessagesAlarmManager.class);
        intent.setAction(ACTION);
        intent.putExtra("receiveAll", receiveAll);
        intent.putExtra("skippedRead", false);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + notifyInterval, pi);
    }

    public static void setAlarmWithDialog(Context context, long notifyInterval) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, IncomeMessagesAlarmManager.class);
        intent.setAction(ACTION);
        intent.putExtra("skippedRead", true);
        intent.putExtra("receiveAll", true);
        PendingIntent pi = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + NOTIFY_INTERVAL_BUSY, pi);
    }

    public static void cancelAlarmWithDialog(Context context) {
        Intent intent = new Intent(context, IncomeMessagesAlarmManager.class);
        intent.setAction(ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, IncomeMessagesAlarmManager.class);
        intent.setAction(ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void soundNotification(Context context, boolean receiveAll) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
        MediaPlayer mediaPlayer = MediaPlayer.create(context, !receiveAll ? R.raw.error_tone : R.raw.new_message_tone);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }
}
