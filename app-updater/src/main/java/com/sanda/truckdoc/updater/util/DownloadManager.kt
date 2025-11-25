package com.sanda.truckdoc.updater.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    private val context: Context,
    private val notificationManager: NotificationManager
) {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    
    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): File = withContext(Dispatchers.IO) {
        try {
            val fileName = "truckdoc-update-${System.currentTimeMillis()}.apk"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "TruckDoc-Updater/1.0")
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw DownloadException("HTTP ${response.code}: ${response.message}")
                }
                
                val contentLength = response.body?.contentLength() ?: -1L
                
                response.body?.byteStream()?.use { input ->
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
                } ?: throw DownloadException("Response body is null")
            }
            
            notificationManager.showDownloadCompleteNotification()
            file
            
        } catch (e: Exception) {
            notificationManager.cancelDownloadProgressNotification()
            throw DownloadException("Failed to download APK: ${e.message}", e)
        }
    }
    
    fun downloadApkWithFlow(downloadUrl: String): Flow<DownloadProgress> = flow {
        try {
            android.util.Log.d("DownloadManager", "Starting download from: $downloadUrl")
            
            val fileName = "truckdoc-update-${System.currentTimeMillis()}.apk"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            android.util.Log.d("DownloadManager", "Saving to: ${file.absolutePath}")
            
            // Use OkHttp for better redirect handling
            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "TruckDoc-Updater/1.0")
                .build()
            
            android.util.Log.d("DownloadManager", "Executing request...")
            
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error details"
                    android.util.Log.e("DownloadManager", "HTTP ${response.code}: $errorBody")
                    throw DownloadException("Download failed: HTTP ${response.code} - ${response.message}")
                }
                
                val contentLength = response.body?.contentLength() ?: -1L
                android.util.Log.d("DownloadManager", "Content length: $contentLength bytes")
                
                response.body?.byteStream()?.use { input ->
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
                                
                                if (totalBytesRead % (1024 * 1024) < 8192) { // Log every MB
                                    android.util.Log.v("DownloadManager", "Progress: $progress% ($message)")
                                }
                                emit(DownloadProgress(progress, message, file))
                            } else {
                                // Unknown content length - emit progress without percentage
                                val message = "${formatFileSize(totalBytesRead)} downloaded"
                                if (totalBytesRead % (1024 * 1024) < 8192) {
                                    emit(DownloadProgress(0, message, file))
                                }
                            }
                        }
                        
                        android.util.Log.d("DownloadManager", "Download complete: $totalBytesRead bytes")
                    }
                } ?: throw DownloadException("Response body is null")
            }
            
            // Verify file exists and has content
            if (!file.exists()) {
                throw DownloadException("Downloaded file doesn't exist")
            }
            
            if (file.length() == 0L) {
                throw DownloadException("Downloaded file is empty")
            }
            
            android.util.Log.d("DownloadManager", "File verified: ${file.length()} bytes at ${file.absolutePath}")
            emit(DownloadProgress(100, "Download complete", file))
            
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("DownloadManager", "Network error: Unknown host", e)
            throw DownloadException("Network error: Cannot reach GitHub. Check internet connection.", e)
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("DownloadManager", "Download timeout", e)
            throw DownloadException("Download timeout. Check internet connection and try again.", e)
        } catch (e: java.io.IOException) {
            android.util.Log.e("DownloadManager", "IO error during download", e)
            throw DownloadException("Download failed: ${e.message}", e)
        } catch (e: DownloadException) {
            android.util.Log.e("DownloadManager", "Download exception: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            android.util.Log.e("DownloadManager", "Unexpected download error", e)
            throw DownloadException("Failed to download APK: ${e.javaClass.simpleName} - ${e.message}", e)
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
 