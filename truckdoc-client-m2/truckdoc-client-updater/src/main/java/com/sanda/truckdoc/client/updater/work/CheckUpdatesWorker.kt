package com.sanda.truckdoc.client.updater.work

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.sanda.truckdoc.client.updater.Prefs
import com.sanda.truckdoc.client.updater.R
import com.sanda.truckdoc.client.updater.UpdaterApp
import com.sanda.truckdoc.client.updater.network.Backend
import com.sanda.truckdoc.client.updater.receivers.ReceiverConfig
import com.sanda.truckdoc.client.updater.utils.DeviceHelper
import com.sanda.truckdoc.client.updater.utils.L
import com.sanda.truckdoc.client.updater.utils.NotificationHelper
import com.sanda.truckdoc.client.updater.utils.PropertiesHelper
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CheckUpdatesWorker(appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(appContext, workerParams) {

    companion object {
        @JvmStatic
        fun start(c: Context) {
            val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val uploadWorkRequest = PeriodicWorkRequestBuilder<CheckUpdatesWorker>(ReceiverConfig.MIN_INTERVAL_BETWEEN_UPDATE_CHECK, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
/*                  TODO optimize this if you want, use defaults for now
                    .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS)*/
                    .build()

            WorkManager.getInstance(c).enqueueUniquePeriodicWork("checkUpdates", ExistingPeriodicWorkPolicy.KEEP, uploadWorkRequest)
        }
    }

    @Inject
    lateinit var api: Backend

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var prefs: Prefs

    init {
        UpdaterApp.get(appContext).appComponent().inject(this)
        if (StringUtils.isEmpty(prefs.targetPackage())) {
            prefs.targetPackage(PropertiesHelper().targetPackageForUpdate)
        }
    }

    override suspend fun doWork(): Result {
        try {

            if (prefs.targetPackage().isEmpty()) {
//                Toast.makeText(applicationContext, "Target package not set", Toast.LENGTH_SHORT).show()
                L.e("Target package not set for updater. Skipping update request...")
                return Result.failure(workDataOf("msg" to "target package is empty"))
            }
            val ids = DeviceHelper.getDeviceId(applicationContext)
            val updateResponse = api.checkUpdates(prefs.targetPackage(),
                    "1",
                    //TODO DeviceHelper.findVersionName(prefs.targetPackage(), applicationContext),
                    Build.VERSION.SDK_INT,
                    20,
                    //TODO DeviceHelper.findVersionCode(prefs.targetPackage(), applicationContext),
                    DeviceHelper.findUpdaterVersionCode(applicationContext),
                    ids.deviceId,
                    ids.androidId,
                    ids.deviceUuid)
            L.i(updateResponse)

            DownloadWorker.start(applicationContext, "http://tut.by")
            if (updateResponse.updateAvailable) {
                DownloadWorker.start(applicationContext, updateResponse.url!!)
            }

//            showNotification(downloadedFiles)
        } catch (e: Exception) {
            L.e(e, "Error downloading")
            notificationHelper.toast(applicationContext.getString(R.string.download_error, e.message))

            //connection reset, socket timeout, etc
            return if (e is IOException)
                Result.retry()
            else
                Result.failure(workDataOf("msg" to e.toString()))
        } finally {
            NotificationManagerCompat.from(applicationContext).cancel(452)
        }

        return Result.success()
    }
}

