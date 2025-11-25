package com.sanda.truckdoc.client.util.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.DeserializationFeature
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Jackson implementation of IJsonParser.
 * Used for legacy code compatibility during migration.
 */
@Singleton
class JacksonParser @Inject constructor() : IJsonParser {
    
    private val objectMapper = ObjectMapper().apply {
        // Configure standard settings to match typical Android behavior
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    override fun <T> fromJson(json: String, type: Class<T>): T? {
        return try {
            objectMapper.readValue(json, type)
        } catch (e: Exception) {
            null
        }
    }

    override fun <T> fromJson(json: String, type: Type): T? {
        return try {
            val javaType = objectMapper.typeFactory.constructType(type)
            objectMapper.readValue(json, javaType)
        } catch (e: Exception) {
            null
        }
    }

    override fun <T> toJson(obj: T, type: Class<T>): String {
        return try {
            objectMapper.writeValueAsString(obj)
        } catch (e: Exception) {
            ""
        }
    }

    override fun <T> toJson(obj: T, type: Type): String {
        return try {
            objectMapper.writeValueAsString(obj)
        } catch (e: Exception) {
            ""
        }
    }
}

