package app.instructions

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module
object InstructionsModule {
    @Provides
    @Singleton
    fun provideDb(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "instructions.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

    @Provides
    @Singleton
    fun provideDao(db: AppDatabase) = db.instructionDao()

    @Provides
    @Singleton
    fun instructionsRoot(c: Context) = File(c.filesDir, "instructions")
}

interface InstructionsInjector {
    fun inject(act: InstructionsActivity)
    fun inject(act: DownloadFilesWorker)
}

interface InstructionsInjectorProvider {
    fun appComponent(): InstructionsInjector
}
