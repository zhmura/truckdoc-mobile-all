package com.sanda.truckdoc.client.data.model

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.model.LocationRecord
import java.util.*

/**
 * Created by astra on 14.07.2015.
 */
@Entity(tableName = "locations")
class DbLocation(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,

        val latitude: Double,

        val longitude: Double,

        val altitude: Double,

        val speed: Float,

        val accuracy: Float,

        val time: Long,

        ) {
    constructor(l: Location) : this(0, l.latitude, l.longitude, l.altitude, l.speed, l.accuracy, l.time)

    constructor(latitude: Double,
                longitude: Double,
                altitude: Double,
                speed: Float,
                accuracy: Float,
                time: Long) : this(0, latitude, longitude, altitude, speed, accuracy, time)

    fun toLocationRecord(): LocationRecord {
        val l = LocationRecord()
        l.lat = String.format(Locale.US, "%.6f", latitude) //привет
        l.lng = String.format(Locale.US, "%.6f", longitude)
        l.time = Date(time)
        return l
    }
}
