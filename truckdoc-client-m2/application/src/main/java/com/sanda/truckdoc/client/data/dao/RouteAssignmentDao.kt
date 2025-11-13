package com.sanda.truckdoc.client.data.dao

import androidx.room.*
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteAssignmentDao {
    @Query("SELECT * FROM route_assignments")
    fun getAll(): Flow<List<DbRouteAssignment>>

    @Query("SELECT * FROM route_assignments")
    suspend fun getAllSync(): List<DbRouteAssignment>

    @Query("SELECT * FROM route_assignments WHERE serverAssignmentId = :serverId")
    fun findByServerId(serverId: Long): Flow<List<DbRouteAssignment>>

    @Query("SELECT * FROM route_assignments WHERE id = :id")
    fun findById(id: Long): Flow<DbRouteAssignment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: DbRouteAssignment): Long

    @Update
    suspend fun update(assignment: DbRouteAssignment): Int

    @Delete
    suspend fun delete(assignment: DbRouteAssignment)

    @Query("DELETE FROM route_assignments")
    suspend fun deleteAll()
} 