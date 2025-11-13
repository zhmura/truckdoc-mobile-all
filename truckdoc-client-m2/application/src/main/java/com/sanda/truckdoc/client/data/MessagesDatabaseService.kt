package com.sanda.truckdoc.client.data

import com.sanda.truckdoc.client.api.AttachmentPojo
import com.sanda.truckdoc.client.api.ClientToServerMessagePojo
import com.sanda.truckdoc.client.api.v2.ServerToClientMessagePojoNew
import com.sanda.truckdoc.client.api.v3.sync.routing.model.NamedPoint
import com.sanda.truckdoc.client.data.dao.*
import com.sanda.truckdoc.client.data.model.*
import com.sanda.truckdoc.client.data.model.route.*
import com.sanda.truckdoc.client.util.FileHelper
import com.sanda.truckdoc.client.util.RoleTypeMapper
import com.sanda.truckdoc.client.util.commons.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagesDatabaseService @Inject constructor(
    private val serverMessageDao: ServerMessageDao,
    private val attachmentDao: AttachmentDao,
    private val locationDao: LocationDao,
    private val contactRecordDao: ContactRecordDao,
    private val messageFileDao: MessageFileDao,
    private val routeAssignmentDao: RouteAssignmentDao,
    private val routePointDao: RoutePointDao,
    private val routePathDao: RoutePathDao
) {
    // Static methods for Java compatibility
    companion object {
        suspend fun deleteOldMessages(context: android.content.Context) {
            // This would need to be implemented with proper dependency injection
            Timber.w("deleteOldMessages called but not implemented")
        }
        
        suspend fun saveOutgoingMessages(context: android.content.Context, messages: List<ClientToServerMessagePojo>) {
            // This would need to be implemented with proper dependency injection
            Timber.w("saveOutgoingMessages called but not implemented")
        }
        
        suspend fun saveMessageIfNotExist(context: android.content.Context, message: ServerToClientMessagePojoNew) {
            // This would need to be implemented with proper dependency injection
            Timber.w("saveMessageIfNotExist called but not implemented")
        }
        
        suspend fun getNotDownloadedMessages(context: android.content.Context): List<ServerMessage> {
            // This would need to be implemented with proper dependency injection
            Timber.w("getNotDownloadedMessages called but not implemented")
            return emptyList()
        }
        
        suspend fun getNotDownloadedFiles(context: android.content.Context, message: ServerMessage): List<AttachmentInfo> {
            // This would need to be implemented with proper dependency injection
            Timber.w("getNotDownloadedFiles called but not implemented")
            return emptyList()
        }
    }

    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            deleteAllMessages()
            deleteAllMessageFileRecords()
            contactRecordDao.deleteAll()
            locationDao.deleteAll()
            routeAssignmentDao.deleteAll()
            routePathDao.deleteAll()
            routePointDao.deleteAll()
        }
    }

    suspend fun initializeRoutePoints(routeId: Long, points: List<NamedPoint>) {
        withContext(Dispatchers.IO) {
            for ((index, point) in points.withIndex()) {
                val routePoint = DbRoutePoint(
                    id = 0,
                    path_id = routeId,
                    name = point.name ?: "Point ${index + 1}",
                    latitude = point.lat,
                    longitude = point.lng,
                    order = index
                )
                routePointDao.insert(routePoint)
            }
        }
    }

    suspend fun markMessageAsDownloaded(message: ServerMessage): Int {
        return withContext(Dispatchers.IO) {
            val updatedMessage = message.copy(downloaded = true)
            serverMessageDao.update(updatedMessage)
        }
    }

    suspend fun insertLocation(location: DbLocation): Long {
        return withContext(Dispatchers.IO) {
            locationDao.insert(location)
        }
    }

    suspend fun insertRouteAssignment(routeAssignment: DbRouteAssignment): Long {
        return withContext(Dispatchers.IO) {
            routeAssignmentDao.insert(routeAssignment)
        }
    }

    fun findRouteAssignmentByServerId(serverRouteAssignmentId: Long): Flow<List<DbRouteAssignment>> {
        return routeAssignmentDao.findByServerId(serverRouteAssignmentId)
    }

    fun findRouteAssignmentById(routeAssignmentId: Long): Flow<DbRouteAssignment?> {
        return routeAssignmentDao.findById(routeAssignmentId)
    }

    suspend fun addSmsMessage(serverMessage: ServerMessage) {
        withContext(Dispatchers.IO) {
            serverMessageDao.insert(serverMessage)
        }
    }

    suspend fun markHidden(serverMessage: ServerMessage) {
        withContext(Dispatchers.IO) {
            val updatedMessage = serverMessage.copy(hidden = true)
            serverMessageDao.update(updatedMessage)
        }
    }

    fun getLocations(): Flow<List<DbLocation>> {
        return locationDao.getAll()
    }

    fun getContactRecords(): Flow<List<DbContactRecord>> {
        return contactRecordDao.getAll()
    }

    fun getMessageFiles(): Flow<List<MessageFileRecord>> {
        return messageFileDao.getAll()
    }

    fun getNotSentMessageFiles(): Flow<List<MessageFileRecord>> {
        return messageFileDao.getNotSent()
    }

    fun getNotUploadedMessageFiles(): Flow<List<MessageFileRecord>> {
        return messageFileDao.getNotUploaded()
    }

    suspend fun createMessageFileRecord(record: MessageFileRecord) {
        withContext(Dispatchers.IO) {
            messageFileDao.insert(record)
        }
    }

    suspend fun deleteMessageFileRecords(records: List<MessageFileRecord>) {
        withContext(Dispatchers.IO) {
            for (record in records) {
                File(record.path).delete()
            }
            messageFileDao.delete(records)
        }
    }

    suspend fun deleteAllMessageFileRecords() {
        withContext(Dispatchers.IO) {
            val records = messageFileDao.getAllSync()
            for (record in records) {
                File(record.path).delete()
            }
            messageFileDao.deleteAll()
        }
    }

    suspend fun updateMessageFileRecord(record: MessageFileRecord) {
        withContext(Dispatchers.IO) {
            messageFileDao.update(record)
        }
    }

    suspend fun replaceContactRecords(newContacts: List<DbContactRecord>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                contactRecordDao.deleteAll()
                contactRecordDao.insertAll(newContacts)
                true
            } catch (e: Exception) {
                Timber.e(e, "Replace Contact Records failed")
                false
            }
        }
    }

    suspend fun updateContactRecord(record: DbContactRecord): Int {
        return withContext(Dispatchers.IO) {
            contactRecordDao.update(record)
        }
    }

    suspend fun deleteAssignments() {
        withContext(Dispatchers.IO) {
            val assignments = routeAssignmentDao.getAllSync()
            for (assignment in assignments) {
                routePointDao.deleteByPathId(assignment.route_id)
                val route = routePathDao.findById(assignment.route_id).first()
                route?.let { routePathDao.delete(it) }
                routeAssignmentDao.delete(assignment)
            }
        }
    }

    suspend fun deleteLocations(locations: List<DbLocation>) {
        withContext(Dispatchers.IO) {
            locationDao.delete(locations)
        }
    }

    suspend fun markFileAsDownloaded(attachment: AttachmentInfo): Int {
        return withContext(Dispatchers.IO) {
            val updatedAttachment = attachment.copy(downloaded = true)
            attachmentDao.update(updatedAttachment)
        }
    }

    suspend fun deleteAllMessages() {
        withContext(Dispatchers.IO) {
            val messages = serverMessageDao.getAllSync()
            for (message in messages) {
                try {
                    FileUtils.deleteDirectory(FileHelper.getIncomeDirectory(message.id, "*"))
                } catch (e: IOException) {
                    Timber.e(e)
                }
                serverMessageDao.delete(message)
            }
        }
    }

    fun getMessages(showHidden: Boolean): Flow<List<ServerMessage>> {
        return if (showHidden) {
            serverMessageDao.getAll()
        } else {
            serverMessageDao.getNotHidden()
        }
    }

    fun getPendingOutMessages(): Flow<List<ServerMessage>> {
        return serverMessageDao.getPendingOutMessages()
    }

    suspend fun deleteOldMessages() {
        withContext(Dispatchers.IO) {
            val messages = serverMessageDao.getAllSync()
            val twoMonthsAgo = DateTime.now().minusMonths(2)
            
            for (message in messages) {
                if (message.savedDate.isBefore(twoMonthsAgo)) {
                    try {
                        FileUtils.deleteDirectory(FileHelper.getIncomeDirectory(message.id, "*"))
                        serverMessageDao.delete(message)
                    } catch (e: IOException) {
                        Timber.e(e)
                    }
                }
            }
        }
    }

    suspend fun saveOutgoingMessages(messages: List<ClientToServerMessagePojo>) {
        withContext(Dispatchers.IO) {
            for (message in messages) {
                val serverMessage = ServerMessage(
                    id = 0,
                    message = message.text,
                    savedDate = DateTime.now(),
                    recipientId = message.recipientId?.toLong(),
                    senderUserId = null,
                    senderVirtualGroupId = null,
                    downloaded = true,
                    hidden = false,
                    outgoing = true,
                    sent = message.isSent(),
                    attachments = emptyList()
                )
                serverMessageDao.insert(serverMessage)
            }
        }
    }

    suspend fun saveMessageIfNotExist(message: ServerToClientMessagePojoNew) {
        withContext(Dispatchers.IO) {
            val existingMessage = serverMessageDao.getByServerId(message.id)
            if (existingMessage.isEmpty()) {
                val serverMessage = ServerMessage(
                    id = 0,
                    serverMessageId = message.id,
                    message = message.text,
                    savedDate = DateTime.now(),
                    recipientId = null, // This field doesn't exist in the message
                    senderUserId = message.senderUserId,
                    senderVirtualGroupId = message.senderVirtualGroupId,
                    downloaded = false,
                    hidden = false,
                    outgoing = false,
                    sent = false,
                    attachments = emptyList()
                )
                val messageId = serverMessageDao.insert(serverMessage)
                
                // Insert attachments
                for (attachment in message.attachments) {
                    val attachmentInfo = AttachmentInfo(
                        id = 0,
                        message_id = messageId.toInt(),
                        name = attachment.fileName,
                        downloaded = false
                    )
                    attachmentDao.insert(attachmentInfo)
                }
            }
        }
    }

    suspend fun getNotDownloadedMessages(): List<ServerMessage> {
        return withContext(Dispatchers.IO) {
            val allMessages = serverMessageDao.getAllSync()
            allMessages.filter { !it.downloaded }
        }
    }

    suspend fun getNotDownloadedFiles(message: ServerMessage): List<AttachmentInfo> {
        return withContext(Dispatchers.IO) {
            attachmentDao.getNotDownloadedByMessageId(message.id)
        }
    }

    suspend fun updateMessageFile(file: MessageFileRecord): Int {
        return withContext(Dispatchers.IO) {
            messageFileDao.update(file)
        }
    }
} 