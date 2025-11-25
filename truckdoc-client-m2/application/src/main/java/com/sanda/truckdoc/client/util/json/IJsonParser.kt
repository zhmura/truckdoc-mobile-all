package com.sanda.truckdoc.client.util.json

import java.lang.reflect.Type

/**
 * Abstraction layer for JSON parsing to decouple the app from specific libraries.
 * Allows migrating from Jackson/Gson to Moshi incrementally.
 */
interface IJsonParser {
    
    /**
     * Deserialize JSON string to object
     */
    fun <T> fromJson(json: String, type: Class<T>): T?
    
    /**
     * Deserialize JSON string to object with complex type (e.g. List<User>)
     */
    fun <T> fromJson(json: String, type: Type): T?
    
    /**
     * Serialize object to JSON string
     */
    fun <T> toJson(obj: T, type: Class<T>): String
    
    /**
     * Serialize object to JSON string with complex type
     */
    fun <T> toJson(obj: T, type: Type): String
}

