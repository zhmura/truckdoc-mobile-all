package com.sanda.truckdoc.client;

import android.content.Context;
import android.content.ContextWrapper;

import com.sanda.truckdoc.client.service.AppSettings;
import com.sanda.truckdoc.network.api.UserKey;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import de.devland.esperandro.Esperandro;
import com.sanda.truckdoc.client.util.StrictModeUtils;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @NonNull
    @Singleton
    Context provideContext(@ApplicationContext Context context) {
        return context;
    }

    @Provides
    UserKey provideUserKey(@ApplicationContext Context context) {
        return new AppSettings(new ContextWrapper(context)).getUserKey();
    }

    @Provides
    @Singleton
    @NonNull
    Prefs providePrefs(@ApplicationContext Context context) {
        // Esperandro/SharedPreferences can touch disk on first access; avoid StrictMode spam in debug.
        return StrictModeUtils.allowDiskReads(() -> Esperandro.getPreferences(Prefs.class, context));
    }
}
