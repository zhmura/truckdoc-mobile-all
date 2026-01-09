package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.route.DbRoutePath
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePathDao {
    @Query("SELECT * FROM route_pathes")
    fun getAll(): Flow<List<DbRoutePath>>

    @Query("SELECT * FROM route_pathes WHERE id = :id")
    fun findById(id: Long): Flow<DbRoutePath?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(path: DbRoutePath): Long

    @Update
    suspend fun update(path: DbRoutePath): Int

    @Delete
    suspend fun delete(path: DbRoutePath)

    @Query("DELETE FROM route_pathes")
    suspend fun deleteAll()
} 