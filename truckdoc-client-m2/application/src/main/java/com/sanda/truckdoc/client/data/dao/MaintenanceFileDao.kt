package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.MaintenanceFileRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceFileDao {
    @Query("SELECT * FROM maintenance_files")
    fun getAll(): Flow<List<MaintenanceFileRecord>>

    @Query("SELECT * FROM maintenance_files")
    suspend fun getAllSync(): List<MaintenanceFileRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: MaintenanceFileRecord): Long

    @Update
    suspend fun update(file: MaintenanceFileRecord): Int

    @Delete
    suspend fun delete(file: MaintenanceFileRecord)

    @Delete
    suspend fun delete(files: List<MaintenanceFileRecord>)

    @Query("DELETE FROM maintenance_files")
    suspend fun deleteAll()
} 