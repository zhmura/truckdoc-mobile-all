# TruckDoc Network Library

## Overview

The TruckDoc Network Library is a reusable Android library module that provides networking functionality for the TruckDoc mobile application. This module handles all network communications, API calls, authentication, and data synchronization between the mobile app and the TruckDoc server infrastructure.

## Features

### Core Functionality
- **API Communication**: RESTful API client for server communication
- **Authentication**: Token-based authentication and session management
- **Data Synchronization**: Real-time data sync between client and server
- **File Upload/Download**: Efficient file transfer with progress tracking
- **Offline Support**: Offline data caching and synchronization
- **Error Handling**: Comprehensive error handling and retry mechanisms

### Advanced Features
- **Request/Response Interceptors**: Custom request and response processing
- **Caching**: Intelligent caching for improved performance
- **Compression**: Request/response compression for bandwidth optimization
- **SSL/TLS**: Secure communication with certificate pinning
- **Rate Limiting**: API rate limiting and throttling
- **Metrics**: Network performance monitoring and analytics

## Architecture

### Technology Stack
- **Language**: Java and Kotlin
- **Networking**: Retrofit with OkHttp
- **JSON Processing**: Gson for JSON serialization/deserialization
- **Caching**: OkHttp cache and custom caching strategies
- **Authentication**: JWT token management
- **Testing**: JUnit, Mockito, WireMock

### Key Components

#### Network Layer
- **ApiService**: Retrofit interface definitions for API endpoints
- **NetworkManager**: Central network management and configuration
- **RequestInterceptor**: Request modification and authentication
- **ResponseInterceptor**: Response processing and error handling

#### Authentication
- **AuthManager**: Authentication state management
- **TokenManager**: JWT token storage and refresh
- **SessionManager**: User session management
- **LoginService**: Login and logout operations

#### Data Management
- **DataSyncManager**: Data synchronization between client and server
- **CacheManager**: Data caching and cache invalidation
- **FileManager**: File upload and download operations
- **OfflineManager**: Offline data management

#### Utilities
- **NetworkUtils**: Network connectivity and status utilities
- **ApiUtils**: API endpoint and parameter utilities
- **ErrorUtils**: Error handling and user-friendly error messages
- **SecurityUtils**: Security and encryption utilities

## Project Structure

```
src/main/java/com/sanda/truckdoc/network/
├── api/                    # API interfaces
│   ├── service/           # Retrofit service interfaces
│   ├── model/             # API request/response models
│   └── endpoint/          # API endpoint definitions
├── auth/                  # Authentication
│   ├── manager/           # Authentication managers
│   ├── token/             # Token management
│   └── session/           # Session management
├── data/                  # Data management
│   ├── sync/              # Data synchronization
│   ├── cache/             # Caching strategies
│   └── offline/           # Offline data management
├── file/                  # File operations
│   ├── upload/            # File upload
│   ├── download/          # File download
│   └── manager/           # File management
├── interceptor/           # Network interceptors
│   ├── request/           # Request interceptors
│   ├── response/          # Response interceptors
│   └── logging/           # Logging interceptors
├── manager/               # Network managers
├── util/                  # Utilities
│   ├── network/           # Network utilities
│   ├── api/               # API utilities
│   ├── error/             # Error handling
│   └── security/          # Security utilities
└── config/                # Configuration
    ├── network/           # Network configuration
    └── api/               # API configuration
```

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Build Configuration
The network library supports multiple build variants:
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

### Dependencies
Key dependencies include:
- **Retrofit**: REST API client
- **OkHttp**: HTTP client
- **Gson**: JSON processing
- **JUnit & Mockito**: Testing frameworks
- **WireMock**: API mocking for testing

## Usage

### Basic Integration

#### 1. Add Dependency
```gradle
implementation project(':truckdocnetwork')
```

#### 2. Initialize Network Manager
```kotlin
val networkManager = NetworkManager.Builder()
    .baseUrl("https://api.truckdoc.com")
    .timeout(30)
    .enableLogging(true)
    .build()

networkManager.initialize()
```

#### 3. Create API Service
```kotlin
@GET("messages")
suspend fun getMessages(): Response<List<Message>>

@POST("messages")
suspend fun sendMessage(@Body message: Message): Response<Message>
```

#### 4. Make API Calls
```kotlin
val apiService = networkManager.createApiService(ApiService::class.java)

try {
    val messages = apiService.getMessages()
    // Handle response
} catch (e: Exception) {
    // Handle error
}
```

### Authentication

#### 1. Login
```kotlin
val authManager = AuthManager(networkManager)
authManager.login(username, password) { result ->
    when (result) {
        is Success -> {
            val token = result.data
            // Store token and proceed
        }
        is Error -> {
            // Handle login error
        }
    }
}
```

#### 2. Authenticated Requests
```kotlin
// Token is automatically added to requests
val messages = apiService.getMessages()
```

#### 3. Token Refresh
```kotlin
// Automatic token refresh is handled by interceptors
authManager.refreshToken { result ->
    // Handle refresh result
}
```

### File Operations

