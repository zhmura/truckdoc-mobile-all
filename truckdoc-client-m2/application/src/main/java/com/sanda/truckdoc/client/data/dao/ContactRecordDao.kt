package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.DbContactRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactRecordDao {
    @Query("SELECT * FROM contact_records")
    fun getAll(): Flow<List<DbContactRecord>>

    @Query("SELECT * FROM contact_records")
    suspend fun getAllSync(): List<DbContactRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: DbContactRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<DbContactRecord>)

    @Update
    suspend fun update(contact: DbContactRecord): Int

    @Delete
    suspend fun delete(contact: DbContactRecord)

    @Query("DELETE FROM contact_records")
    suspend fun deleteAll()
} 