package app.instructions

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object InstructionsModule {
    @Provides
    @Singleton
    fun provideDb(context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "database-name").allowMainThreadQueries().build()
    }

    @Provides
    @Singleton
    fun provideDao(db: AppDatabase) = db.instructionDao()
}

interface InstructionsActivityInjector {
    fun inject(act: InstructionsActivity)
}

interface InstructionsActivityInjectorProvider {
    fun appComponent(): InstructionsActivityInjector
}
