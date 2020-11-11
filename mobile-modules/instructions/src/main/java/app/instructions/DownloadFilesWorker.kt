package app.instructions

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.sanda.truckdoc.client.api.v3.sync.instructions.model.InstructionSetWithVersion
import com.sanda.truckdoc.network.AuthorizedBackend
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownloadFilesWorker(appContext: Context, workerParams: WorkerParameters)
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
        (appContext as InstructionsInjectorProvider).appComponent().inject(this)
    }

    override fun doWork(): Result {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            }
            val downloadedFiles = mutableListOf<String>()
            rootFileDir.mkdirs()
            val pending = dao.findPending()
            pending.forEach {
                Log.i("DownloadFilesWorker", "Downloading file $it")
                val response = api.downloadFile(it.file!!.fileId).execute()
                if (response.isSuccessful) {
                    showProgressNotification(it.file.fileName)
                    response.body().byteStream().saveToFile(it.file.fileName)
                    dao.update(it.copy(file = it.file.copy(timestamp = it.file.pending!!, pending = null)))
                    downloadedFiles.add(it.file.fileName)
                } else {
                    //most probably it is server error like 4xx
                    Log.e("DownloadFilesWorker", response.errorBody().string())
                    return Result.failure()
                }
            }
            showNotification(downloadedFiles)
        } catch (e: Exception) {
            Log.e("DownloadFilesWorker", "Error downloading", e)
            //connection reset, socket timeout, etc
            return if (e is IOException)
                Result.retry()
            else
                Result.failure()
        } finally {
            NotificationManagerCompat.from(applicationContext).cancel(452)
        }

        // Indicate whether the task finished successfully with the Result
        return Result.success()
    }

    private fun showNotification(files: List<String>) {
        val notification = NotificationCompat.Builder(applicationContext, "instructions_channel").apply {
            if (files.isNotEmpty()) {
                setContentTitle(applicationContext.resources.getQuantityString(R.plurals.instructions_files_downloaded, files.size, files.size))
                setContentText(files.joinToString())
                setStyle(InboxStyle().also { style ->
                    style.setBigContentTitle(applicationContext.resources.getQuantityString(R.plurals.instructions_files_downloaded, files.size))
                    files.forEach { style.addLine(it) }
                })
            } else {
                setContentTitle(applicationContext.getString(R.string.instructions_no_files_downloaded))
            }
            priority = PRIORITY_LOW
            setCategory(CATEGORY_EVENT)
            setSmallIcon(R.drawable.ic_baseline_cloud_done_24)
        }
        NotificationManagerCompat.from(applicationContext).notify(451, notification.build())
    }

    private fun showProgressNotification(file: String) {
        val notification = NotificationCompat.Builder(applicationContext, "instructions_channel").apply {
            setContentTitle(applicationContext.getString(R.string.instructions_downloading))
            setContentText(file)
            priority = PRIORITY_DEFAULT
            setCategory(CATEGORY_EVENT)
            setSmallIcon(R.drawable.ic_baseline_cloud_download_24)
        }
        NotificationManagerCompat.from(applicationContext).notify(452, notification.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel("instructions_channel", "instructions_channel", NotificationManagerCompat.IMPORTANCE_LOW)
        NotificationManagerCompat.from(applicationContext).createNotificationChannel(channel)
    }

    private fun InputStream.saveToFile(file: String) = use { input ->
        File(rootFileDir, file).outputStream().use { output ->
            input.copyTo(output)
        }
    }
}




