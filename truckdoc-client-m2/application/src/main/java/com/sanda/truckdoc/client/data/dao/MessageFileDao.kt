package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.MessageFileRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageFileDao {
    @Query("SELECT * FROM message_files")
    fun getAll(): Flow<List<MessageFileRecord>>

    @Query("SELECT * FROM message_files")
    suspend fun getAllSync(): List<MessageFileRecord>

    @Query("SELECT * FROM message_files WHERE sent = 0")
    fun getNotSent(): Flow<List<MessageFileRecord>>

    @Query("SELECT * FROM message_files WHERE serverId IS NULL")
    fun getNotUploaded(): Flow<List<MessageFileRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: MessageFileRecord): Long

    @Update
    suspend fun update(file: MessageFileRecord): Int

    @Delete
    suspend fun delete(file: MessageFileRecord)

    @Delete
    suspend fun delete(files: List<MessageFileRecord>)

    @Query("DELETE FROM message_files")
    suspend fun deleteAll()
} 