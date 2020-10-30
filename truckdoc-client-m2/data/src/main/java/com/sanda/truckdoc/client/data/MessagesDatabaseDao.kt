@file:Suppress("unused")

package com.sanda.truckdoc.client.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanda.truckdoc.client.api.AttachmentPojo
import com.sanda.truckdoc.client.api.ClientToServerMessagePojo
import com.sanda.truckdoc.client.api.v2.ServerToClientMessagePojoNew
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RouteAssignment
import com.sanda.truckdoc.client.api.v3.sync.routing.model.RoutePath
import com.sanda.truckdoc.client.data.model.*
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment
import com.sanda.truckdoc.client.data.model.route.DbRoutePath
import com.sanda.truckdoc.client.data.model.route.DbRoutePoint
import common.toObservable
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.sql.SQLException
import javax.inject.Inject
import javax.inject.Singleton

interface BaseDao<T> {

    @Insert
    fun create(message: T): Long

    @Update
    fun update(message: T): Int

    @Delete
    fun delete(message: T)

    @Delete
    fun delete(message: List<T>)
}

@Dao
interface FileRecordDao : BaseDao<MessageFileRecord> {
    @Query("select * from message_files")
    fun queryForAll(): List<MessageFileRecord>

    @Query("select * from message_files where isSent = 0")
    fun queryNotSent(): List<MessageFileRecord>

    @Query("select * from message_files where serverId = NULL")
    fun queryNotUploaded(): List<MessageFileRecord>
}

@Dao
interface ServerMessageDao : BaseDao<ServerMessage> {

    @Query("select * from server_message")
    fun findAll(): List<ServerMessage>

    @Query("SELECT * FROM server_message WHERE (isOutgoing=1 OR (isOutgoing=0 AND isDownloaded=1)) order by savedDate desc")
    fun findMessagesWithHidden(): LiveData<List<ServerMessage>>

    @Query("SELECT * FROM server_message WHERE (isOutgoing=1 OR (isOutgoing=0 AND isDownloaded=1)) AND isHidden=0 order by savedDate desc")
    fun findMessages(): LiveData<List<ServerMessage>>

    @Query("select * from server_message where isDownloaded = 0")
    fun findNotDownloadedMessages(): List<ServerMessage>

    @Query("select * from server_message where isSent = 0 and isOutgoing = 1")
    fun findPendingOutMessages(): List<ServerMessage>

    @Query("select * from server_message where serverMessageId = :id")
    fun findByServerMessage(id: Int): List<ServerMessage>

    @Query("select * from server_message where id = :id")
    fun queryForId(id: Int): ServerMessage?

    @Insert
    fun saveAttachment(attachmentInfo: AttachmentInfo)

    @Query("select * from attachment_info where messageId = :id and isDownloaded = 0")
    fun getNotDownloadedFiles(id: Int): List<AttachmentInfo>

    @Query("select * from server_message order by saved asc")
    fun findOldMessages(): List<ServerMessage>

    @Update
    fun update(message: AttachmentInfo)

    @Insert
    fun create(message: DbLocation): Long

    @Query("select * from locations limit 100")
    fun findLocations(): List<DbLocation>

    @Delete
    fun deleteLocations(locations: List<DbLocation>?)

    @Query("delete from contact_records")
    fun clearContacts()

    @Insert
    fun insertContacts(newContacts: List<DbContactRecord>)

    @Transaction
    fun replaceContacts(newContacts: List<DbContactRecord>) {
        clearContacts()
        insertContacts(newContacts)
    }

    @Insert
    fun update(record: DbContactRecord)

    @Query("select * from contact_records")
    fun findContacts(): List<DbContactRecord>

    @Query("select * from contact_records")
    fun findContactsLive(): LiveData<List<DbContactRecord>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun createAssignment(dbRouteAssignment: DbRouteAssignment): Long

    @Insert
    fun createPath(path: DbRoutePath): Long

    @Insert
    fun createPoints(points: List<DbRoutePoint>)

    @Query("select * from route_assignments where serverAssignmentId = :serverRouteAssignmentId")
    fun findRouteAssignmentByServerId(serverRouteAssignmentId: Long?): List<DbRouteAssignment>

    @Query("select * from route_assignments where id = :routeAssignmentId")
    fun findAssignmentById(routeAssignmentId: Long): DbRouteAssignment

    @Query("select * from route_assignments")
    fun findAssignments(): List<DbRouteAssignment>

    @Delete
    fun deleteAssignment(assignments: List<DbRouteAssignment>)

    @Query("select * from route_assignments where id = :id")
    fun findAssignmentWithPath(id: Long): DbRouteAssignmentWithPath?
}

