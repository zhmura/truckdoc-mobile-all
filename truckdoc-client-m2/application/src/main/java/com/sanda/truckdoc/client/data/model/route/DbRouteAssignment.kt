package com.sanda.truckdoc.client.data.model.route

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RouteAssignment
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RoutePath

@Entity(
    tableName = "route_assignments",
    foreignKeys = [
        ForeignKey(
            entity = DbRoutePath::class,
            parentColumns = ["id"],
            childColumns = ["route_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("route_id"), Index("serverAssignmentId")]
)
data class DbRouteAssignment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverAssignmentId: Long = 0,
    val route_id: Long = 0,
    val name: String = "",
    val description: String = ""
) {
    // Constructor for Java compatibility
    constructor(routeAssignment: RouteAssignment, routePath: RoutePath) : this(
        id = 0,
        serverAssignmentId = routeAssignment.routeAssignmentId,
        route_id = routeAssignment.routeId,
        name = routePath.name ?: "",
        description = ""
    )
    
    // Java compatibility method
    fun getRoute(): DbRoutePath? = null // This would need to be implemented with a proper relationship
} 