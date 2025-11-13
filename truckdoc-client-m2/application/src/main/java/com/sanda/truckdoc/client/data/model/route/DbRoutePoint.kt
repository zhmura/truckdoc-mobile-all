package com.sanda.truckdoc.client.data.model.route

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sanda.truckdoc.client.data.Converters
import org.joda.time.DateTime

@Entity(
    tableName = "route_points",
    foreignKeys = [
        ForeignKey(
            entity = DbRoutePath::class,
            parentColumns = ["id"],
            childColumns = ["path_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("path_id"), Index("status")]
)
@TypeConverters(Converters::class)
data class DbRoutePoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path_id: Long = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val name: String = "",
    val description: String = "",
    val order: Int = 0,
    val estimated_time: DateTime? = null,
    val status: String? = null
) 