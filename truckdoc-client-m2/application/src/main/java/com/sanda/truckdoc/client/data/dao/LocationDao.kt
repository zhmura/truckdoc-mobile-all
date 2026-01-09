package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.DbLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY time DESC LIMIT 100")
    fun getAll(): Flow<List<DbLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: DbLocation): Long

    @Update
    suspend fun update(location: DbLocation): Int

    @Delete
    suspend fun delete(location: DbLocation)

    @Delete
    suspend fun delete(locations: List<DbLocation>)

    @Query("DELETE FROM locations")
    suspend fun deleteAll()
} 