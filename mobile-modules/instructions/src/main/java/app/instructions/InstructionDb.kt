package app.instructions

import androidx.lifecycle.LiveData
import androidx.room.*
import java.io.Serializable

data class FileDesc(
        val fileId: Long,
        val fileName: String,
        val mimeType: String?,
        val timestamp: Long,
        var pending: Long?
)

@Entity
data class InstructionDb(
        @PrimaryKey val id: String,
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

    @Query("SELECT * FROM InstructionDb WHERE parentId IS NULL")
    fun findRoot(): LiveData<List<InstructionDb>>

    @Query("SELECT * FROM InstructionDb WHERE parentId = :parentId")
    fun findEntries(parentId: String): LiveData<List<InstructionDb>>

    @Query("DELETE FROM InstructionDb where id NOT IN(:ids)")
    fun removeMissingNodes(ids: List<String>)

    @Query("SELECT * FROM InstructionDb where id = :id")
    fun find(id: String): InstructionDb?
}

@Database(entities = [InstructionDb::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun instructionDao(): InstructionsDao
}
