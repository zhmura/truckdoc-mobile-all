package com.sanda.truckdoc.client.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sanda.checker.Checker;
import com.sanda.truckdoc.client.Prefs;
import com.sanda.truckdoc.client.TruckDocApp;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.SyncReason;
import com.sanda.truckdoc.client.ui.floating.ApnWaiterReceiver;

import java.util.concurrent.TimeUnit;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import app.camera.tdoc.camera_library.PreferenceKeys;
import timber.log.Timber;

import static app.camera.tdoc.camera_library.GalleryActivity.SEND_FILES_ACTION;
import static com.sanda.truckdoc.client.ui.DialogActivity.TAG;

public class ConnectionChangeReceiver extends BroadcastReceiver {

    public static final long MIN_NETWORK_CHANGE_NOTIFICATION_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (haveNetworkConnection(context)) {
            //CustomToast.showToast(context, "Восстановилось соединение. Перезапускаем проверку обновления");
            Prefs prefs = TruckDocApp.get(context).appComponent().prefs();
            long currentTimeMillis = System.currentTimeMillis();
            if (prefs.lastNetworkChangeNotificationTs() + MIN_NETWORK_CHANGE_NOTIFICATION_INTERVAL <= currentTimeMillis) {
                prefs.lastNetworkChangeNotificationTs(currentTimeMillis);
                MessageCheckService.executeGetNewMessagesAction(context, true, false, SyncReason.NETWORK_AVAILABLE);
                // TODO: Investigate if this necessary
                Timber.i("Connection changed intent received. Connection is on");
                try {
                    Checker.cancelCheckConnectionAfterBoot(context);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "onReceive: ", e);
                }
                ApnWaiterReceiver.stopCheckInstallReceiver(context);
                intent = new Intent(context, ConnectionRestoredReceiverForFileUpload.class);
                intent.setAction(ConnectionRestoredReceiverForFileUpload.ACTION_FINISH);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                // TODO: Restrict how often network status change can trigger upload checkIfError

                // Note: that really not needed right now
                intent = new Intent(context, FileActionIntentReceiver.class);
                intent.setAction(SEND_FILES_ACTION);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                sharedPreferences.edit().putInt(PreferenceKeys.getSessionPhotoCountPreferenceKey(), 0).apply();
            }
        }
    }

    private static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                if (ni.isConnected()) {
                    haveConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                if (ni.isConnected()) {
                    haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}