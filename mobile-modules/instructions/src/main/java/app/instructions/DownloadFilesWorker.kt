package app.instructions

import android.content.Context
import android.util.Log
import androidx.work.*
import com.sanda.truckdoc.network.AuthorizedBackend
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class DownloadFilesWorker(private val appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    companion object {
        @JvmStatic
        fun start(c: Context) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val uploadWorkRequest = OneTimeWorkRequestBuilder<DownloadFilesWorker>()
                    .setConstraints(constraints)
/*                  TODO optimize this if you want, use defaults for now
                    .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS)*/
                    .build()
            WorkManager.getInstance(c).enqueue(uploadWorkRequest)
        }
    }

    @Inject
    lateinit var dao: InstructionsDao

    @Inject
    lateinit var api: AuthorizedBackend

    @Inject
    lateinit var rootFileDir: File

    init {
        (appContext.applicationContext as InstructionsInjectorProvider).appComponent().inject(this)
    }

    override fun doWork(): Result {
        try {
            val pending = dao.findPending()
            pending.forEach {
                api.downloadFile(it.file!!.fileName).execute().body().byteStream().saveToFile(it.file.fileName)
                dao.update(it.copy(file = it.file.copy(timestamp = it.file.timestamp, pending = null)))
            }
        } catch (e: Exception) {
            Log.e("DownloadFilesWorker", "Error downloading", e)
            return Result.retry()
        }

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun InputStream.saveToFile(file: String) = use { input ->
        File(rootFileDir, file).outputStream().use { output ->
            input.copyTo(output)
        }
    }
}




