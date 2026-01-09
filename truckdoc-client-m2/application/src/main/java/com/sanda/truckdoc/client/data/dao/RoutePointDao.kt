package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.route.DbRoutePoint
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Query("SELECT * FROM route_points")
    fun getAll(): Flow<List<DbRoutePoint>>

    @Query("SELECT * FROM route_points WHERE path_id = :pathId")
    fun getByPathId(pathId: Long): Flow<List<DbRoutePoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: DbRoutePoint): Long

    @Update
    suspend fun update(point: DbRoutePoint): Int

    @Delete
    suspend fun delete(point: DbRoutePoint)

    @Query("DELETE FROM route_points WHERE path_id = :pathId")
    suspend fun deleteByPathId(pathId: Long)

    @Query("DELETE FROM route_points")
    suspend fun deleteAll()
} 