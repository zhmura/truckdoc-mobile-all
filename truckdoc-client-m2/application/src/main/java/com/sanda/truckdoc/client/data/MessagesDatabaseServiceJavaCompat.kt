package com.sanda.truckdoc.client.data

import com.sanda.truckdoc.client.data.model.DbContactRecord
import com.sanda.truckdoc.client.data.model.DbLocation
import com.sanda.truckdoc.client.data.model.ServerMessage
import com.sanda.truckdoc.client.data.model.AttachmentInfo
import com.sanda.truckdoc.client.data.model.MessageFileRecord
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment
import com.sanda.truckdoc.client.data.model.route.DbRoutePath
import com.sanda.truckdoc.client.api.v3.sync.routing.model.NamedPoint
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

object MessagesDatabaseServiceJavaCompat {
    @JvmStatic
    fun getContactRecordsBlocking(service: MessagesDatabaseService): List<DbContactRecord> = runBlocking {
        service.getContactRecords().first()
    }

    @JvmStatic
    fun getLocationsBlocking(service: MessagesDatabaseService): List<DbLocation> = runBlocking {
        service.getLocations().first()
    }

    @JvmStatic
    fun getMessagesBlocking(service: MessagesDatabaseService, showHidden: Boolean): List<ServerMessage> = runBlocking {
        service.getMessages(showHidden).first()
    }

    @JvmStatic
    fun addSmsMessageBlocking(service: MessagesDatabaseService, message: ServerMessage) = runBlocking {
        service.addSmsMessage(message)
    }

    @JvmStatic
    fun markHiddenBlocking(service: MessagesDatabaseService, message: ServerMessage) = runBlocking {
        service.markHidden(message)
    }

    @JvmStatic
    fun deleteAllMessagesBlocking(service: MessagesDatabaseService) = runBlocking {
        service.deleteAllMessages()
    }

    @JvmStatic
    fun insertLocationBlocking(service: MessagesDatabaseService, location: DbLocation): Long = runBlocking {
        service.insertLocation(location)
    }

    @JvmStatic
    fun deleteLocationsBlocking(service: MessagesDatabaseService, locations: List<DbLocation>) = runBlocking {
        service.deleteLocations(locations)
    }

    @JvmStatic
    fun markFileAsDownloadedBlocking(service: MessagesDatabaseService, attachment: AttachmentInfo): Int = runBlocking {
        service.markFileAsDownloaded(attachment)
    }

    @JvmStatic
    fun markMessageAsDownloadedBlocking(service: MessagesDatabaseService, message: ServerMessage): Int = runBlocking {
        service.markMessageAsDownloaded(message)
    }

    @JvmStatic
    fun replaceContactRecordsBlocking(service: MessagesDatabaseService, contacts: List<DbContactRecord>): Boolean = runBlocking {
        service.replaceContactRecords(contacts)
    }

    @JvmStatic
    fun createMessageFileRecordBlocking(service: MessagesDatabaseService, record: MessageFileRecord) = runBlocking {
        service.createMessageFileRecord(record)
    }

    @JvmStatic
    fun deleteAllMessageFileRecordsBlocking(service: MessagesDatabaseService) = runBlocking {
        service.deleteAllMessageFileRecords()
    }

    @JvmStatic
    fun deleteAllDataBlocking(service: MessagesDatabaseService) = runBlocking {
        service.deleteAllData()
    }

    @JvmStatic
    fun updateContactRecordBlocking(service: MessagesDatabaseService, record: DbContactRecord): Int = runBlocking {
        service.updateContactRecord(record)
    }

    @JvmStatic
    fun updateMessageFileRecordBlocking(service: MessagesDatabaseService, record: MessageFileRecord) = runBlocking {
        service.updateMessageFileRecord(record)
    }

    @JvmStatic
    fun deleteMessageFileRecordsBlocking(service: MessagesDatabaseService, records: List<MessageFileRecord>) = runBlocking {
        service.deleteMessageFileRecords(records)
    }

    @JvmStatic
    fun initializeRoutePointsBlocking(service: MessagesDatabaseService, routeId: Long, points: List<NamedPoint>) = runBlocking {
        service.initializeRoutePoints(routeId, points)
    }

    @JvmStatic
    fun findRouteAssignmentByServerIdBlocking(service: MessagesDatabaseService, serverId: Long): List<DbRouteAssignment> = runBlocking {
        service.findRouteAssignmentByServerId(serverId).first()
    }

    @JvmStatic
    fun findRouteAssignmentByIdBlocking(service: MessagesDatabaseService, id: Long): DbRouteAssignment? = runBlocking {
        service.findRouteAssignmentById(id).first()
    }

    @JvmStatic
    fun insertRouteAssignmentBlocking(service: MessagesDatabaseService, assignment: DbRouteAssignment): Long = runBlocking {
        service.insertRouteAssignment(assignment)
    }

    @JvmStatic
    fun getNotUploadedMessageFilesBlocking(service: MessagesDatabaseService): List<MessageFileRecord> = runBlocking {
        service.getNotUploadedMessageFiles().first()
    }

    @JvmStatic
    fun getNotSentMessageFilesBlocking(service: MessagesDatabaseService): List<MessageFileRecord> = runBlocking {
        service.getNotSentMessageFiles().first()
    }

    @JvmStatic
    fun deleteOldMessagesBlocking(service: MessagesDatabaseService) = runBlocking {
        service.deleteOldMessages()
    }

    @JvmStatic
    fun saveOutgoingMessagesBlocking(service: MessagesDatabaseService, messages: List<Any>) = runBlocking {
        service.saveOutgoingMessages(messages as List<com.sanda.truckdoc.client.api.ClientToServerMessagePojo>)
    }

    @JvmStatic
    fun saveMessageIfNotExistBlocking(service: MessagesDatabaseService, message: Any) = runBlocking {
        service.saveMessageIfNotExist(message as com.sanda.truckdoc.client.api.v2.ServerToClientMessagePojoNew)
    }

    @JvmStatic
    fun getNotDownloadedMessagesBlocking(service: MessagesDatabaseService): List<ServerMessage> = runBlocking {
        service.getNotDownloadedMessages()
    }

    @JvmStatic
    fun getNotDownloadedFilesBlocking(service: MessagesDatabaseService, message: ServerMessage): List<AttachmentInfo> = runBlocking {
        service.getNotDownloadedFiles(message)
    }

    @JvmStatic
    fun updateMessageFileBlocking(service: MessagesDatabaseService, file: MessageFileRecord): Int = runBlocking {
        service.updateMessageFile(file)
    }
} 