#### 1. File Upload
```kotlin
val fileManager = FileManager(networkManager)
fileManager.uploadFile(file, "documents") { progress, result ->
    when (result) {
        is Success -> {
            val uploadedFile = result.data
            // Handle uploaded file
        }
        is Error -> {
            // Handle upload error
        }
    }
}
```

#### 2. File Download
```kotlin
fileManager.downloadFile(fileUrl, destinationFile) { progress, result ->
    when (result) {
        is Success -> {
            val downloadedFile = result.data
            // Handle downloaded file
        }
        is Error -> {
            // Handle download error
        }
    }
}
```

## API Reference

### NetworkManager
Main class for network operations.

#### Methods
- `initialize()`: Initialize network manager
- `createApiService(serviceClass: Class<T>)`: Create API service
- `setBaseUrl(url: String)`: Set base URL
- `setTimeout(timeout: Int)`: Set request timeout
- `enableLogging(enable: Boolean)`: Enable/disable logging

### AuthManager
Authentication management.

#### Methods
- `login(username: String, password: String, callback: ResultCallback<Token>)`: Login
- `logout(callback: ResultCallback<Unit>)`: Logout
- `refreshToken(callback: ResultCallback<Token>)`: Refresh token
- `isAuthenticated(): Boolean`: Check authentication status

### FileManager
File upload and download operations.

#### Methods
- `uploadFile(file: File, endpoint: String, callback: ProgressCallback<File>)`: Upload file
- `downloadFile(url: String, destination: File, callback: ProgressCallback<File>)`: Download file
- `cancelUpload(uploadId: String)`: Cancel upload
- `cancelDownload(downloadId: String)`: Cancel download

## Configuration

### Network Configuration
```kotlin
val config = NetworkConfig.Builder()
    .baseUrl("https://api.truckdoc.com")
    .timeout(30)
    .retryCount(3)
    .enableCache(true)
    .cacheSize(10 * 1024 * 1024) // 10MB
    .build()
```

### API Configuration
```kotlin
val apiConfig = ApiConfig.Builder()
    .version("v1")
    .format("json")
    .compression(true)
    .build()
```

### Security Configuration
```kotlin
val securityConfig = SecurityConfig.Builder()
    .certificatePinning(true)
    .sslVerification(true)
    .tokenEncryption(true)
    .build()
```

## Testing

### Unit Tests
- **Location**: `src/test/java/`
- **Coverage**: Business logic and utilities
- **Frameworks**: JUnit 4, Mockito

### Integration Tests
- **Location**: `src/androidTest/java/`
- **Coverage**: API integration and network operations
- **Frameworks**: WireMock, AndroidJUnit4

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test variant
./gradlew testDebugUnitTest

# Run integration tests
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

## Error Handling

### Network Errors
```kotlin
sealed class NetworkError {
    object NoConnection : NetworkError()
    object Timeout : NetworkError()
    object ServerError : NetworkError()
    object AuthenticationError : NetworkError()
    data class ApiError(val code: Int, val message: String) : NetworkError()
}
```

### Error Handling Example
```kotlin
try {
    val result = apiService.getData()
    // Handle success
} catch (e: NetworkError) {
    when (e) {
        is NetworkError.NoConnection -> {
            // Handle no connection
        }
        is NetworkError.AuthenticationError -> {
            // Handle authentication error
        }
        is NetworkError.ApiError -> {
            // Handle API error
        }
        else -> {
            // Handle other errors
        }
    }
}
```

## Performance Optimization

### Caching Strategy
- **HTTP Cache**: OkHttp HTTP caching
- **Response Cache**: Custom response caching
- **Image Cache**: Image caching for network images
- **Cache Invalidation**: Intelligent cache invalidation

### Request Optimization
- **Request Compression**: Gzip compression for requests
- **Response Compression**: Gzip compression for responses
- **Connection Pooling**: HTTP connection pooling
- **Request Batching**: Batch multiple requests

### Memory Management
- **Stream Processing**: Stream-based file processing
- **Memory Pooling**: Object pooling for network objects
- **Garbage Collection**: Proper cleanup and garbage collection

## Security

### Authentication
- **JWT Tokens**: JSON Web Token authentication
- **Token Refresh**: Automatic token refresh
- **Secure Storage**: Encrypted token storage
- **Session Management**: Secure session management

### Network Security
- **SSL/TLS**: Secure communication protocols
- **Certificate Pinning**: Certificate pinning for security
- **Request Signing**: Request signature verification
- **Data Encryption**: End-to-end data encryption

## Troubleshooting

### Common Issues
1. **Network Timeout**: Check network connectivity and timeout settings
2. **Authentication Errors**: Verify token validity and refresh mechanism
3. **File Upload Failures**: Check file size limits and network stability
4. **Cache Issues**: Verify cache configuration and storage

### Debugging
- Enable network logging in `NetworkManager.kt`
- Check network logs for request/response details
- Verify API endpoints and authentication
- Monitor network performance metrics

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
- Check for security implications

## License

This module is part of the TruckDoc mobile application and is proprietary software. 