package com.sanda.truckdoc.client.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_records",
    indices = [Index(value = ["phone"], unique = true)]
)
data class DbContactRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String = "",
    val phone: String = "",
    val role: String = "",
    val recipientId: Long = 0L,
    val recipientIdType: String = ""
) {
    // Constructor for Java compatibility
    constructor(id: Int, label: String, phone: String, role: String) : this(
        id = id,
        label = label,
        phone = phone,
        role = role,
        recipientId = 0L,
        recipientIdType = ""
    )
    
    fun getColor(): Int = 0xFF0000.toInt() // Default red color
    fun getRecordId(): Long = id.toLong()
    
    // Java compatibility methods
    fun setPhone(phone: String): DbContactRecord = copy(phone = phone)
    fun setLabel(label: String): DbContactRecord = copy(label = label)
    fun setRole(role: String): DbContactRecord = copy(role = role)
} 