package com.sanda.checker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class ConnectionRestoredReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.sanda.checker.ConnectionRestoredReceiver.ACTION";

    public static void start(final @NonNull Context context) {
        Intent intent;
        try {
            intent = new Intent(context,
                    Class.forName("com.sanda.truckdoc.client.receivers.ConnectionRestoredReceiverForFileUpload"));
            intent.setAction(ACTION);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
