package com.sanda.truckdoc.client.data

import android.content.ContentValues
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanda.truckdoc.client.data.model.DbContactRecord
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by astra on 03.06.2015.
 */
@Module
class DbModule {

    @Provides
    @Singleton
    fun provideDb(context: Context): MessageDatabase = Room.databaseBuilder(context, MessageDatabase::class.java, "messages.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(sqldb: SupportSQLiteDatabase) {

                    val init = listOf(
                            DbContactRecord(1L,
                                    "Колонный",
                                    null,
                                    context.getResources().getColor(R.color.button_default_green),
                                    null),
                            DbContactRecord(2L,
                                    "Экспедитор",
                                    null,
                                    context.getResources().getColor(R.color.button_default_blue),
                                    null),
                            DbContactRecord(3L,
                                    "Оператор",
                                    null,
                                    context.getResources().getColor(R.color.button_default_red),
                                    null))
                    init.forEach {
                        val c = ContentValues()
                        c.put("recipientId", it.id)
                        c.put("label", it.label)
                        c.put("color", it.color)
                        //TODO seems we don't need this
                        // sqldb.insert("contact_records", OnConflictStrategy.IGNORE, c)
                    }
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    onCreate(db)
                }
            })
            .build()

    @Provides
    @Singleton
    fun serverMessageDao(db: MessageDatabase): ServerMessageDao = db.messageDao()

    @Provides
    @Singleton
    fun fileRecordDao(db: MessageDatabase): FileRecordDao = db.fileRecordDao()
}
