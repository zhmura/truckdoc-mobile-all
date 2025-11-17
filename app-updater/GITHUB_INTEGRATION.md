# GitHub Releases Integration

## Overview

The app-updater now checks for updates from GitHub Releases instead of Jenkins. It monitors updates for **both**:
1. **TruckDoc Client App** (`com.sanda.truckdoc.client.default`)
2. **TruckDoc Updater App** itself (`com.sanda.truckdoc.updater`)

## Configuration

### GitHubConfig.kt
- **Repository**: `zhmura/truckdoc-mobile-all`
- **GitHub API Base URL**: `https://api.github.com/`
- **Client APK Pattern**: `truckdoc-client-v`
- **Updater APK Pattern**: `truckdoc-updater-v`

### Expected APK Naming in Releases
Based on our Jenkins build configuration:
- Main app: `truckdoc-client-v{version}-defaultClient.apk` (e.g., `truckdoc-client-v1.0-defaultClient.apk`)
- Updater: `truckdoc-updater-v{version}.apk` (e.g., `truckdoc-updater-v1.0.apk`)

## Implementation

### New Components Created

1. **GitHubConfig.kt** - Configuration for GitHub API and target apps
2. **GitHubRelease.kt** - Data models for GitHub API responses
3. **GitHubApiService.kt** - Retrofit interface for GitHub API
4. **GitHubUpdateRepository.kt** - Repository that checks both apps for updates
5. **NetworkModule.kt** - Updated to provide both GitHub and Jenkins API services

### Key Features

#### Dual App Monitoring
```kotlin
data class SystemUpdateInfo(
    val clientAppUpdate: AppUpdateInfo,
    val updaterAppUpdate: AppUpdateInfo,
    val lastCheckTime: Long
) {
    fun hasAnyUpdate(): Boolean = 
        clientAppUpdate.updateAvailable || updaterAppUpdate.updateAvailable
}
```

#### Version Comparison
- Compares version codes first
- Falls back to semantic version name comparison (1.2.3 vs 1.2.2)
- Handles apps not installed gracefully

#### APK Pattern Matching
- Finds APKs in release assets by matching filename patterns
- Extracts version from filename using regex
- Gets download URL from GitHub asset

## Integration TODO

To complete the integration, the following changes are needed:

### 1. Update MainViewModel
Replace `UpdateRepository` injection with `GitHubUpdateRepository`:
```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateRepository: GitHubUpdateRepository,  // Changed
    // ... other dependencies
) : ViewModel()
```

Update `checkForUpdates()` to handle `SystemUpdateInfo`:
```kotlin
fun checkForUpdates() {
    viewModelScope.launch {
        try {
            _uiState.value = UiState.Loading
            
            val systemUpdate = updateRepository.checkForUpdates()
            
            // Check if client app is installed
            if (!updateRepository.isAppInstalled(GitHubConfig.TargetApps.CLIENT_PACKAGE_NAME)) {
                _uiState.value = UiState.Error("TruckDoc client app is not installed")
                return@launch
            }
            
            if (systemUpdate.hasAnyUpdate()) {
                _uiState.value = UiState.UpdateAvailable(systemUpdate)
                // Show notification for available updates
            } else {
                _uiState.value = UiState.NoUpdateAvailable(systemUpdate)
            }
            
        } catch (e: UpdateException) {
            _uiState.value = UiState.Error(e.message ?: "Update check failed")
        }
    }
}
```

### 2. Update UI to Display Both Apps
The UI should show update status for both apps:
- Client App: version, update available status
- Updater App: version, self-update available status

### 3. Update Download Logic
Handle downloading and installing updates for both apps:
- Client app updates: download and prompt install
- Updater self-updates: download, prompt install (will restart updater)

### 4. Update Services and Workers
Update `UpdateCheckService` and `UpdateCheckWorker` to use `GitHubUpdateRepository`.

## API Usage

### Check for Updates
```kotlin
val systemUpdate = gitHubUpdateRepository.checkForUpdates()

// Client app update
if (systemUpdate.clientAppUpdate.updateAvailable) {
    val newVersion = systemUpdate.clientAppUpdate.latestVersion
    val downloadUrl = newVersion?.downloadUrl
    // Download and install
}

// Updater self-update
if (systemUpdate.updaterAppUpdate.updateAvailable) {
    val newVersion = systemUpdate.updaterAppUpdate.latestVersion
    val downloadUrl = newVersion?.downloadUrl
    // Download and install (will restart updater)
}
```

### GitHub API Rate Limits
- **Unauthenticated**: 60 requests/hour per IP
- **Authenticated**: 5000 requests/hour per token

Current implementation uses unauthenticated requests. For production, consider adding GitHub token authentication if rate limits become an issue.

## Testing

1. **Build and install the updater APK**
2. **Create a GitHub release** with both APKs:
   - Tag: `v1.0.0`
   - Assets: `truckdoc-client-v1.0-defaultClient.apk`, `truckdoc-updater-v1.0.apk`
3. **Run the updater app**
4. **Verify it detects updates for both apps**

## Migration from Jenkins

The old Jenkins-based update checking is still available via `UpdateRepository` and `JenkinsApiService`. Once GitHub integration is fully tested and working, the Jenkins code can be deprecated or removed.

## Security Considerations

- APKs are downloaded over HTTPS from GitHub
- Verify APK signature before installation
- Consider adding APK checksum verification against release assets
- GitHub API communication is over HTTPS

