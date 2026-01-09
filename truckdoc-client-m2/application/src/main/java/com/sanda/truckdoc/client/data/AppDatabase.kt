package com.sanda.truckdoc.client.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sanda.truckdoc.client.data.dao.*
import com.sanda.truckdoc.client.data.model.*
import com.sanda.truckdoc.client.data.model.route.*

@Database(
    entities = [
        ServerMessage::class,
        AttachmentInfo::class,
        DbLocation::class,
        DbContactRecord::class,
        MessageFileRecord::class,
        DbRoutePoint::class,
        DbRoutePath::class,
        DbRouteAssignment::class,
        MaintenanceFileRecord::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun serverMessageDao(): ServerMessageDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun locationDao(): LocationDao
    abstract fun contactRecordDao(): ContactRecordDao
    abstract fun messageFileDao(): MessageFileDao
    abstract fun routeAssignmentDao(): RouteAssignmentDao
    abstract fun routePointDao(): RoutePointDao
    abstract fun routePathDao(): RoutePathDao
    abstract fun maintenanceFileDao(): MaintenanceFileDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add indices for better query performance
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_server_message_serverMessageId` ON `server_message` (`serverMessageId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_server_message_savedDate` ON `server_message` (`savedDate`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_locations_time` ON `locations` (`time`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_route_assignments_serverAssignmentId` ON `route_assignments` (`serverAssignmentId`)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for future features
                database.execSQL("ALTER TABLE `server_message` ADD COLUMN `priority` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `server_message` ADD COLUMN `read` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `server_message` ADD COLUMN `tags` TEXT")
                
                // Add new columns for route points
                database.execSQL("ALTER TABLE `route_points` ADD COLUMN `estimated_time` INTEGER")
                database.execSQL("ALTER TABLE `route_points` ADD COLUMN `status` TEXT")
                
                // Add new columns for maintenance files
                database.execSQL("ALTER TABLE `maintenance_files` ADD COLUMN `upload_date` INTEGER")
                database.execSQL("ALTER TABLE `maintenance_files` ADD COLUMN `file_type` TEXT")
                
                // Add new indices
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_server_message_priority` ON `server_message` (`priority`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_server_message_read` ON `server_message` (`read`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_route_points_status` ON `route_points` (`status`)")
            }
        }

        val MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
    }
} 