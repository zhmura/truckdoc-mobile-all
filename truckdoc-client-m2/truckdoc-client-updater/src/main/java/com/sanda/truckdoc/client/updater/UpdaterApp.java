package com.sanda.truckdoc.client.updater;

import android.app.Application;
import android.content.Context;

import com.sanda.truckdoc.client.updater.utils.L;

import org.jetbrains.annotations.Nullable;

import androidx.annotation.NonNull;

/**
 * Created by astra on 05.06.2015.
 */
public class UpdaterApp extends Application {

    @Nullable private volatile UpdaterAppComponent updaterAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
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
        return DaggerUpdaterAppComponent.factory().create(this);
    }
}
