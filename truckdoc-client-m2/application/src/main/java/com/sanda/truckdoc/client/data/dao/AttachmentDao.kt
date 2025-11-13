package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.AttachmentInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachment_info")
    fun getAll(): Flow<List<AttachmentInfo>>

    @Query("SELECT * FROM attachment_info WHERE message_id = :messageId")
    fun getByMessageId(messageId: Int): Flow<List<AttachmentInfo>>

    @Query("SELECT * FROM attachment_info WHERE message_id = :messageId AND downloaded = 0")
    suspend fun getNotDownloadedByMessageId(messageId: Int): List<AttachmentInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentInfo): Long

    @Update
    suspend fun update(attachment: AttachmentInfo): Int

    @Delete
    suspend fun delete(attachment: AttachmentInfo)

    @Query("DELETE FROM attachment_info WHERE message_id = :messageId")
    suspend fun deleteByMessageId(messageId: Int)
} 