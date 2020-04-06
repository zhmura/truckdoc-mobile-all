package com.sanda.truckdoc.client.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.sanda.truckdoc.client.receivers.ConnectivityReceiver;

import java.util.concurrent.TimeUnit;

public class NetworkSchedulerServiceEvernote //extends Job
        implements
        ConnectivityReceiver.ConnectivityReceiverListener {

    public static final String TAG = "NetworkSchedulerService";
    private static final long MIN_NETWORK_CHANGE_NOTIFICATION_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private ConnectivityReceiver mConnectivityReceiver = new ConnectivityReceiver(this);
/*
    @Override
    protected void onCancel() {
        Log.i(TAG, "onStopJob");
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mConnectivityReceiver);
    }*/

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
      /*  Context context = getContext();
        if (haveNetworkConnection(context)) {
            CustomToast.showToast(getContext(), "Восстановилось соединение. Перезапускаем проверку обновления");
            Prefs prefs = TruckDocApp.get(getContext()).appComponent().prefs();
            long currentTimeMillis = System.currentTimeMillis();
            if (prefs.lastNetworkChangeNotificationTs() + MIN_NETWORK_CHANGE_NOTIFICATION_INTERVAL <= currentTimeMillis) {
                prefs.lastNetworkChangeNotificationTs(currentTimeMillis);
                MessageCheckService.executeGetNewMessagesAction(getContext(), true, false, SyncReason.NETWORK_AVAILABLE);
                // TODO: Investigate if this necessary
                Timber.i("Connection changed intent received. Connection is on");
                try {
                    Checker.cancelCheckConnectionAfterBoot(getContext());
                } catch (ClassNotFoundException e) {
                    Log.e(Crashlytics.TAG, "onStartJob: ", e);
                }
                ApnWaiterReceiver.stopCheckInstallReceiver(getContext());
                Intent intent = new Intent(getContext(), ConnectionRestoredReceiverForFileUpload.class);
                intent.setAction(ConnectionRestoredReceiverForFileUpload.ACTION_FINISH);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                // TODO: Restrict how often network status change can trigger upload checkIfError

                // Note: that really not needed right now
                intent = new Intent(getContext(), FileActionIntentReceiver.class);
                intent.setAction(SEND_FILES_ACTION);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
/*

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mConnectivityReceiver, new IntentFilter(CONNECTIVITY_ACTION));
        onNetworkConnectionChanged(haveNetworkConnection(getContext()));
        return Result.SUCCESS;
    }
*/

/*    public static void schedulePeriodic() {
        new JobRequest.Builder(NetworkSchedulerServiceEvernote.TAG)
                .setExecutionWindow(TimeUnit.MINUTES.toMillis(1), TimeUnit.DAYS.toMillis(1))
                .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                .build()
                .schedule();
    }*/
}