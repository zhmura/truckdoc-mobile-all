# TruckDoc Common Modules

## Overview

The TruckDoc Common Modules contain shared utilities, APIs, and common functionality used across the TruckDoc mobile application ecosystem. This module provides reusable components, data models, and utilities that are shared between different modules of the TruckDoc application.

## Modules

### truckdoc-util
Utility library containing common helper functions, extensions, and utilities.

### truckdoc-client-api
API definitions and data models for client-server communication.

## Features

### Common Utilities (truckdoc-util)
- **String Utilities**: String manipulation and formatting utilities
- **Date/Time Utilities**: Date and time handling utilities
- **File Utilities**: File operations and management utilities
- **Network Utilities**: Network connectivity and status utilities
- **Security Utilities**: Encryption, hashing, and security utilities
- **Validation Utilities**: Data validation and verification utilities
- **UI Utilities**: Common UI helper functions and extensions

### Client API (truckdoc-client-api)
- **API Models**: Data models for API requests and responses
- **API Interfaces**: Retrofit service interface definitions
- **API Constants**: API endpoint constants and configuration
- **API Utilities**: API-related utility functions
- **Error Models**: Standardized error response models
- **Serialization**: JSON serialization and deserialization utilities

## Architecture

### Technology Stack
- **Language**: Java and Kotlin
- **Networking**: Retrofit service interfaces
- **JSON Processing**: Gson for serialization/deserialization
- **Utilities**: Custom utility functions and extensions
- **Testing**: JUnit, Mockito, comprehensive test coverage

### Key Components

#### Utility Components (truckdoc-util)
- **StringUtils**: String manipulation and formatting
- **DateUtils**: Date and time operations
- **FileUtils**: File operations and management
- **NetworkUtils**: Network connectivity utilities
- **SecurityUtils**: Security and encryption utilities
- **ValidationUtils**: Data validation utilities
- **UIUtils**: UI helper functions

#### API Components (truckdoc-client-api)
- **ApiModels**: Request and response data models
- **ApiServices**: Retrofit service interfaces
- **ApiConstants**: API configuration constants
- **ApiUtils**: API utility functions
- **ErrorModels**: Error response models
- **SerializationUtils**: JSON serialization utilities

## Project Structure

```
mobile-modules/truckdoccommon/
├── truckdoc-util/          # Utility library
│   ├── src/main/java/
│   │   ├── string/         # String utilities
│   │   ├── date/           # Date/time utilities
│   │   ├── file/           # File utilities
│   │   ├── network/        # Network utilities
│   │   ├── security/       # Security utilities
│   │   ├── validation/     # Validation utilities
│   │   ├── ui/             # UI utilities
│   │   └── common/         # Common utilities
│   └── build.gradle
├── truckdoc-client-api/    # Client API library
│   ├── src/main/java/
│   │   ├── model/          # API models
│   │   ├── service/        # API services
│   │   ├── constant/       # API constants
│   │   ├── util/           # API utilities
│   │   ├── error/          # Error models
│   │   └── serialization/  # Serialization utilities
│   └── build.gradle
└── README.md
```

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Build Configuration
Both modules support multiple build variants:
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

### Dependencies
Key dependencies include:
- **Retrofit**: API service interfaces
- **Gson**: JSON processing
- **Android Support**: Support library components
- **JUnit & Mockito**: Testing frameworks

## Usage

### truckdoc-util Integration

#### 1. Add Dependency
```gradle
implementation project(':truckdoc-util')
```

#### 2. String Utilities
```java
// String formatting
String formatted = StringUtils.formatPhoneNumber("1234567890");
String masked = StringUtils.maskEmail("user@example.com");

// String validation
boolean isValidEmail = StringUtils.isValidEmail("user@example.com");
boolean isValidPhone = StringUtils.isValidPhone("+1234567890");
```

#### 3. Date/Time Utilities
```java
// Date formatting
String formatted = DateUtils.formatDate(date, "yyyy-MM-dd");
String relative = DateUtils.getRelativeTime(date);

// Date validation
boolean isValidDate = DateUtils.isValidDate("2024-01-15");
Date parsed = DateUtils.parseDate("2024-01-15", "yyyy-MM-dd");
```

#### 4. File Utilities
```java
// File operations
boolean exists = FileUtils.fileExists(filePath);
long size = FileUtils.getFileSize(filePath);
String extension = FileUtils.getFileExtension(filePath);

// File validation
boolean isValidImage = FileUtils.isValidImageFile(filePath);
boolean isValidDocument = FileUtils.isValidDocumentFile(filePath);
```

#### 5. Network Utilities
```java
// Network connectivity
boolean isConnected = NetworkUtils.isNetworkAvailable(context);
boolean isWifiConnected = NetworkUtils.isWifiConnected(context);

// Network information
String networkType = NetworkUtils.getNetworkType(context);
int signalStrength = NetworkUtils.getSignalStrength(context);
```

#### 6. Security Utilities
```java
// Encryption
String encrypted = SecurityUtils.encrypt(data, key);
String decrypted = SecurityUtils.decrypt(encrypted, key);

// Hashing
String hashed = SecurityUtils.hashString(input);
boolean matches = SecurityUtils.verifyHash(input, hashed);
```

### truckdoc-client-api Integration

#### 1. Add Dependency
```gradle
implementation project(':truckdoc-client-api')
```

#### 2. API Models
```java
// Request models
MessageRequest request = new MessageRequest();
request.setMessage("Hello World");
request.setRecipientId(123L);

// Response models
MessageResponse response = new MessageResponse();
response.setId(456L);
response.setMessage("Hello World");
response.setTimestamp(new Date());
```

