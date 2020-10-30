package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.ClientToServerMessagePojo
import com.sanda.truckdoc.client.api.v2.ServerToClientMessagePojoNew
import com.sanda.truckdoc.util.entity.EntityWithId
import org.joda.time.DateTime


@Entity(tableName = "server_message")
data class ServerMessage(
        @PrimaryKey(autoGenerate = true)
        private var id: Int = 0,

        var recipientId: Int? = null,

        var serverMessageId: Int? = null,

        var senderRoleId: Int? = null,

        var senderUserId: Long? = null,

        var senderVirtualGroupId: Long? = null,

        var senderName: String? = null,

        var text: String? = null,

        var isDownloaded: Boolean = false,

        var isOutgoing: Boolean = false,

        var isSent: Boolean = false,

        var isProcessed: Boolean = false,

        var isHidden: Boolean = false,

        @get:Deprecated("")

        val saved: String? = null,

        var savedDate: DateTime? = null,

/*        @Deprecated("@ForeignCollectionField(eager = true)", level = DeprecationLevel.ERROR)
        @ForeignCollectionField
        private var attachments: ForeignCollection<AttachmentInfo>? = null*/
) : EntityWithId<Int> {

    constructor(pojo: ServerToClientMessagePojoNew) : this(id = pojo.id,
            text = pojo.text,
            serverMessageId = pojo.id,
            senderRoleId = pojo.senderRoleId,
            senderUserId = pojo.senderUserId,
            senderVirtualGroupId = pojo.senderVirtualGroupId,
            senderName = pojo.senderName)

    constructor(pojo: ClientToServerMessagePojo) : this(id = pojo.id,
            text = pojo.text,
            isSent = pojo.isSent,
            recipientId = pojo.recipientId)

    override fun getId(): Int = id

/*    fun getAttachments(): Collection<AttachmentInfo> {
        return if (attachments == null) {
            emptyList()
        } else attachments
    }*/
}
