package com.sanda.truckdoc.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sanda.checker.Checker;
import com.sanda.truckdoc.client.util.SmsHelper;
import com.sanda.truckdoc.client.util.timber.L;

public class CheckConnectionAfterBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean wasConnectionLastTime = Checker.wasConnectionLastTime(context);
        L.v("Was connection? = " + wasConnectionLastTime);
        if (!wasConnectionLastTime) {
            SmsHelper.sendSmsToOperatorNoConnection(context);
        }
    }
}
