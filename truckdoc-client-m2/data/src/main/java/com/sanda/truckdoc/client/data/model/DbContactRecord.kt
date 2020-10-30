package com.sanda.truckdoc.client.data.model

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sanda.truckdoc.client.api.model.ContactListAttribute
import com.sanda.truckdoc.client.api.model.ContactRecord
import java.io.Serializable

@Entity(tableName = "contact_records")
data class DbContactRecord(
        @PrimaryKey(autoGenerate = true)
        val recipientId: Long,

        val label: String,

        val recipientIdType: String?,

        val color: Int,

        var phone: String? = null
) : Serializable {

    constructor(contactRecord: ContactRecord) : this(contactRecord.recipientId, contactRecord.label, contactRecord.recipientIdType, if (contactRecord.attributes != null && contactRecord.attributes.containsKey(ContactListAttribute.COLOR.key)) {
        Color.parseColor(contactRecord.attributes[ContactListAttribute.COLOR.key])
    } else {
        Color.WHITE
    })

    val id
        get() = recipientId
}
