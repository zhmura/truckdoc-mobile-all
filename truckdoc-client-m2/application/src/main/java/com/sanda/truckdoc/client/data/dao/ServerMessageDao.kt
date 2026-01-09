package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.ServerMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerMessageDao {
    @Query("SELECT * FROM server_message")
    fun getAll(): Flow<List<ServerMessage>>

    @Query("SELECT * FROM server_message")
    suspend fun getAllSync(): List<ServerMessage>

    @Query("SELECT * FROM server_message WHERE hidden = 0")
    fun getNotHidden(): Flow<List<ServerMessage>>

    @Query("SELECT * FROM server_message WHERE sent = 0 AND outgoing = 1")
    fun getPendingOutMessages(): Flow<List<ServerMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ServerMessage): Long

    @Update
    suspend fun update(message: ServerMessage): Int

    @Delete
    suspend fun delete(message: ServerMessage)

    @Query("SELECT * FROM server_message WHERE id = :id")
    suspend fun getById(id: Int): ServerMessage?

    @Query("SELECT * FROM server_message WHERE serverMessageId = :serverMessageId")
    suspend fun getByServerId(serverMessageId: Int): List<ServerMessage>
} 