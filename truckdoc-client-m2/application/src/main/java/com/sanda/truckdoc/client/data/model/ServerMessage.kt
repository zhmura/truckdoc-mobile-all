package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.room.Ignore
import com.sanda.truckdoc.client.data.Converters
import org.joda.time.DateTime

@Entity(tableName = "server_message")
@TypeConverters(Converters::class)
data class ServerMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val serverMessageId: Int = 0,
    val message: String = "",
    val sender: String = "",
    val recipient: String = "",
    val savedDate: DateTime = DateTime.now(),
    val sentDate: DateTime? = null,
    val outgoing: Boolean = false,
    val sent: Boolean = false,
    val downloaded: Boolean = false,
    val hidden: Boolean = false,
    val type: String = "SMS",
    val priority: Int = 0,
    val read: Boolean = false,
    val tags: String? = null,
    val senderUserId: Long? = null,
    val senderVirtualGroupId: Long? = null,
    val recipientId: Long? = null,
    val attachments: List<AttachmentInfo>? = null
) {
    // Java compatibility methods
    @Ignore
    fun getSaved(): String = savedDate.toString()
    @Ignore
    fun getText(): String = message
    @Ignore
    fun isSent(): Boolean = sent
    @Ignore
    fun isHidden(): Boolean = hidden
    @Ignore
    fun isOutgoing(): Boolean = outgoing
    @Ignore
    fun getSenderName(): String? = null // This would need to be populated from API data
    @Ignore
    fun getSenderRoleId(): Long? = null // This would need to be populated from API data
} 