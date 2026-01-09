package com.sanda.truckdoc.client.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sanda.truckdoc.client.data.model.AttachmentInfo
import org.joda.time.DateTime

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): DateTime? {
        return value?.let { DateTime(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: DateTime?): Long? {
        return date?.millis
    }

    @TypeConverter
    fun fromAttachmentInfoList(value: List<AttachmentInfo>?): String? {
        return value?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun toAttachmentInfoList(value: String?): List<AttachmentInfo>? {
        return value?.let {
            val listType = object : TypeToken<List<AttachmentInfo>>() {}.type
            Gson().fromJson(it, listType)
        }
    }
} 