package com.sanda.truckdoc.client.data.model.route

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_pathes")
data class DbRoutePath(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val description: String = ""
) {
    // Java compatibility method
    fun getPoints(): List<DbRoutePoint> = emptyList() // This would need to be implemented with a proper relationship
} 