package com.sanda.truckdoc.client.data.api

import com.sanda.truckdoc.client.data.model.ServerMessage
import retrofit2.http.*

interface ApiService {
    @GET("messages")
    suspend fun getMessages(): List<ServerMessage>

    @POST("messages")
    suspend fun sendMessage(@Body message: ServerMessage): ServerMessage

    @DELETE("messages/{id}")
    suspend fun deleteMessage(@Path("id") id: Long)

    @PUT("messages/{id}")
    suspend fun updateMessage(@Path("id") id: Long, @Body message: ServerMessage): ServerMessage
} 