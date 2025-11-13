package com.sanda.truckdoc.updater.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager
) {
    
    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): File = withContext(Dispatchers.IO) {
        try {
            val fileName = "truckdoc-update-${System.currentTimeMillis()}.apk"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val connection = URL(downloadUrl).openConnection() as URLConnection
            val contentLength = connection.contentLengthLong
            
            connection.connectTimeout = 30000 // 30 seconds
            connection.readTimeout = 60000 // 60 seconds
            
            connection.getInputStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        if (contentLength > 0) {
                            val progress = ((totalBytesRead * 100) / contentLength).toInt()
                            val message = "${formatFileSize(totalBytesRead)} / ${formatFileSize(contentLength)}"
                            
                            onProgress(progress, message)
                            notificationManager.showDownloadProgressNotification(progress, message)
                        }
                    }
                }
            }
            
            notificationManager.showDownloadCompleteNotification()
            file
            
        } catch (e: Exception) {
            notificationManager.cancelDownloadProgressNotification()
            throw DownloadException("Failed to download APK", e)
        }
    }
    
    fun downloadApkWithFlow(downloadUrl: String): Flow<DownloadProgress> = flow {
        try {
            val fileName = "truckdoc-update-${System.currentTimeMillis()}.apk"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val connection = URL(downloadUrl).openConnection() as URLConnection
            val contentLength = connection.contentLengthLong
            
            connection.connectTimeout = 30000
            connection.readTimeout = 60000
            
            connection.getInputStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        if (contentLength > 0) {
                            val progress = ((totalBytesRead * 100) / contentLength).toInt()
                            val message = "${formatFileSize(totalBytesRead)} / ${formatFileSize(contentLength)}"
                            
                            emit(DownloadProgress(progress, message, file))
                        }
                    }
                }
            }
            
            emit(DownloadProgress(100, "Download complete", file))
            
        } catch (e: Exception) {
            throw DownloadException("Failed to download APK", e)
        }
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
    
    fun cleanupOldDownloads() {
        val downloadDir = context.getExternalFilesDir(null)
        downloadDir?.listFiles()?.forEach { file ->
            if (file.name.startsWith("truckdoc-update-") && file.name.endsWith(".apk")) {
                // Delete files older than 7 days
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                if (file.lastModified() < sevenDaysAgo) {
                    file.delete()
                }
            }
        }
    }
}

data class DownloadProgress(
    val progress: Int,
    val message: String,
    val file: File? = null
)

class DownloadException(message: String, cause: Throwable? = null) : Exception(message, cause) 
 