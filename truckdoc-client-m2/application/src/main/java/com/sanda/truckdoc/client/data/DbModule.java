package com.sanda.truckdoc.client.data;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Created by astra on 03.06.2015.
 */
@Module
@InstallIn(SingletonComponent.class)
public class DbModule {

    @Provides
    @Singleton
    @NotNull
    DatabaseHelper provideDatabaseHelper(@ApplicationContext Context context) {
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