#### 3. API Services
```java
// Service interface
@POST("messages")
Call<MessageResponse> sendMessage(@Body MessageRequest request);

@GET("messages")
Call<List<MessageResponse>> getMessages();

@GET("messages/{id}")
Call<MessageResponse> getMessage(@Path("id") Long id);
```

#### 4. API Constants
```java
// API endpoints
String MESSAGES_ENDPOINT = "/api/v1/messages";
String USERS_ENDPOINT = "/api/v1/users";
String FILES_ENDPOINT = "/api/v1/files";

// API headers
String AUTHORIZATION_HEADER = "Authorization";
String CONTENT_TYPE_HEADER = "Content-Type";
```

#### 5. API Utilities
```java
// Request building
Request.Builder requestBuilder = ApiUtils.createRequestBuilder();
requestBuilder.addHeader(AUTHORIZATION_HEADER, "Bearer " + token);

// Response handling
ApiResponse<MessageResponse> response = ApiUtils.parseResponse(rawResponse);
if (response.isSuccess()) {
    MessageResponse data = response.getData();
} else {
    ApiError error = response.getError();
}
```

## API Reference

### StringUtils
String manipulation utilities.

#### Methods
- `formatPhoneNumber(phone: String): String`: Format phone number
- `maskEmail(email: String): String`: Mask email address
- `isValidEmail(email: String): Boolean`: Validate email format
- `isValidPhone(phone: String): Boolean`: Validate phone format
- `capitalizeFirst(str: String): String`: Capitalize first letter
- `truncate(str: String, maxLength: Int): String`: Truncate string

### DateUtils
Date and time utilities.

#### Methods
- `formatDate(date: Date, pattern: String): String`: Format date
- `parseDate(dateStr: String, pattern: String): Date`: Parse date string
- `getRelativeTime(date: Date): String`: Get relative time
- `isValidDate(dateStr: String): Boolean`: Validate date string
- `addDays(date: Date, days: Int): Date`: Add days to date
- `getDaysBetween(date1: Date, date2: Date): Long`: Get days between dates

### FileUtils
File operation utilities.

#### Methods
- `fileExists(path: String): Boolean`: Check if file exists
- `getFileSize(path: String): Long`: Get file size
- `getFileExtension(path: String): String`: Get file extension
- `isValidImageFile(path: String): Boolean`: Validate image file
- `isValidDocumentFile(path: String): Boolean`: Validate document file
- `createDirectory(path: String): Boolean`: Create directory

### NetworkUtils
Network connectivity utilities.

#### Methods
- `isNetworkAvailable(context: Context): Boolean`: Check network availability
- `isWifiConnected(context: Context): Boolean`: Check WiFi connection
- `getNetworkType(context: Context): String`: Get network type
- `getSignalStrength(context: Context): Int`: Get signal strength
- `isNetworkMetered(context: Context): Boolean`: Check if network is metered

### SecurityUtils
Security and encryption utilities.

#### Methods
- `encrypt(data: String, key: String): String`: Encrypt data
- `decrypt(encrypted: String, key: String): String`: Decrypt data
- `hashString(input: String): String`: Hash string
- `verifyHash(input: String, hash: String): Boolean`: Verify hash
- `generateRandomString(length: Int): String`: Generate random string
- `generateUUID(): String`: Generate UUID

## Configuration

### Utility Configuration
```java
UtilConfig config = new UtilConfig.Builder()
    .setDateFormat("yyyy-MM-dd")
    .setTimeFormat("HH:mm:ss")
    .setMaxFileSize(10 * 1024 * 1024) // 10MB
    .setEncryptionEnabled(true)
    .build();
```

### API Configuration
```java
ApiConfig config = new ApiConfig.Builder()
    .setBaseUrl("https://api.truckdoc.com")
    .setApiVersion("v1")
    .setTimeout(30)
    .setRetryCount(3)
    .build();
```

## Testing

### Unit Tests
- **Location**: `src/test/java/`
- **Coverage**: Business logic and utilities
- **Frameworks**: JUnit 4, Mockito

### Integration Tests
- **Location**: `src/androidTest/java/`
- **Coverage**: API integration and utility functions
- **Frameworks**: AndroidJUnit4

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

## Performance Optimization

### Utility Optimization
- **Efficient Algorithms**: Optimized algorithms for common operations
- **Caching**: Intelligent caching for frequently used operations
- **Memory Management**: Proper memory management for utility objects
- **Performance Monitoring**: Performance tracking and optimization

### API Optimization
- **Request Optimization**: Optimized API request handling
- **Response Caching**: Intelligent response caching
- **Serialization Optimization**: Efficient JSON serialization
- **Error Handling**: Optimized error handling and recovery

## Security Considerations

### Data Security
- **Encryption**: Secure data encryption and decryption
- **Input Validation**: Comprehensive input validation
- **Output Sanitization**: Secure output sanitization
- **Access Control**: Proper access control for sensitive operations

### API Security
- **Authentication**: Secure API authentication
- **Authorization**: Proper API authorization
- **Data Protection**: Protection of sensitive API data
- **Error Handling**: Secure error handling without information leakage

## Troubleshooting

### Common Issues
1. **Utility Not Working**: Check utility configuration and dependencies
2. **API Issues**: Verify API configuration and network connectivity
3. **Performance Issues**: Monitor performance metrics and optimize
4. **Security Issues**: Verify security configuration and encryption

### Debugging
- Enable utility logging in respective utility classes
- Check API configuration and network logs
- Monitor performance metrics and memory usage
- Verify security settings and encryption

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