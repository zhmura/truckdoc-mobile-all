package com.sanda.truckdoc.client.data.model.route

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.v3.sync.routing.model.NamedPoint

/**
 *
 */
@Entity(tableName = "route_points",
        foreignKeys = [
            ForeignKey(entity = DbRoutePath::class, parentColumns = ["id"], childColumns = ["pathId"], onDelete = ForeignKey.CASCADE)
        ])
data class DbRoutePoint constructor(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var name: String? = null,
        var serverId: String? = null,
        var pathId: Long? = null
) {
    constructor(namedPoint: NamedPoint) : this(latitude = namedPoint.lat,
            longitude = namedPoint.lng,
            name = namedPoint.name,
            serverId = namedPoint.id)
}
