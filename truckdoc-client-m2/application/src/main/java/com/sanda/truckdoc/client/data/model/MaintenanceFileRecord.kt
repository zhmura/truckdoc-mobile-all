package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.sanda.truckdoc.client.data.Converters
import org.joda.time.DateTime

@Entity(tableName = "maintenance_files")
@TypeConverters(Converters::class)
data class MaintenanceFileRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serverId: Int? = null,
    val fileName: String = "",
    val filePath: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val uploadStatus: String = "",
    val createdAt: DateTime = DateTime.now()
) 