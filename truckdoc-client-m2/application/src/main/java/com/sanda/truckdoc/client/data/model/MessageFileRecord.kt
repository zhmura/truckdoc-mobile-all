package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.data.model.file.FileType
import org.joda.time.DateTime

@Entity(tableName = "message_files")
data class MessageFileRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val path: String = "",
    val type: String = "",
    val metadata: String? = null,
    val serverId: Long? = null,
    val recipientId: Long? = null,
    val sent: Boolean = false,
    val markForSend: Boolean = false,
    val creationTime: DateTime = DateTime.now()
) {
    // Java compatibility methods
    fun setPath(path: String): MessageFileRecord = copy(path = path)
    fun setName(name: String): MessageFileRecord = copy(name = name)
    fun setRecipientId(recipientId: Long?): MessageFileRecord = copy(recipientId = recipientId)
    fun setType(type: FileType): MessageFileRecord = copy(type = type.name)
    fun setServerId(serverId: Long?): MessageFileRecord = copy(serverId = serverId)
    fun setSent(sent: Boolean): MessageFileRecord = copy(sent = sent)
    fun setReadyForSend(ready: Boolean): MessageFileRecord = copy(markForSend = ready)
    fun isReadyForSend(): Boolean = markForSend && !sent
} 