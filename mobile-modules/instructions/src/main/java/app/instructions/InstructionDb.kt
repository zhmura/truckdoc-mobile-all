package app.instructions

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetNode
import java.io.Serializable

data class FileDesc(
        val fileId: Long,
        val fileName: String,
        val mimeType: String?,
        val timestamp: Long,
        var pending: Long?
)

@Entity(tableName = "instructions")
data class InstructionDb(
        @PrimaryKey val id: String,
        val type: InstructionSetNode.Type,
        val icon: String?,
        val displayName: String,

        val parentId: String? = null,
        @Embedded val file: FileDesc?
) : Serializable

@Dao
interface InstructionsDao {

    @Insert
    fun insertAll(value: List<InstructionDb>)

    @Insert
    fun insert(value: InstructionDb)

    @Update
    fun update(value: InstructionDb)

    @Query("SELECT * FROM instructions WHERE parentId IS NULL")
    fun findRoot(): LiveData<List<InstructionDb>>

    @Query("SELECT * FROM instructions WHERE parentId = :parentId")
    fun findEntries(parentId: String): LiveData<List<InstructionDb>>

    @Query("DELETE FROM instructions where id NOT IN(:ids)")
    fun removeMissingNodes(ids: List<String>)

    @Query("SELECT * FROM instructions where id = :id")
    fun find(id: String): InstructionDb?

    @Query("SELECT * FROM instructions WHERE pending IS NOT NULL")
    fun findPending(): List<InstructionDb>
}

@TypeConverters(Converters::class)
@Database(entities = [InstructionDb::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instructionDao(): InstructionsDao
}

class Converters {
    @TypeConverter
    fun fromStringToType(s: String) = InstructionSetNode.Type.valueOf(s)

    @TypeConverter
    fun fromTypeToString(e: InstructionSetNode.Type) = e.name
}
