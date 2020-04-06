package com.sanda.truckdoc.client.updater;

import android.content.Context;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import de.devland.esperandro.Esperandro;

@Module
public class UpdaterAppModule {

    @NonNull
    private final UpdaterApp app;

    UpdaterAppModule(@NonNull UpdaterApp app) {
        this.app = app;
    }

    @Provides
    @NonNull
    @Singleton
    Context provideContext() {
        return app;
    }

    @Provides
    @NonNull
    @Singleton
    Prefs providePrefs(Context context) {
        return Esperandro.getPreferences(Prefs.class, context);
    }
}