data class DbRouteAssignmentWithPath(
        @Embedded
        val assignment: DbRouteAssignment,
        @Relation(entity = DbRoutePath::class,
                parentColumn = "id",
                entityColumn = "routeAssignmentId")
        val pathWithPoints: DbPathWithPoints,
)

data class DbPathWithPoints(
        @Embedded
        val path: DbRoutePath,
        @Relation(parentColumn = "id", entityColumn = "pathId")
        val points: List<DbRoutePoint>
)

@Singleton
class MessagesDatabaseService @Inject constructor(
        val serverMessagesDao: ServerMessageDao,
        val fileRecordDao: FileRecordDao,
        val deleter: Deleter,
        val db: MessageDatabase
) {

    fun markMessageAsDownloaded(message: ServerMessage): Observable<Int> {
        return Observable.fromCallable {
            message.isDownloaded = true
            serverMessagesDao.update(message)
        }
    }

    fun insertLocation(location: DbLocation): Observable<Int> {
        return Observable.fromCallable {
            serverMessagesDao.create(location)
            1
        }
    }

    fun findRouteAssignmentByServerId(serverRouteAssignmentId: Long?): List<DbRouteAssignment> {
        return serverMessagesDao.findRouteAssignmentByServerId(serverRouteAssignmentId)
    }

    fun findRouteAssignmentById(routeAssignmentId: Long): DbRouteAssignmentWithPath? {
        return serverMessagesDao.findAssignmentWithPath(routeAssignmentId)
    }

    fun insertRouteAssignmentAndPath(routeAssignment: RouteAssignment, routePath: RoutePath): DbRouteAssignment {
        val dbRouteAssignment = DbRouteAssignment(routeAssignment)

        val id = serverMessagesDao.createAssignment(dbRouteAssignment)

        val path = DbRoutePath(routePath, routeAssignment.routeId).apply {
            routeAssignmentId = id
        }

        val pathId = serverMessagesDao.createPath(path)

        val points = routePath.points.map {
            DbRoutePoint(it).apply { this.pathId = pathId }
        }
        serverMessagesDao.createPoints(points)

        return dbRouteAssignment.copy(id = id)
    }

    fun addSmsMessage(serverMessage: ServerMessage) {
        serverMessagesDao.create(serverMessage)
    }

    fun markHidden(serverMessage: ServerMessage) {
        serverMessage.isHidden = true
        serverMessagesDao.update(serverMessage)
    }

    val locations: List<DbLocation>
        get() = try {
            serverMessagesDao.findLocations()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    val contactRecords: Observable<DbContactRecord>
        get() = Observable.fromCallable { serverMessagesDao.findContacts() }.flatMapIterable { it }

    fun getContacts() = serverMessagesDao.findContactsLive()

    val messageFiles: Observable<MessageFileRecord>
        get() = Observable.fromCallable { fileRecordDao.queryForAll() }.flatMap { iterable: List<MessageFileRecord>? -> Observable.from(iterable) }
    val notSentMessageFiles: Observable<MessageFileRecord>
        get() = Observable.fromCallable { fileRecordDao.queryNotSent() }.flatMap { iterable: List<MessageFileRecord>? -> Observable.from(iterable) }
    val notUploadedMessageFiles: Observable<MessageFileRecord>
        get() = Observable.fromCallable { fileRecordDao.queryNotUploaded() }.flatMap { iterable: List<MessageFileRecord>? -> Observable.from(iterable) }

    fun createMessageFileRecord(record: MessageFileRecord) {
        fileRecordDao.create(record)
    }

    fun deleteMessageFileRecords(records: List<MessageFileRecord>) {
        for (record in records) {
            File(record.getPath()).delete()
        }
        fileRecordDao.delete(records)
    }

    fun deleteAllMessageFileRecords() {
        val records = fileRecordDao.queryForAll()
        for (record in records) {
            val f = File(record.getPath())
            if (f.exists()) f.delete()
        }
        fileRecordDao.delete(records)
    }

    fun updateMessageFileRecord(record: MessageFileRecord) {
        fileRecordDao.update(record)
    }

    fun replaceContactRecords(newContacts: List<DbContactRecord>): Boolean {
        return try {
            serverMessagesDao.replaceContacts(newContacts)
            true
        } catch (e: SQLException) {
            Log.e("tag", "Replace Contact Records failed", e)
            false
        }
    }

    fun updateContactRecord(record: DbContactRecord): Observable<Int> {
        return Observable.fromCallable {
            serverMessagesDao.update(record)
            1
        }
    }

    fun deleteAssignments() {
        serverMessagesDao.deleteAssignment(serverMessagesDao.findAssignments())
    }

    fun deleteLocations(locations: List<DbLocation>?) {
        serverMessagesDao.deleteLocations(locations)
    }

    fun markFileAsDownloaded(attachment: AttachmentInfo): Observable<Int> {
        return Observable.fromCallable {
            attachment.isDownloaded = true
            serverMessagesDao.update(attachment)
            1
        }
    }

    fun deleteAllMessages(): Observable<Int> {
        return Observable.fromCallable {
            var list: List<ServerMessage> = serverMessagesDao.findAll()
            for (message in list) {
                try {
                    deleter.deleteDirectoryForMessage(message.id)
                } catch (e: IOException) {
                    //
                }
                serverMessagesDao.delete(message)
            }
            0
        }.subscribeOn(Schedulers.newThread())
    }

    fun getMessages(showHidden: Boolean): Observable<List<ServerMessage>> {
        return getMessagesLive(!showHidden).toObservable()
    }

    fun getMessagesLive(showHidden: Boolean): LiveData<List<ServerMessage>> {
        return if (showHidden) serverMessagesDao.findMessagesWithHidden()
        else serverMessagesDao.findMessages()
    }

    fun deleteAllData() {
        try {
            deleter.deleteAll()
            db.clearAllTables()
        } catch (e: Exception) {
            //throw new RuntimeException(e);
        }
    }

    fun getNotDownloadedMessages(): List<ServerMessage> {
        return serverMessagesDao.findNotDownloadedMessages()
    }

    fun getPendingOutMessages(): List<ServerMessage> {
        return serverMessagesDao.findPendingOutMessages()
    }

    fun saveMessageIfNotExist(messagePojo: ServerToClientMessagePojoNew) {
        val oldMessages = serverMessagesDao.findByServerMessage(messagePojo.id)
        if (oldMessages.isEmpty()) {
            val message = ServerMessage(messagePojo)
            message.savedDate = DateTime.now()
            message.serverMessageId = messagePojo.id
            if (messagePojo.attachments.size > 0) {
                message.isDownloaded = false
            }
            serverMessagesDao.create(message)
            saveFiles(messagePojo.attachments, message)
        }
    }

    fun saveOutgoingMessages(messagePojos: Collection<ClientToServerMessagePojo>) {

        for (messagePojo in messagePojos) {
            var oldMessage: ServerMessage? = null
            if (messagePojo.id != null) {
                oldMessage = serverMessagesDao.queryForId(messagePojo.id)
                oldMessage!!.isSent = messagePojo.isSent
                serverMessagesDao.update(oldMessage)
            }
            if (oldMessage == null) {
                messagePojo.id = 0
                val message = ServerMessage(messagePojo)
                message.savedDate = DateTime.now()
                message.isOutgoing = true
                message.isSent = messagePojo.isSent
                message.isProcessed = false
                message.isDownloaded = true
                message.recipientId = messagePojo.recipientId
                serverMessagesDao.create(message)
                saveFiles(messagePojo.attachments, message)
            }
        }
    }

    fun saveFiles(attachmentPojos: Collection<AttachmentPojo?>, message: ServerMessage) {
        for (attachmentPojo in attachmentPojos) {
            val attachmentInfo = AttachmentInfo(attachmentPojo!!)
            attachmentInfo.messageId = message.id
            serverMessagesDao.saveAttachment(attachmentInfo)
        }
    }

    fun getNotDownloadedFiles(message: ServerMessage): List<AttachmentInfo> {
        return serverMessagesDao.getNotDownloadedFiles(message.id)
    }

    fun deleteOldMessages() {
        val list = serverMessagesDao.findOldMessages()
        for (message in list) {
            val date = message.savedDate
            try {
                if (date!!.isBefore(DateTime.now().minusMonths(2))) {
                    deleter.deleteDirectoryForMessage(message.id)
                    serverMessagesDao.delete(message)
                }
            } catch (e: IOException) {
                //
            }
        }
    }
}

interface Deleter {
    fun deleteDirectoryForMessage(id: Int)

    fun deleteAll()
}

class Converters {
    @TypeConverter
    fun fromStringToDatetime(s: String?) = s?.let { DateTime.parse(it) }

    @TypeConverter
    fun fromTypeToString(e: DateTime) = e.toString(ISODateTimeFormat.dateTime())
}

@TypeConverters(Converters::class)
@Database(entities = [
    ServerMessage::class,
    MessageFileRecord::class,
    DbLocation::class,
    DbContactRecord::class,
    AttachmentInfo::class,
    DbRouteAssignment::class,
    DbRoutePoint::class,
    DbRoutePath::class
], version = 1)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): ServerMessageDao
    abstract fun fileRecordDao(): FileRecordDao
}

object OnCreateCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
    }
}
