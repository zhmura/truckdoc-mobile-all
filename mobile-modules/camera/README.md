# TruckDoc Camera Library

## Overview

The TruckDoc Camera Library is a reusable Android library module that provides camera functionality for the TruckDoc mobile application. This module handles photo capture, image processing, and file management for document scanning and image uploads.

## Features

### Core Functionality
- **Photo Capture**: High-quality photo capture with camera controls
- **Document Scanning**: Optimized for document and receipt scanning
- **Image Processing**: Automatic image enhancement and optimization
- **File Management**: Efficient file storage and retrieval
- **Multiple Formats**: Support for JPEG, PNG, and other image formats
- **Quality Control**: Configurable image quality and compression

### Advanced Features
- **Auto-Focus**: Automatic focus detection for clear images
- **Flash Control**: Manual and automatic flash control
- **Image Stabilization**: Reduce blur and improve image quality
- **Batch Processing**: Capture multiple images in sequence
- **Custom Overlays**: Document guides and alignment helpers
- **Metadata Handling**: EXIF data preservation and management

## Architecture

### Technology Stack
- **Language**: Java and Kotlin
- **Camera API**: Camera2 API for modern devices, Camera API for legacy support
- **Image Processing**: Android Bitmap manipulation
- **File Management**: Android FileProvider
- **UI Components**: Custom camera interface
- **Testing**: JUnit, Mockito, Espresso

### Key Components

#### Camera Management
- **CameraManager**: Handles camera initialization and configuration
- **CameraController**: Manages camera operations and state
- **ImageProcessor**: Handles image enhancement and optimization
- **FileHandler**: Manages file storage and retrieval

#### UI Components
- **CameraView**: Custom camera preview component
- **CameraOverlay**: Document guides and UI overlays
- **CameraControls**: Camera control buttons and sliders
- **ImagePreview**: Captured image preview and editing

#### Utilities
- **ImageUtils**: Image manipulation and processing utilities
- **FileUtils**: File management and storage utilities
- **PermissionUtils**: Camera and storage permission handling
- **NotificationHelper**: Camera-related notifications

## Project Structure

```
src/main/java/app/camera/tdoc/camera_library/
├── camera/                  # Camera management
│   ├── controller/         # Camera controllers
│   ├── manager/            # Camera managers
│   └── preview/            # Camera preview components
├── image/                  # Image processing
│   ├── processor/          # Image processors
│   ├── enhancement/        # Image enhancement
│   └── compression/        # Image compression
├── ui/                     # User interface
│   ├── view/               # Custom views
│   ├── overlay/            # Camera overlays
│   └── controls/           # Camera controls
├── file/                   # File management
│   ├── handler/            # File handlers
│   ├── storage/            # Storage utilities
│   └── provider/           # File providers
├── util/                   # Utilities
│   ├── permission/         # Permission handling
│   ├── notification/       # Notifications
│   └── common/             # Common utilities
└── model/                  # Data models
    ├── config/             # Configuration models
    └── result/             # Result models
```

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Build Configuration
The camera library supports multiple build variants:
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

### Dependencies
Key dependencies include:
- **Android Camera2**: Camera API support
- **Android Support**: Support library components
- **JUnit & Mockito**: Testing frameworks
- **Espresso**: UI testing

## Usage

### Basic Integration

#### 1. Add Dependency
```gradle
implementation project(':camera')
```

#### 2. Request Permissions
```kotlin
// Request camera permission
if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
    != PackageManager.PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(this, 
        arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
}
```

#### 3. Initialize Camera
```kotlin
val cameraManager = CameraManager(this)
cameraManager.initializeCamera()
```

#### 4. Capture Image
```kotlin
cameraManager.captureImage { result ->
    when (result) {
        is Success -> {
            val imageFile = result.data
            // Handle captured image
        }
        is Error -> {
            // Handle error
        }
    }
}
```

### Advanced Configuration

#### Camera Configuration
```kotlin
val config = CameraConfig.Builder()
    .setQuality(ImageQuality.HIGH)
    .setFlashMode(FlashMode.AUTO)
    .setFocusMode(FocusMode.AUTO)
    .setImageFormat(ImageFormat.JPEG)
    .build()

cameraManager.configure(config)
```

