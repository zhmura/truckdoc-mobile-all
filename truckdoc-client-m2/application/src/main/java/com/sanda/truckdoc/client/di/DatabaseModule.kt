package com.sanda.truckdoc.client.di

import android.content.Context
import androidx.room.Room
import com.sanda.truckdoc.client.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "td_messages.db"
        )
        .addMigrations(*AppDatabase.MIGRATIONS)
        .build()
    }

    @Provides
    @Singleton
    fun provideServerMessageDao(database: AppDatabase) = database.serverMessageDao()

    @Provides
    @Singleton
    fun provideAttachmentDao(database: AppDatabase) = database.attachmentDao()

    @Provides
    @Singleton
    fun provideLocationDao(database: AppDatabase) = database.locationDao()

    @Provides
    @Singleton
    fun provideContactRecordDao(database: AppDatabase) = database.contactRecordDao()

    @Provides
    @Singleton
    fun provideMessageFileDao(database: AppDatabase) = database.messageFileDao()

    @Provides
    @Singleton
    fun provideRouteAssignmentDao(database: AppDatabase) = database.routeAssignmentDao()

    @Provides
    @Singleton
    fun provideRoutePointDao(database: AppDatabase) = database.routePointDao()

    @Provides
    @Singleton
    fun provideRoutePathDao(database: AppDatabase) = database.routePathDao()

    @Provides
    @Singleton
    fun provideMaintenanceFileDao(database: AppDatabase) = database.maintenanceFileDao()
} 