package com.sanda.truckdoc.updater.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanda.truckdoc.updater.data.model.AppVersion
import com.sanda.truckdoc.updater.data.model.UpdateInfo
import com.sanda.truckdoc.updater.data.repository.UpdateException
import com.sanda.truckdoc.updater.data.repository.UpdateRepository
import com.sanda.truckdoc.updater.util.DownloadManager
import com.sanda.truckdoc.updater.util.NotificationManager
import com.sanda.truckdoc.updater.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val downloadManager: DownloadManager,
    private val notificationManager: NotificationManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    
    private val _updateInfo = MutableLiveData<UpdateInfo>()
    val updateInfo: LiveData<UpdateInfo> = _updateInfo
    
    private val _downloadProgress = MutableLiveData<DownloadProgress>()
    val downloadProgress: LiveData<DownloadProgress> = _downloadProgress
    
    init {
        checkForUpdates()
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                if (!updateRepository.isAppInstalled()) {
                    _uiState.value = UiState.Error("Target app is not installed")
                    return@launch
                }
                
                val updateInfo = updateRepository.checkForUpdates()
                _updateInfo.value = updateInfo
                
                // Update preferences
                preferencesManager.updateLastCheckTime()
                preferencesManager.lastKnownVersion = updateInfo.currentVersion.versionName
                preferencesManager.lastKnownVersionCode = updateInfo.currentVersion.versionCode
                
                if (updateInfo.updateAvailable) {
                    _uiState.value = UiState.UpdateAvailable(updateInfo)
                    
                    // Show notification if enabled
                    if (preferencesManager.isNotificationsEnabled) {
                        notificationManager.showUpdateAvailableNotification(updateInfo)
                    }
                    
                    // Auto-download if enabled
                    if (preferencesManager.isAutoDownloadEnabled) {
                        downloadUpdate()
                    }
                } else {
                    _uiState.value = UiState.NoUpdateAvailable(updateInfo)
                }
                
            } catch (e: UpdateException) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to check for updates: ${e.message}")
            }
        }
    }
    
    fun downloadUpdate() {
        val updateInfo = _updateInfo.value
        val latestVersion = updateInfo?.latestVersion
        
        if (latestVersion == null) {
            _uiState.value = UiState.Error("No update available to download")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Downloading
                
                downloadManager.downloadApkWithFlow(latestVersion.downloadUrl)
                    .collectLatest { progress ->
                        _downloadProgress.value = DownloadProgress(progress.progress, progress.message)
                        
                        if (progress.progress == 100 && progress.file != null) {
                            _uiState.value = UiState.DownloadComplete(progress.file)
                            preferencesManager.updateLastUpdateTime()
                        }
                    }
                
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to download update: ${e.message}")
            }
        }
    }
    
    fun installUpdate(file: File) {
        _uiState.value = UiState.Installing
        // The actual installation is handled by MainActivity
    }
    
    fun getFormattedLastCheckTime(timestamp: Long): String {
        if (timestamp == 0L) return "Never"
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    fun cleanupOldDownloads() {
        downloadManager.cleanupOldDownloads()
    }
    
    fun getPreferencesManager(): PreferencesManager = preferencesManager
}

sealed class UiState {
    object Loading : UiState()
    data class NoUpdateAvailable(val updateInfo: UpdateInfo) : UiState()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UiState()
    object Downloading : UiState()
    data class DownloadComplete(val file: File) : UiState()
    object Installing : UiState()
    data class Error(val message: String) : UiState()
}

data class DownloadProgress(
    val progress: Int,
    val message: String
) 