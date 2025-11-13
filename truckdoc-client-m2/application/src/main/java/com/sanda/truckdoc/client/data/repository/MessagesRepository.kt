package com.sanda.truckdoc.client.data.repository

import com.sanda.truckdoc.client.data.dao.ServerMessageDao
import com.sanda.truckdoc.client.data.model.ServerMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagesRepository @Inject constructor(
    private val messageDao: ServerMessageDao
) {
    fun getMessages(): Flow<List<ServerMessage>> = messageDao.getAll()

    fun getPendingMessages(): Flow<List<ServerMessage>> = flow {
        val messages = messageDao.getAllSync()
        val pendingMessages = messages.filter { !it.sent && it.outgoing }
        emit(pendingMessages)
    }

    suspend fun insertMessage(message: ServerMessage): Long {
        return messageDao.insert(message)
    }

    suspend fun insertMessages(messages: List<ServerMessage>) {
        for (message in messages) {
            messageDao.insert(message)
        }
    }

    suspend fun updateMessage(message: ServerMessage) {
        messageDao.update(message)
    }

    suspend fun deleteMessage(message: ServerMessage) {
        messageDao.delete(message)
    }

    suspend fun getMessageById(id: Int): ServerMessage? {
        return messageDao.getById(id)
    }

    suspend fun getMessageByServerId(serverId: Int): List<ServerMessage> {
        return messageDao.getByServerId(serverId)
    }
} 