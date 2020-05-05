package com.sanda.truckdoc.client;

import android.content.Context;

import com.sanda.truckdoc.network.AppSettings;
import com.sanda.truckdoc.network.api.UserKey;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.Module;
import dagger.Provides;
import de.devland.esperandro.Esperandro;

@Module
public class AppModule {

    @NonNull
    private final TruckDocApp app;

    AppModule(@NonNull TruckDocApp app) {
        this.app = app;
    }

    @Provides
    @NonNull
    @Singleton
    Context provideContext() {
        return app;
    }

    @Provides
    @Nullable
    UserKey provideUserKey() {
        return new AppSettings(app).getUserKey();
    }

    @Provides
    @Singleton
    @NonNull
    Prefs providePrefs(Context context) {
        return Esperandro.getPreferences(Prefs.class, context);
    }
}
