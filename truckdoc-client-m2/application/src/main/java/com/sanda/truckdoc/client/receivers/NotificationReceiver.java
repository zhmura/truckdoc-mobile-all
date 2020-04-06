package com.sanda.truckdoc.client.receivers;

import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.util.NotificationHelper;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.api.support.content.AbstractBroadcastReceiver;

/**
 * Created by Asus on 4/17/2017.
 */
@EReceiver
public class NotificationReceiver extends AbstractBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ServiceResultReceiver.NOTIFICATION_MESSAGE.equals(intent.getAction())) {
            String message = intent.getStringExtra(NotificationHelper.PARAM_MSG);
            Boolean isError = intent.getBooleanExtra(NotificationHelper.PARAM_IS_ERROR, false);
            if (isError) {
                NotificationHelper.showErrorMessage(message, context);
            } else {
                NotificationHelper.showNotificationMessage(message, context);
            }
        }
    }
}
