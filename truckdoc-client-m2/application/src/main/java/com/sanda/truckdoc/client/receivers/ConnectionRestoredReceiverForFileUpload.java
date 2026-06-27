package com.sanda.truckdoc.client.receivers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sanda.checker.Checker;
import com.sanda.checker.ConnectionRestoredReceiver;
import com.sanda.truckdoc.client.service.NewMessageService_;
import com.sanda.truckdoc.client.ui.floating.ApnWaiterReceiver;

import androidx.annotation.NonNull;
import timber.log.Timber;

public class ConnectionRestoredReceiverForFileUpload extends ConnectionRestoredReceiver {

    private static final String TAG = "ConnectionRestoredReceiverForFileUpload";

    public static final String ACTION_FINISH = "com.sanda.truckdoc.client.action.ConnectionRestoredReceiverForFileUpload";

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Timber.i("Connection restored intent received");
        try {
            Checker.cancelCheckConnectionAfterBoot(context);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "onReceive: ", e);
        }
        ApnWaiterReceiver.stopCheckInstallReceiver(context);
        // TODO: Restrict how often network status change can trigger upload checkIfError

        // Note: that really not needed right now
        NewMessageService_.intent(context).uploadFiles(false).start();
    }
}
