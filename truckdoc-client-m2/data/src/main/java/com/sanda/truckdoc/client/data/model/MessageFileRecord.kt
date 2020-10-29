package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.data.model.file.FileType
import org.joda.time.DateTime
import java.io.File

@Entity(tableName = "message_files")
data class MessageFileRecord(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0,

        var name: String? = null,

        private var path: String? = null,

        var type: String? = null,

        var metadata: String? = null,

        var serverId: Long? = null,

        var recipientId: Long? = null,

        var isSent: Boolean = false,

        var isReadyForSend: Boolean = false,

        val creationTime: DateTime = DateTime.now()
) {
    fun getPath(): String? {
        return path
    }

    fun setPath(path: String?) {
        this.path = path
        name = File(path).name
    }

    fun setType(type: FileType) {
        this.type = type.name
    }
}
