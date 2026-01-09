# TruckDoc Client Application

## Overview

The TruckDoc Client Application is the main Android application module that provides the core functionality for the TruckDoc mobile platform. This module serves as the primary interface for truck drivers and fleet managers to manage routes, messages, contacts, and locations.

## Features

### Core Functionality
- **Message Management**: Send and receive messages with support for attachments
- **Route Management**: View and manage delivery routes with GPS tracking
- **Contact Management**: Manage driver and dispatcher contacts
- **Location Tracking**: Real-time location services and history
- **File Upload**: Upload documents and images with progress tracking
- **Notifications**: Push notifications for new messages and updates

### UI Components
- **Dashboard**: Main activity with navigation to all features
- **Messages**: Inbox and outbox with message composition
- **Routes**: Route list and detailed view with map integration
- **Contacts**: Contact list with search and filtering
- **Locations**: Location history and current position
- **Settings**: Application configuration and preferences

## Architecture

### Technology Stack
- **Language**: Kotlin (primary) and Java
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Database**: Room with SQLite
- **Networking**: Retrofit with OkHttp
- **Image Loading**: Glide
- **UI**: Material Design components
- **Testing**: JUnit, Mockito, Espresso

### Key Components

#### Data Layer
- **Database**: Room database with entities for messages, contacts, routes, and locations
- **API Client**: Retrofit-based network client for server communication
- **Repository**: Data access layer that coordinates between local and remote data sources

#### Domain Layer
- **Use Cases**: Business logic implementation
- **Models**: Data models and entities
- **Services**: Background services for message checking and file uploads

#### Presentation Layer
- **Activities**: Main UI components
- **Fragments**: Modular UI components
- **ViewModels**: UI state management
- **Adapters**: RecyclerView adapters for lists

## Project Structure

```
src/main/java/com/sanda/truckdoc/client/
├── data/                    # Data layer
│   ├── dao/                # Room DAOs
│   ├── db/                 # Database configuration
│   ├── entity/             # Database entities
│   ├── model/              # Data models
│   ├── repository/         # Repository implementations
│   └── service/            # Network services
├── di/                     # Dependency injection
│   ├── modules/            # Hilt modules
│   └── qualifiers/         # Custom qualifiers
├── domain/                 # Domain layer
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic
├── ui/                     # Presentation layer
│   ├── contacts/           # Contact management UI
│   ├── dashboard/          # Dashboard UI
│   ├── locations/          # Location tracking UI
│   ├── messages/           # Message management UI
│   ├── routes/             # Route management UI
│   └── utils/              # UI utilities
├── service/                # Background services
├── receiver/               # Broadcast receivers
├── util/                   # Utility classes
└── TruckDocApp.kt         # Application class
```

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Build Configuration
The application supports multiple build variants:
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization
- **Client Variants**: Different client configurations (defaultClient, etc.)

### Dependencies
Key dependencies include:
- **Hilt**: Dependency injection
- **Room**: Local database
- **Retrofit**: Network communication
- **Glide**: Image loading
- **Material Design**: UI components
- **JUnit & Mockito**: Testing

## Testing

### Unit Tests
- **Location**: `src/test/java/`
- **Coverage**: Business logic, data layer, and utilities
- **Frameworks**: JUnit 4, Mockito, Kotlin Test

### Instrumented Tests
- **Location**: `src/androidTest/java/`
- **Coverage**: UI components and integration
- **Frameworks**: Espresso, AndroidJUnit4

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test variant
./gradlew testDefaultClientDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Build Process

### Debug Build
```bash
./gradlew assembleDefaultClientDebug
```

### Release Build
```bash
./gradlew assembleDefaultClientRelease
```

### APK Generation
```bash
# Generate debug APK
./gradlew assembleDefaultClientDebug

# Generate release APK
./gradlew assembleDefaultClientRelease
```

## Configuration

### Build Variants
The application supports multiple client configurations through build variants:
- `defaultClient`: Standard client configuration
- Additional client variants can be added as needed

### ProGuard Rules
Release builds use ProGuard for code optimization and obfuscation. Rules are defined in `proguard-rules.pro`.

### Signing
Release builds are signed using the keystore configuration in `keystore.properties`.

## Deployment

### Debug Deployment
```bash
./gradlew installDefaultClientDebug
```

### Release Deployment
```bash
./gradlew installDefaultClientRelease
```

## Troubleshooting

### Common Issues
1. **Build Failures**: Check Gradle version compatibility
2. **Test Failures**: Ensure all dependencies are properly configured
3. **Runtime Errors**: Verify database migrations and API endpoints

### Debugging
- Enable debug logging in `TruckDocApp.kt`
- Use Android Studio's debugger for step-through debugging
- Check logcat for runtime errors

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
- Check for performance implications

## License

This module is part of the TruckDoc mobile application and is proprietary software. 