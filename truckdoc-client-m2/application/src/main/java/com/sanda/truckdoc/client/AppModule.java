package com.sanda.truckdoc.client;

import android.app.Activity;
import android.content.Context;

import com.sanda.truckdoc.client.data.Deleter;
import com.sanda.truckdoc.client.data.MessagesDatabaseService;
import com.sanda.truckdoc.client.util.FileHelper;
import com.sanda.truckdoc.client.util.MessagesMenu;
import com.sanda.truckdoc.client.util.ShowAttachments;
import com.sanda.truckdoc.client.util.commons.FileUtils;
import com.sanda.truckdoc.client.util.timber.L;
import com.sanda.truckdoc.network.AppSettings;
import com.sanda.truckdoc.network.api.UserKey;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.instructions.InstructionsPrefs;
import app.messages2.MessageDependenciesProvider;
import app.messages2.OnMessageCallbacks;
import app.messages2.OnMessagesMenu;
import dagger.Module;
import dagger.Provides;
import de.devland.esperandro.Esperandro;

@Module
public class AppModule {

    @NonNull private final TruckDocApp app;

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

    @Provides
    @Singleton
    @NonNull
    InstructionsPrefs provideInstructionsPrefs(Prefs p) {
        //esperandro can't load interface from another module
        return new InstructionsPrefs() {
            @Override
            public void lastKnownInstructionsVersion(long value) {
                p.get().edit().putLong("lastKnownInstructionsVersion", value).commit();
            }

            @Override
            public long lastKnownInstructionsVersion() {
                return p.get().getLong("lastKnownInstructionsVersion", -1);
            }
        };
    }

    @Provides
    @Singleton
    @NotNull Deleter provideDeleter(Context c) {
        return new Deleter() {

            @Override
            public void deleteAll() {
                FileHelper.deleteAllAppFiles();
            }

            @Override
            public void deleteDirectoryForMessage(int id) {
                try {
                    FileUtils.deleteDirectory(FileHelper.getIncomeDirectory(id, "*"));
                } catch (IOException e) {
                    L.e(e);
                }
            }
        };
    }

    @Provides
    @NonNull
    MessageDependenciesProvider onMessagesMenu(MessagesDatabaseService databaseService) {
        return new MessageDependenciesProvider() {
            @NotNull
            @Override
            public OnMessagesMenu provideOnMessageMenu(@NotNull Activity activity) {
                return new MessagesMenu(activity, databaseService);
            }

            @NotNull
            @Override
            public OnMessageCallbacks provideOnMessageClicked(@NotNull Activity activity) {
                return new ShowAttachments(activity);
            }
        };
    }
}
