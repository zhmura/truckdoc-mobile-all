package com.sanda.truckdoc.client.data.model.route

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RoutePath

@Entity(tableName = "route_paths", foreignKeys = [
    ForeignKey(entity = DbRouteAssignment::class, parentColumns = ["id"], childColumns = ["routeAssignmentId"], onDelete = ForeignKey.CASCADE)
])
data class DbRoutePath constructor(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,
        var name: String,
        var serverRouteId: Double,
        //@DatabaseField(canBeNull = false, foreign = true)
        var routeAssignmentId: Long = 0
) {
    constructor(routePath: RoutePath, routeId: Long) : this(name = routePath.name,
            serverRouteId = routeId.toDouble())
}
