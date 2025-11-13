# TruckDoc App Updater

## Overview

The TruckDoc App Updater is a standalone Android application module that provides automatic update functionality for the main TruckDoc application. This module handles checking for updates, downloading new versions, and managing the update installation process.

## Features

### Core Functionality
- **Update Detection**: Automatically check for new application versions
- **Download Management**: Download APK files with progress tracking
- **Installation Handling**: Manage the installation process for updates
- **Background Updates**: Check for updates in the background
- **User Notifications**: Inform users about available updates
- **Update Scheduling**: Configurable update check intervals

### Advanced Features
- **Delta Updates**: Support for incremental updates (planned)
- **Rollback Support**: Ability to revert to previous versions
- **Update Verification**: Checksum validation for downloaded files
- **Network Optimization**: Efficient download with resume capability
- **Battery Optimization**: Respects device battery optimization settings

## Architecture

### Technology Stack
- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Networking**: Retrofit with OkHttp
- **File Management**: Android FileProvider
- **Notifications**: Android Notification API
- **Background Processing**: WorkManager for scheduled tasks

### Key Components

#### Data Layer
- **UpdateRepository**: Manages update checking and downloading
- **PreferencesManager**: Stores update preferences and settings
- **DownloadManager**: Handles file downloads with progress tracking

#### Domain Layer
- **UpdateInfo**: Data models for update information
- **UpdateService**: Business logic for update operations
- **UpdateChecker**: Core update detection logic

#### Presentation Layer
- **MainActivity**: Main UI for update management
- **SettingsActivity**: Configuration interface
- **UpdateCheckService**: Background service for update checking

## Project Structure

```
src/main/java/com/sanda/truckdoc/updater/
├── data/                    # Data layer
│   ├── model/              # Data models
│   ├── repository/         # Repository implementations
│   └── local/              # Local data storage
├── di/                     # Dependency injection
│   └── modules/            # Hilt modules
├── domain/                 # Domain layer
│   ├── model/              # Domain models
│   └── repository/         # Repository interfaces
├── ui/                     # Presentation layer
│   ├── main/               # Main activity
│   └── settings/           # Settings activity
├── service/                # Background services
├── util/                   # Utility classes
└── config/                 # Configuration classes
```

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Build Configuration
The updater supports multiple build variants:
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

### Dependencies
Key dependencies include:
- **Hilt**: Dependency injection
- **Retrofit**: Network communication
- **WorkManager**: Background task scheduling
- **Material Design**: UI components
- **JUnit & Mockito**: Testing

## Configuration

### Update Server Configuration
The updater connects to a Jenkins server for update information. Configuration is handled in `JenkinsConfig.kt`:

```kotlin
object JenkinsConfig {
    const val BASE_URL = "https://your-jenkins-server.com"
    const val USERNAME = "jenkins-user"
    const val API_TOKEN = "your-api-token"
}
```

### Update Check Intervals
Update check intervals can be configured in the settings:
- **Manual**: User-initiated checks only
- **Daily**: Check once per day
- **Weekly**: Check once per week
- **Custom**: User-defined interval

### Notification Settings
Users can configure:
- **Enable/Disable**: Toggle update notifications
- **Sound**: Enable/disable notification sounds
- **Vibration**: Enable/disable vibration
- **Priority**: Set notification priority level

## API Integration

### Update Check Endpoint
```
GET /api/updates/latest
```

### Response Format
```json
{
  "currentVersion": {
    "versionName": "1.0.0",
    "versionCode": 1
  },
  "latestVersion": {
    "versionName": "1.1.0",
    "versionCode": 2,
    "downloadUrl": "https://example.com/app-v1.1.0.apk",
    "releaseNotes": "Bug fixes and improvements"
  },
  "updateAvailable": true,
  "lastCheckTime": "2024-01-15T10:30:00Z"
}
```

## Background Services

### UpdateCheckService
- **Purpose**: Background update checking
- **Trigger**: Scheduled via WorkManager
- **Features**: 
  - Respects battery optimization
  - Network-aware checking
  - User preference compliance

### DownloadService
- **Purpose**: Handle APK downloads
- **Features**:
  - Progress tracking
  - Resume capability
  - Network interruption handling
  - File integrity verification

## User Interface

### Main Activity
- **Update Status**: Current version and latest available
- **Check for Updates**: Manual update check button
- **Download Progress**: Visual progress indicator
- **Install Button**: Trigger installation when ready

### Settings Activity
- **Update Frequency**: Configure check intervals
- **Notification Settings**: Customize notifications
- **Network Settings**: Wi-Fi only downloads
- **Storage Settings**: Download location preferences

## Testing

### Unit Tests
- **Location**: `src/test/java/`
- **Coverage**: Business logic and data layer
- **Frameworks**: JUnit 4, Mockito

### Instrumented Tests
- **Location**: `src/androidTest/java/`
- **Coverage**: UI components and integration
- **Frameworks**: Espresso, AndroidJUnit4

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test variant
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Build Process

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### APK Generation
```bash
# Generate debug APK
./gradlew assembleDebug

# Generate release APK
./gradlew assembleRelease
```

## Deployment

### Debug Deployment
```bash
./gradlew installDebug
```

### Release Deployment
```bash
./gradlew installRelease
```

## Security Considerations

### File Verification
- **Checksum Validation**: Verify downloaded APK integrity
- **Signature Verification**: Validate APK signatures
- **Secure Downloads**: Use HTTPS for all downloads

### Permissions
- **Internet**: Required for update checking and downloading
- **Install Packages**: Required for APK installation
- **Storage**: Required for APK file storage

## Troubleshooting

### Common Issues
1. **Update Check Failures**: Verify network connectivity and server configuration
2. **Download Failures**: Check storage space and network stability
3. **Installation Failures**: Verify APK signature and permissions

### Debugging
- Enable debug logging in `UpdateRepository.kt`
- Check network logs for API communication
- Verify file permissions and storage access

## Integration with Main App

### Communication
The updater communicates with the main TruckDoc app through:
- **Shared Preferences**: Update settings and status
- **File Provider**: APK file sharing
- **Broadcast Receivers**: Update status notifications

### Installation Process
1. Download APK to shared storage
2. Verify file integrity
3. Launch installation intent
4. Handle installation result

## Contributing

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Maintain consistent formatting

### Git Workflow
1. Create feature branch from `main`
2. Implement changes with tests
3. Run full test suite
4. Submit pull request with description

### Code Review
- All changes require code review
- Ensure tests pass
- Verify build success
- Check for security implications

## License

This module is part of the TruckDoc mobile application and is proprietary software. 