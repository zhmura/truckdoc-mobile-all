package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachment_info",
    foreignKeys = [
        ForeignKey(
            entity = ServerMessage::class,
            parentColumns = ["id"],
            childColumns = ["message_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("message_id")]
)
data class AttachmentInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message_id: Int = 0,
    val name: String = "",
    val mimeType: String = "",
    val size: Long = 0,
    val downloaded: Boolean = false,
    val path: String = ""
) {
    // Java compatibility methods
    @Ignore
    fun isDownloaded(): Boolean = downloaded
    fun getServerId(): Int = id
    fun getFileName(): String = name
    fun getMessage(): ServerMessage? = null // This would need to be implemented with a proper relationship
} 