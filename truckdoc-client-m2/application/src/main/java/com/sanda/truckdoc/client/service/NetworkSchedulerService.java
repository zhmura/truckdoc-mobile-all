package com.sanda.truckdoc.client.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sanda.truckdoc.client.receivers.ConnectivityReceiver;

import java.util.concurrent.TimeUnit;

public class NetworkSchedulerService //extends JobService
        implements
        ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = NetworkSchedulerService.class.getSimpleName();

    private ConnectivityReceiver mConnectivityReceiver;

/*    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        mConnectivityReceiver = new ConnectivityReceiver(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob" + mConnectivityReceiver);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob");
        unregisterReceiver(mConnectivityReceiver);
        return true;
    }*/

    public static final long MIN_NETWORK_CHANGE_NOTIFICATION_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
       /* Context context = this;
        if (haveNetworkConnection(context)) {
            CustomToast.showToast(getApplicationContext(), "Восстановилось соединение. Перезапускаем проверку обновления");
            Prefs prefs = TruckDocApp.get(getApplicationContext()).appComponent().prefs();
            long currentTimeMillis = System.currentTimeMillis();
            if (prefs.lastNetworkChangeNotificationTs() + MIN_NETWORK_CHANGE_NOTIFICATION_INTERVAL <= currentTimeMillis) {
                prefs.lastNetworkChangeNotificationTs(currentTimeMillis);
                MessageCheckService.executeGetNewMessagesAction(getApplicationContext(), true, false, SyncReason.NETWORK_AVAILABLE);
                // TODO: Investigate if this necessary
                Timber.i("Connection changed intent received. Connection is on");
                try {
                    Checker.cancelCheckConnectionAfterBoot(getApplicationContext());
                } catch (ClassNotFoundException e) {
                    Log.e(Crashlytics.TAG, "onStartJob: ", e);
                }
                ApnWaiterReceiver.stopCheckInstallReceiver(getApplicationContext());
                Intent intent = new Intent(getApplicationContext(), ConnectionRestoredReceiverForFileUpload.class);
                intent.setAction(ConnectionRestoredReceiverForFileUpload.ACTION_FINISH);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                // TODO: Restrict how often network status change can trigger upload checkIfError

                // Note: that really not needed right now
                intent = new Intent(getApplicationContext(), FileActionIntentReceiver.class);
                intent.setAction(SEND_FILES_ACTION);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPreferences.edit().putInt(PreferenceKeys.getSessionPhotoCountPreferenceKey(), 0).apply();
            }
        }*/
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