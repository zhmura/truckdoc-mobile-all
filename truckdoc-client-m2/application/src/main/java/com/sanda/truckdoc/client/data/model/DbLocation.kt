package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sanda.truckdoc.client.data.Converters
import com.sanda.truckdoc.client.api.model.LocationRecord
import org.joda.time.DateTime
import java.util.Date

@Entity(tableName = "locations")
@TypeConverters(Converters::class)
data class DbLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val time: DateTime = DateTime.now(),
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val altitude: Double = 0.0,
    val provider: String = ""
) {
    fun toLocationRecord(): LocationRecord {
        val record = LocationRecord()
        record.time = time.toDate()
        record.lat = latitude.toString()
        record.lng = longitude.toString()
        return record
    }
} 