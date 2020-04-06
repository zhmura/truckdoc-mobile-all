package com.sanda.truckdoc.client.data;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by astra on 03.06.2015.
 */
@Module
public class DbModule {

    @Provides
    @Singleton
    @NotNull
    DatabaseHelper provideDatabaseHelper(Context context) {
        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

//    @Provides
//    @Singleton
//    @NotNull
//    RuntimeExceptionDao<ServerMessage, Integer> provideServiceMessageDao(DatabaseHelper d) {
//        return d.getRuntimeExceptionDao(ServerMessage.class);
//    }
//
//    @Provides
//    @Singleton
//    @NotNull
//    RuntimeExceptionDao<AttachmentInfo, Integer> provideAttachmentInfoDao(DatabaseHelper d) {
//        return d.getRuntimeExceptionDao(AttachmentInfo.class);
//    }
}
