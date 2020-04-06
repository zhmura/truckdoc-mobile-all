package com.sanda.truckdoc.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.service.intent.BootCompletedJobIntentService;

/**
 * User: zhmura
 * Date: 23.09.12
 * Time: 16:35
 */
public class StartMyServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        BootCompletedJobIntentService.enqueueWork(context, new Intent());
    }
}
