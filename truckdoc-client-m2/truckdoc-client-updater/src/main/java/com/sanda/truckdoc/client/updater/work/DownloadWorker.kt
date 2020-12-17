package com.sanda.truckdoc.client.updater.work

import android.content.Context
import android.net.ConnectivityManager
import androidx.work.*
import com.sanda.truckdoc.client.updater.Prefs
import com.sanda.truckdoc.client.updater.R
import com.sanda.truckdoc.client.updater.UpdaterApp
import com.sanda.truckdoc.client.updater.network.Backend
import com.sanda.truckdoc.client.updater.utils.L
import com.sanda.truckdoc.client.updater.utils.NotificationHelper
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownloadWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

    companion object {
        @JvmStatic
        fun start(c: Context, url: String) {

            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val uploadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf("url" to url))
                    .setInitialDelay(500, TimeUnit.MICROSECONDS)
/*                  TODO optimize this if you want, use defaults for now
                    .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS)*/
                    .build()
            WorkManager.getInstance(c)
                    .beginUniqueWork("download", ExistingWorkPolicy.REPLACE, uploadWorkRequest)
                    .then(CheckInstallWorker.create())
                    .enqueue()
        }
    }

    @Inject
    lateinit var api: Backend

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var notificationHelper: NotificationHelper

    init {
        UpdaterApp.get(appContext).appComponent().inject(this)
    }

    override suspend fun doWork(): Result {
        return try {
            if (prefs.useWiFi() && !hasWifi())
                return Result.retry()

            val url = inputData.getString("url")

            val execute = api.downloadFile(url)
            //we can show info later
//            setForegroundAsync(ForegroundInfo())
            L.i("Starting download $url")
            val (file, size) = execute.byteStream().saveToFile("file.apk")
            L.d("Bytes written: $size")
            notificationHelper.notifyAppDownloaded(file.absolutePath)
            Result.success(workDataOf("apk" to file.absolutePath))

        } catch (e: Exception) {
            L.e(e, "Error downloading")
            notificationHelper.toast(applicationContext.getString(R.string.download_error, e.message))

            //connection reset, socket timeout, etc
            return if (e is IOException)
                Result.retry()
            else
                Result.failure()
        } finally {
        }
    }

    private fun hasWifi(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.isConnectedOrConnecting ?: false
    }

    private fun InputStream.saveToFile(file: String) = use { input ->
        File(applicationContext.filesDir, file).let {
            it to it.outputStream().use { output ->
                input.copyTo(output)
            }
        }

    }
}

