package com.sanda.truckdoc.client.updater;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;

import com.sanda.truckdoc.client.updater.receivers.CheckInstallReceiver;
import com.sanda.truckdoc.client.updater.receivers.CheckUpdateReceiver;
import com.sanda.truckdoc.client.updater.receivers.ConnectionChangeReceiver;
import com.sanda.truckdoc.client.updater.receivers.DownloadCompleteReceiver;
import com.sanda.truckdoc.client.updater.utils.L;

import org.jetbrains.annotations.Nullable;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * Created by astra on 05.06.2015.
 */
public class UpdaterApp extends Application {

    @Nullable
    private volatile UpdaterAppComponent updaterAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter(CheckInstallReceiver.INTENT_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new CheckInstallReceiver(), intentFilter);
        intentFilter = new IntentFilter(CheckUpdateReceiver.INTENT_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new CheckUpdateReceiver(), intentFilter);
        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        LocalBroadcastManager.getInstance(this).registerReceiver(new ConnectionChangeReceiver(), intentFilter);
        intentFilter = new IntentFilter("android.intent.action.DOWNLOAD_COMPLETE");
        LocalBroadcastManager.getInstance(this).registerReceiver(new DownloadCompleteReceiver(), intentFilter);
        if (BuildConfig.DEBUG) {
            L.plant(new L.DebugTree());
        }
    }

    public static UpdaterApp get(Context context) {
        return ((UpdaterApp) context.getApplicationContext());
    }

    @NonNull
    public UpdaterAppComponent appComponent() {
        if (updaterAppComponent == null) {
            synchronized (UpdaterApp.class) {
                if (updaterAppComponent == null) {
                    updaterAppComponent = createAppComponent();
                }
            }
        }

        //noinspection ConstantConditions
        return updaterAppComponent;
    }

    @NonNull
    private UpdaterAppComponent createAppComponent() {
        return DaggerUpdaterAppComponent.builder().updaterAppModule(new UpdaterAppModule(this)).build();
    }
}
