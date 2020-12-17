package com.sanda.truckdoc.client.updater.work

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.*
import com.sanda.truckdoc.client.updater.Prefs
import com.sanda.truckdoc.client.updater.R
import com.sanda.truckdoc.client.updater.UpdaterApp
import com.sanda.truckdoc.client.updater.utils.DeviceHelper
import com.sanda.truckdoc.client.updater.utils.L
import com.sanda.truckdoc.client.updater.utils.NotificationHelper
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CheckInstallWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

    companion object {
        @JvmStatic
        fun create(apk: String? = null): OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<CheckInstallWorker>()
                        .apply {
                            if (apk != null)
                                setInputData(workDataOf("apk" to apk))
                        }
//
/*                  TODO optimize this if you want, use defaults for now
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS)*/
                        .build()
    }

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var notificationHelper: NotificationHelper

    init {
        UpdaterApp.get(appContext).appComponent().inject(this)
    }

    override suspend fun doWork(): Result = try {
        val apk = inputData.getString("apk")
        L.d("Check install $apk")
        val fileVersionCode = DeviceHelper.getFileVersionCode(applicationContext, apk)
        val targetVersionCode = DeviceHelper.findVersionCode(prefs.targetPackage(), applicationContext)
        val file = File(apk)
        if (fileVersionCode > targetVersionCode) {
            Handler(Looper.getMainLooper()).post {
                DeviceHelper.installFile(applicationContext, file)
            }
            Result.retry()
        } else {
            alreadyInstalled(file)
            Result.success()
        }
    } catch (e: Exception) {
        L.e(e)
        notificationHelper.toast(applicationContext.getString(R.string.install_error, e.message))
        Result.failure()
    } finally {

    }

    private fun alreadyInstalled(file: File) {
        Timber.i("File already installed " + file.name)
        notificationHelper.hide()
        FileUtils.deleteQuietly(file)
    }
}

