package com.sanda.truckdoc.client.updater;

import com.sanda.truckdoc.client.updater.network.NetworkModule;
import com.sanda.truckdoc.client.updater.receivers.DownloadCompleteReceiver;
import com.sanda.truckdoc.client.updater.service.UpdaterIntentService;
import com.sanda.truckdoc.client.updater.ui.PrefsActivity;
import com.sanda.truckdoc.client.updater.work.CheckInstallWorker;
import com.sanda.truckdoc.client.updater.work.CheckUpdatesWorker;
import com.sanda.truckdoc.client.updater.work.DownloadWorker;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import dagger.Component;

@Singleton
@Component(
        modules = {UpdaterAppModule.class, NetworkModule.class})
public interface UpdaterAppComponent {

    void inject(@NonNull UpdaterIntentService service);

    Prefs prefs();

    void inject(@NonNull DownloadCompleteReceiver downloadCompleteReceiver);

    void inject(@NotNull CheckUpdatesWorker checkUpdatesWorker);

    void inject(@NotNull DownloadWorker downloadWorker);

    void inject(PrefsActivity prefsActivity);

    void inject(@NotNull CheckInstallWorker checkInstallWorker);
}
