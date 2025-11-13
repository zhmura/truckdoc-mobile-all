package com.sanda.truckdoc.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sanda.truckdoc.client.HiltEntryPoint;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.service.NotificationHelper;
import com.sanda.truckdoc.client.ui.utils.SoundUtils;

import javax.inject.Inject;

import timber.log.Timber;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_MESSAGE = "com.sanda.truckdoc.client.NOTIFICATION_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_IS_ERROR = "isError";

    @Inject
    NotificationHelper notificationHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Use Hilt entry point pattern for BroadcastReceivers
        HiltEntryPoint entryPoint = TruckDocApp.getEntryPoint(context);
        NotificationHelper notificationHelper = entryPoint.notificationHelper();
        String action = intent.getAction();
        if (NOTIFICATION_MESSAGE.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String message = extras.getString(EXTRA_MESSAGE);
                boolean isError = extras.getBoolean(EXTRA_IS_ERROR, false);
                if (message != null) {
                    onNotificationReceived(notificationHelper, message, isError, context);
                }
            }
        }
    }

    private void onNotificationReceived(NotificationHelper notificationHelper, String message, boolean isError, Context context) {
        Timber.d("Notification received: %s, isError: %b", message, isError);
        notificationHelper.showNotificationMessage(message, context);
        SoundUtils.soundNotification(context, isError);
    }
}
