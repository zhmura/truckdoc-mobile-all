package com.sanda.truckdoc.client.updater;

import android.content.Context;

import com.sanda.truckdoc.client.updater.network.NetworkModule;
import com.sanda.truckdoc.client.updater.ui.PrefsActivity;
import com.sanda.truckdoc.client.updater.work.CheckInstallWorker;
import com.sanda.truckdoc.client.updater.work.CheckUpdatesWorker;
import com.sanda.truckdoc.client.updater.work.DownloadWorker;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = { UpdaterAppModule.class, NetworkModule.class })
public interface UpdaterAppComponent {

    Prefs prefs();

    void inject(@NotNull CheckUpdatesWorker checkUpdatesWorker);

    void inject(@NotNull DownloadWorker downloadWorker);

    void inject(PrefsActivity prefsActivity);

    void inject(@NotNull CheckInstallWorker checkInstallWorker);

    @Component.Factory
    interface Factory {
        UpdaterAppComponent create(@BindsInstance Context context);
    }
}
