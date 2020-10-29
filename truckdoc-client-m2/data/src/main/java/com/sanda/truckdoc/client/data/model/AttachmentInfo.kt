package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.AttachmentPojo
import com.sanda.truckdoc.util.entity.EntityWithId

/**
 * @author Alexei Osipov
 */
@Entity(tableName = "attachment_info")
data class AttachmentInfo(
        @PrimaryKey(autoGenerate = true)
        @JvmField
        var id: Int = 0,
        var serverId: Int? = null,
        var fileSize: Long? = null,
        var fileName: String? = null,
        var mimeType: String? = null,
        var isDownloaded: Boolean = false,

        var messageId: Int = 0,
) : EntityWithId<Int> {

    constructor(pojo: AttachmentPojo) : this(pojo.id,
            pojo.id,
            pojo.fileSize,
            pojo.fileName,
            pojo.mimeType)

    override fun getId(): Int = id
}
