package com.sanda.truckdoc.client.updater.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sanda.truckdoc.client.updater.Prefs;
import com.sanda.truckdoc.client.updater.UpdaterApp;

public class BootAndUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        CheckUpdateReceiver.restartCheckUpdateReceiver(context, true);
        Prefs prefs = UpdaterApp.get(context).appComponent().prefs();
        if (prefs.contains(Prefs.APK_PATH) && prefs.contains(Prefs.VERSION_CODE)) {
            CheckInstallReceiver.restartCheckInstallReceiver(context);
        }
    }
}
