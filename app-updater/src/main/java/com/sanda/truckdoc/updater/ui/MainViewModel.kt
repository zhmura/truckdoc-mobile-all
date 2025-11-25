package com.sanda.truckdoc.updater.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanda.truckdoc.updater.config.GitHubConfig
import com.sanda.truckdoc.updater.data.model.AppUpdateInfo
import com.sanda.truckdoc.updater.data.model.SystemUpdateInfo
import com.sanda.truckdoc.updater.data.repository.GitHubUpdateRepository
import com.sanda.truckdoc.updater.data.repository.UpdateException
import com.sanda.truckdoc.updater.util.DownloadManager
import com.sanda.truckdoc.updater.util.NotificationManager
import com.sanda.truckdoc.updater.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

import com.sanda.truckdoc.updater.data.repository.UpdateProvider

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateRepository: UpdateProvider,
    private val downloadManager: DownloadManager,
    private val notificationManager: NotificationManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState
    
    private val _systemUpdateInfo = MutableLiveData<SystemUpdateInfo>()
    val systemUpdateInfo: LiveData<SystemUpdateInfo> = _systemUpdateInfo
    
    private val _downloadProgress = MutableLiveData<DownloadProgress>()
    val downloadProgress: LiveData<DownloadProgress> = _downloadProgress
    
    private var currentDownloadTarget: DownloadTarget = DownloadTarget.CLIENT_APP
    
    init {
        checkForUpdates()
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val systemUpdate = updateRepository.checkForUpdates()
                _systemUpdateInfo.value = systemUpdate
                
                // Check if client app is installed
                val isClientInstalled = updateRepository.isAppInstalled(GitHubConfig.TargetApps.CLIENT_PACKAGE_NAME)
                
                if (!isClientInstalled) {
                    // Client not installed - offer to download latest version
                    _uiState.value = UiState.ClientNotInstalled(systemUpdate)
                    preferencesManager.updateLastCheckTime()
                    return@launch
                }
                
                // Update preferences with client app version
                preferencesManager.updateLastCheckTime()
                preferencesManager.lastKnownVersion = systemUpdate.clientAppUpdate.currentVersion.versionName
                preferencesManager.lastKnownVersionCode = systemUpdate.clientAppUpdate.currentVersion.versionCode
                
                if (systemUpdate.hasAnyUpdate()) {
                    _uiState.value = UiState.UpdateAvailable(systemUpdate)
                    
                    // Show notification if enabled
                    if (preferencesManager.isNotificationsEnabled) {
                        // Show notification about available updates
                        // notificationManager.showUpdateAvailableNotification(systemUpdate)
                    }
                    
                    // Auto-download client app if enabled
                    if (preferencesManager.isAutoDownloadEnabled && systemUpdate.clientAppUpdate.updateAvailable) {
                        downloadUpdate(DownloadTarget.CLIENT_APP)
                    }
                } else {
                    _uiState.value = UiState.NoUpdateAvailable(systemUpdate)
                }
                
            } catch (e: UpdateException) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to check for updates: ${e.message}")
            }
        }
    }
    
    fun downloadUpdate(target: DownloadTarget) {
        val systemUpdate = _systemUpdateInfo.value
        
        val appUpdate = when (target) {
            DownloadTarget.CLIENT_APP -> systemUpdate?.clientAppUpdate
            DownloadTarget.UPDATER_APP -> systemUpdate?.updaterAppUpdate
        }
        
        val latestVersion = appUpdate?.latestVersion
        
        if (latestVersion == null || latestVersion.downloadUrl.isEmpty()) {
            _uiState.value = UiState.Error("No update available to download for ${target.displayName}")
            return
        }
        
        currentDownloadTarget = target
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    _uiState.value = UiState.Downloading(target)
                }
                
                downloadManager.downloadApkWithFlow(latestVersion.downloadUrl)
                    .collectLatest { progress ->
                        withContext(Dispatchers.Main) {
                            _downloadProgress.value = DownloadProgress(
                                progress.progress, 
                                "${target.displayName}: ${progress.message}"
                            )
                            
                            if (progress.progress == 100 && progress.file != null) {
                                _uiState.value = UiState.DownloadComplete(progress.file, target)
                                if (target == DownloadTarget.CLIENT_APP) {
                                    preferencesManager.updateLastUpdateTime()
                                }
                            }
                        }
                    }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = UiState.Error("Failed to download ${target.displayName}: ${e.message}")
                }
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
    data class ClientNotInstalled(val systemUpdate: SystemUpdateInfo) : UiState()
    data class NoUpdateAvailable(val systemUpdate: SystemUpdateInfo) : UiState()
    data class UpdateAvailable(val systemUpdate: SystemUpdateInfo) : UiState()
    data class Downloading(val target: DownloadTarget) : UiState()
    data class DownloadComplete(val file: File, val target: DownloadTarget) : UiState()
    object Installing : UiState()
    data class Error(val message: String) : UiState()
}

enum class DownloadTarget(val displayName: String) {
    CLIENT_APP("TruckDoc Client"),
    UPDATER_APP("App Updater")
}

data class DownloadProgress(
    val progress: Int,
    val message: String
) 