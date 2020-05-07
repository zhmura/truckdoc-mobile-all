package app.instructions

import android.content.Context
import android.util.Log
import androidx.work.*
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetWithVersion
import com.sanda.truckdoc.network.AuthorizedBackend
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownloadFilesWorker(private val appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams) {

    companion object {
        @JvmStatic
        fun start(c: Context, set: InstructionSetWithVersion? = null) {
            WorkManager.getInstance(c).cancelAllWork()
            set?.let {
                processConfig(c, set)
            }

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val uploadWorkRequest = OneTimeWorkRequestBuilder<DownloadFilesWorker>()
                    .setConstraints(constraints)
                    .setInitialDelay(500, TimeUnit.MICROSECONDS)
/*                  TODO optimize this if you want, use defaults for now
                    .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS)*/
                    .build()
            WorkManager.getInstance(c).enqueue(uploadWorkRequest)
        }

        private fun processConfig(c: Context, set: InstructionSetWithVersion) {
            (c.applicationContext as InstructionsInjectorProvider).appComponent().helper.processIncomingSet(set)
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
            rootFileDir.mkdirs()
            val pending = dao.findPending()
            pending.forEach {
                Log.i("DownloadFilesWorker", "Downloading file $it")
                val response = api.downloadFile(it.file!!.fileId).execute()
                if (response.isSuccessful) {
                    response.body().byteStream().saveToFile(it.file.fileName)
                    dao.update(it.copy(file = it.file.copy(timestamp = it.file.timestamp, pending = null)))
                } else {
                    //most probably it is server error like 4xx
                    Log.e("DownloadFilesWorker", response.errorBody().string())
                    return Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e("DownloadFilesWorker", "Error downloading", e)
            //connection reset, socket timeout, etc
            return if (e is IOException)
                Result.retry()
            else
                Result.failure()
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




