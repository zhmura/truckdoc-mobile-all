package com.sanda.truckdoc.client;

import com.sanda.truckdoc.client.data.DbModule;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.service.MessageCheckService;
import com.sanda.truckdoc.client.service.NewMessageService;
import com.sanda.truckdoc.client.to.data.db.MntDbService;
import com.sanda.truckdoc.client.to.service.NewMntService;
import com.sanda.truckdoc.client.ui.DashboardActivity;
import com.sanda.truckdoc.client.ui.SplashActivity;
import com.sanda.truckdoc.client.ui.TruckdocPreferenceActivity;
import com.sanda.truckdoc.client.ui.message.InboxFragment;
import com.sanda.truckdoc.client.ui.message.NewMessageFragment;
import com.sanda.truckdoc.network.api.AuthorizedNetworkComponent;
import com.sanda.truckdoc.network.api.AuthorizedNetworkModule;
import com.sanda.truckdoc.network.api.NetworkModule;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import app.instructions.InstructionsActivity;
import app.instructions.InstructionsInjector;
import app.instructions.InstructionsModule;
import app.messages2.Messages2Injector;
import dagger.Component;

@Singleton
@Component(
        modules = { AppModule.class, DbModule.class, NetworkModule.class, InstructionsModule.class, AuthorizedNetworkModule.class })
public interface AppComponent extends InstructionsInjector, Messages2Injector {

    AuthorizedNetworkComponent.Builder auth();

    void inject(@NonNull MessageCheckService service);

    void inject(@NonNull InboxFragment service);

    void inject(@NonNull DashboardActivity dashboardActivity);

    void inject(@NonNull SplashActivity splashActivity);

    void inject(@NonNull NewMntService service);

    void inject(@NonNull InstructionsActivity service);

    MessagesDatabaseService db();

    MntDbService mntdb();

    Prefs prefs();

    void inject(NewMessageFragment newMessageFragment);

    void inject(TruckdocPreferenceActivity truckdocPreferenceActivity);

    void inject(NewMessageService newMessageService);
}
