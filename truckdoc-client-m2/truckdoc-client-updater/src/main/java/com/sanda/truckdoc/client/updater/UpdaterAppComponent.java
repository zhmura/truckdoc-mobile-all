package com.sanda.truckdoc.client.updater;

import com.sanda.truckdoc.client.updater.network.NetworkModule;
import com.sanda.truckdoc.client.updater.receivers.DownloadCompleteReceiver;
import com.sanda.truckdoc.client.updater.service.UpdaterIntentService;

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
}
