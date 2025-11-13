package com.sanda.truckdoc.client.updater;

import android.content.Context;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import de.devland.esperandro.Esperandro;

@Module
@InstallIn(SingletonComponent.class)
public class UpdaterAppModule {

    @Provides
    @NonNull
    @Singleton
    Context provideContext(@ApplicationContext Context context) {
        return context;
    }

    @Provides
    @NonNull
    @Singleton
    Prefs providePrefs(@ApplicationContext Context context) {
        return Esperandro.getPreferences(Prefs.class, context);
    }
}
