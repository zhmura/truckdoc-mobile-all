package com.sanda.truckdoc.client.data.model.route

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RouteAssignment
import org.joda.time.DateTime

/**
 * Created by astra on 14.07.2015.
 */
@Entity(
        tableName = "route_assignments",
)
data class DbRouteAssignment(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,

        var serverAssignmentId: Double = 0.0,

        @Deprecated("@DatabaseField(foreign = true, foreignAutoCreate = true, foreignAutoRefresh = true)", level = DeprecationLevel.ERROR)
        var routePathId: Long? = null,

        var dateOfAssignment: DateTime? = null

) {
    constructor(routeAssignment: RouteAssignment) : this(serverAssignmentId = routeAssignment.routeAssignmentId.toDouble(),
            dateOfAssignment = DateTime.now())
}
