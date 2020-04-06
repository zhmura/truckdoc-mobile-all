package com.sanda.truckdoc.client.updater.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sanda.truckdoc.client.updater.UpdaterApp;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (haveNetworkConnection(context)) {
            CheckUpdateReceiver.restartCheckUpdateReceiver(context, false);
        }
    }

    private static boolean haveNetworkConnection(Context context) {
        boolean wifiOnly = UpdaterApp.get(context).appComponent().prefs().useWiFi();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            String typeName = ni.getTypeName();
            if (typeName.equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    return true;
                }
            }

            if (!wifiOnly && typeName.equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }
}