package com.sanda.truckdoc.client;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.sanda.checker.Checker;
import com.sanda.checker.ConnectionRestoredReceiver;
import com.sanda.checker.NoConnectionReceiver;
import com.sanda.truckdoc.client.receivers.CheckConnectionAfterBootReceiver;
import com.sanda.truckdoc.client.receivers.CheckerConnectionReceiver;
import com.sanda.truckdoc.client.receivers.ConnectionChangeReceiver;
import com.sanda.truckdoc.client.receivers.ConnectionRestoredReceiverForFileUpload;
import com.sanda.truckdoc.client.receivers.FileActionIntentReceiver;
import com.sanda.truckdoc.client.receivers.GetNewMessagesAlarmManager;
import com.sanda.truckdoc.client.receivers.IncomeMessagesAlarmManager;
import com.sanda.truckdoc.client.receivers.LocationReceiver;
import com.sanda.truckdoc.client.receivers.LocationReceiver_;
import com.sanda.truckdoc.client.receivers.NotificationReceiver_;
import com.sanda.truckdoc.client.util.EmulatorDetector;
import com.sanda.truckdoc.client.util.UpdateClientInfoHelper;
import com.sanda.truckdoc.client.util.timber.FileLoggingTree;

import net.danlew.android.joda.JodaTimeAndroid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
Major refactoring doneimport androidx.multidex.MultiDexApplication;

import timber.log.Timber;

import static com.sanda.truckdoc.client.receivers.LocationReceiver.ACTION_LOCATION_CHANGED;
import static com.sanda.truckdoc.client.receivers.ServiceResultReceiver.NOTIFICATION_MESSAGE;

/**
 * User: Sergey Zhmura
 * Date: 19.02.14
 * Time: 20:43
 */
public class TruckDocApp extends Application {

    private Activity currentActivity;

    public Activity getActivityContext() {
        return currentActivity;
    }

    @Nullable
    private volatile AppComponent appComponent;

    private static final String TAG = TruckDocApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        long start = System.currentTimeMillis();
        if (BuildConfig.DEBUG) {
            new TimberInitTask().execute();
        }
        registerReceivers();
        GetNewMessagesAlarmManager.setGetMessagesAlarm(getApplicationContext(), false);
        appComponent().prefs().gpsDataSyncTime(LocationReceiver.DEFAULT_GPS_CHECK_INTERVAL);
        new UpdateClientInfoHelper().setTargetPackageForUpdate(new ContextWrapper(getApplicationContext()));
        LocationReceiver.requestLocationUpdates(this, appComponent().prefs().gpsDataSyncTime());
        appComponent().prefs().timerStatus(true);
        JodaTimeAndroid.init(this);
        long end = System.currentTimeMillis() - start;
        permitDiskReads();
        EmulatorDetector.logcat();

        /* create dispatcher */
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                TruckDocApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                TruckDocApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityResumed(Activity activity) {
                TruckDocApp.this.currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                TruckDocApp.this.currentActivity = null;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                // don't clear current activity because activity may get stopped after
                // the new activity is resumed
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                // don't clear current activity because activity may get destroyed after
                // the new activity is resumed
            }
        });
    }

    private void registerReceivers() {
        registerFileIntentReceiver();
        registerCheckConnectionAfterBootReceiver();
        registerCheckerConnectionReceiver();
        registerLocationReceiver();
        registerNotificationReceiver();
        registerGetMessagesReceiver();
        registerIncomeMessagesReceiver();
        registerAlarmReceivers();
        registerConnectionChangerReceiver();
        registerFileUploadReceiver();
    }

    private void registerAlarmReceivers() {
        IntentFilter intentFilter = new IntentFilter(IncomeMessagesAlarmManager.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new IncomeMessagesAlarmManager(), intentFilter);
        intentFilter = new IntentFilter(GetNewMessagesAlarmManager.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new GetNewMessagesAlarmManager(), intentFilter);
    }

    private void registerConnectionChangerReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionChangeReceiver(), intentFilter);
        intentFilter = new IntentFilter(ConnectionRestoredReceiverForFileUpload.ACTION_FINISH);
        LocalBroadcastManager.getInstance(this).registerReceiver(new ConnectionRestoredReceiverForFileUpload(), intentFilter);

    }

    private void registerFileUploadReceiver() {
        IntentFilter intentFilter = new IntentFilter(ConnectionRestoredReceiver.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new ConnectionRestoredReceiverForFileUpload(), intentFilter);
    }

    private void registerFileIntentReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.sanda.truckdoc.client.receivers.FileSaveReceiverIntent");
        intentFilter.addAction("com.sanda.truckdoc.client.receivers.MntFileSaveReceiverIntent");
        intentFilter.addAction("com.sanda.truckdoc.client.receivers.FileSendReceiverIntent");
        intentFilter.addAction("com.sanda.truckdoc.client.receivers.FilesDeleteReceiverIntent");
        LocalBroadcastManager.getInstance(this).registerReceiver(new FileActionIntentReceiver(), intentFilter);
    }

    private void registerCheckConnectionAfterBootReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Checker.AFTER_BOOT_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new CheckConnectionAfterBootReceiver(),
                intentFilter);
    }

    private void registerCheckerConnectionReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NoConnectionReceiver.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new CheckerConnectionReceiver(),
                intentFilter);
    }

    private void registerLocationReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_LOCATION_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new LocationReceiver_(),
                intentFilter);
    }

    private void registerNotificationReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFICATION_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(new NotificationReceiver_(),
                intentFilter);
    }

    private void registerGetMessagesReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetNewMessagesAlarmManager.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new GetNewMessagesAlarmManager(),
                intentFilter);
    }

    private void registerIncomeMessagesReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(IncomeMessagesAlarmManager.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new IncomeMessagesAlarmManager(),
                intentFilter);
    }


    private void turnOnStrictMode() {
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .penaltyDeath().build());
    }

    private void permitDiskReads() {
        StrictMode.ThreadPolicy oldThreadPolicy = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(
                new StrictMode.ThreadPolicy.Builder(oldThreadPolicy).permitDiskWrites()
                        .permitDiskReads().build());
        StrictMode.setThreadPolicy(oldThreadPolicy);
    }

    private static class TimberInitTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            Timber.plant(new FileLoggingTree());
            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, result);
        }
    }

    @Override
    public void onTerminate() {
        GetNewMessagesAlarmManager.cancelGetMessagesAlarm(getApplicationContext());
        super.onTerminate();
    }

    @NonNull
    public static TruckDocApp get(@NotNull Context context) {
        return ((TruckDocApp) context.getApplicationContext());
    }

    @NonNull
    public AppComponent appComponent() {
        if (appComponent == null) {
            synchronized (TruckDocApp.class) {
                if (appComponent == null) {
                    appComponent = createAppComponent();
                }
            }
        }

        //noinspection ConstantConditions
        return appComponent;
    }

    @NonNull
    private AppComponent createAppComponent() {
        return DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }
}