#### Image Processing
```kotlin
val processor = ImageProcessor()
processor.setEnhancement(Enhancement.AUTO_CONTRAST)
processor.setCompression(Compression.HIGH_QUALITY)
processor.processImage(imageFile) { processedFile ->
    // Handle processed image
}
```

## API Reference

### CameraManager
Main class for camera operations.

#### Methods
- `initializeCamera()`: Initialize camera system
- `configure(config: CameraConfig)`: Configure camera settings
- `captureImage(callback: ResultCallback<File>)`: Capture image
- `startPreview()`: Start camera preview
- `stopPreview()`: Stop camera preview
- `release()`: Release camera resources

### CameraConfig
Configuration class for camera settings.

#### Properties
- `quality: ImageQuality`: Image quality setting
- `flashMode: FlashMode`: Flash mode setting
- `focusMode: FocusMode`: Focus mode setting
- `imageFormat: ImageFormat`: Image format setting

### ImageProcessor
Class for image processing operations.

#### Methods
- `processImage(file: File, callback: ResultCallback<File>)`: Process image
- `setEnhancement(enhancement: Enhancement)`: Set enhancement mode
- `setCompression(compression: Compression)`: Set compression level

## Testing

### Unit Tests
- **Location**: `src/test/java/`
- **Coverage**: Business logic and utilities
- **Frameworks**: JUnit 4, Mockito

### Instrumented Tests
- **Location**: `src/androidTest/java/`
- **Coverage**: Camera operations and UI components
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

### AAR Generation
```bash
# Generate debug AAR
./gradlew assembleDebug

# Generate release AAR
./gradlew assembleRelease
```

## Configuration

### Camera Permissions
The library requires the following permissions:
- `CAMERA`: For camera access
- `WRITE_EXTERNAL_STORAGE`: For image storage
- `READ_EXTERNAL_STORAGE`: For image access

### File Provider Configuration
Add to `AndroidManifest.xml`:
```xml
<provider
    android:name="app.camera.tdoc.camera_library.file.CameraFileProvider"
    android:authorities="${applicationId}.camera.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/camera_file_paths" />
</provider>
```

### ProGuard Rules
Add to `proguard-rules.pro`:
```proguard
# Camera library
-keep class app.camera.tdoc.camera_library.** { *; }
-keepclassmembers class app.camera.tdoc.camera_library.** { *; }
```

## Performance Considerations

### Memory Management
- **Image Caching**: Efficient image caching to reduce memory usage
- **Bitmap Recycling**: Proper bitmap recycling to prevent memory leaks
- **File Cleanup**: Automatic cleanup of temporary files

### Battery Optimization
- **Camera Release**: Proper camera resource release
- **Background Processing**: Efficient background image processing
- **Power Management**: Respect device power management settings

### Storage Optimization
- **Compression**: Configurable image compression
- **Format Selection**: Optimal image format selection
- **File Size Control**: Maximum file size limits

## Troubleshooting

### Common Issues
1. **Camera Not Available**: Check camera permissions and device compatibility
2. **Image Quality Issues**: Verify camera configuration and image processing settings
3. **File Access Errors**: Check storage permissions and file provider configuration
4. **Memory Issues**: Monitor memory usage and implement proper cleanup

### Debugging
- Enable debug logging in `CameraManager.kt`
- Check camera logs for initialization issues
- Verify file permissions and storage access
- Monitor memory usage during image processing

## Integration Examples

### Document Scanning
```kotlin
val scanner = DocumentScanner(cameraManager)
scanner.setDocumentGuide(true)
scanner.setAutoEnhancement(true)
scanner.scanDocument { result ->
    when (result) {
        is Success -> {
            val document = result.data
            // Handle scanned document
        }
        is Error -> {
            // Handle error
        }
    }
}
```

### Receipt Capture
```kotlin
val receiptCapture = ReceiptCapture(cameraManager)
receiptCapture.setReceiptMode(true)
receiptCapture.setAutoCrop(true)
receiptCapture.captureReceipt { result ->
    when (result) {
        is Success -> {
            val receipt = result.data
            // Handle captured receipt
        }
        is Error -> {
            // Handle error
        }
    }
}
```

## Contributing

### Code Style
- Follow Java and Kotlin coding conventions
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
- Check for performance implications

## License

This module is part of the TruckDoc mobile application and is proprietary software. 