# TruckDoc Mobile Application

## Overview

TruckDoc is a comprehensive mobile application designed for truck drivers and fleet managers to manage routes, messages, contacts, and locations. The application provides real-time communication, route tracking, and document management capabilities for the transportation industry.

## Project Architecture

The TruckDoc mobile application is built using a modular architecture with the following components:

### Core Modules

#### 🏠 [Main Application](truckdoc-client-m2/application/README.md)
The primary Android application module containing the main UI, business logic, and user interface components.

**Key Features:**
- Message management and communication
- Route tracking and management
- Contact management
- Location services
- File upload and document management
- Real-time notifications

#### 🔄 [App Updater](app-updater/README.md)
Standalone update management module that handles automatic application updates.

**Key Features:**
- Automatic update detection
- Background update checking
- APK download and installation
- User notification system
- Update scheduling and configuration

#### 📷 [Camera Library](mobile-modules/camera/README.md)
Reusable camera functionality for document scanning and photo capture.

**Key Features:**
- High-quality photo capture
- Document scanning optimization
- Image processing and enhancement
- File format support
- Custom camera overlays

#### 🌐 [Network Library](mobile-modules/truckdocnetwork/app/README.md)
Networking and API communication module.

**Key Features:**
- RESTful API client
- Authentication and session management
- File upload/download
- Data synchronization
- Offline support and caching

#### ✅ [Checker Module](truckdoc-client-m2/checker/README.md)
Data validation and verification module.

**Key Features:**
- Comprehensive data validation
- Business rule enforcement
- Real-time validation
- Error detection and recovery
- Quality assurance

#### 🪟 [Standout Module](truckdoc-client-m2/standout/README.md)
Floating window and overlay functionality.

**Key Features:**
- Floating UI elements
- Quick action buttons
- Information display overlays
- Interactive floating components
- Window management

#### 🔧 [Common Modules](mobile-modules/truckdoccommon/README.md)
Shared utilities and API definitions.

**Key Features:**
- Common utility functions
- API models and interfaces
- Shared data structures
- Reusable components

## Technology Stack

### Core Technologies
- **Language**: Kotlin (primary) and Java
- **Architecture**: MVVM with Repository pattern
- **Dependency Injection**: Hilt
- **Database**: Room with SQLite
- **Networking**: Retrofit with OkHttp
- **Image Loading**: Glide
- **UI**: Material Design components

### Build System
- **Build Tool**: Gradle 8.0+
- **Android Gradle Plugin**: Latest stable version
- **Kotlin Version**: 1.8+
- **Target SDK**: API 33+
- **Minimum SDK**: API 21+

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Installation

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd truckdoc-mobile
   ```

2. **Configure Environment**
   ```bash
   # Copy and configure local.properties
   cp local.properties.example local.properties
   # Edit local.properties with your SDK path
   ```

3. **Sync Project**
   ```bash
   ./gradlew clean
   ./gradlew build
   ```

4. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory
   - Wait for Gradle sync to complete

### Build Configuration

The project supports multiple build variants:

- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization
- **Client Variants**: Different client configurations (defaultClient, etc.)

### Dependencies

Key project dependencies include:
- **Hilt**: Dependency injection
- **Room**: Local database
- **Retrofit**: Network communication
- **Glide**: Image loading
- **Material Design**: UI components
- **JUnit & Mockito**: Testing

## Project Structure

```
truckdoc-mobile/
├── app-updater/                    # App update module
├── truckdoc-client-m2/             # Main client modules
│   ├── application/               # Main application
│   ├── checker/                   # Validation module
│   ├── standout/                  # Floating window module
│   ├── truckdoc-client-api/       # Client API
│   └── truckdoc-client-updater/   # Client updater
├── mobile-modules/                 # Shared modules
│   ├── camera/                    # Camera library
│   ├── truckdoccommon/            # Common utilities
│   │   ├── truckdoc-util/         # Utility library
│   │   └── truckdoc-client-api/   # API definitions
│   ├── truckdocnetwork/           # Network library
│   └── instructions/              # Instructions module
├── buildSrc/                      # Build configuration
├── gradle/                        # Gradle wrapper
├── jenkins/                       # CI/CD configuration
├── build.gradle                   # Root build configuration
├── settings.gradle                # Project settings
└── README.md                      # This file
```

## Building the Project

### Debug Build
```bash
./gradlew assembleDefaultClientDebug
```

### Release Build
```bash
./gradlew assembleDefaultClientRelease
```

### All Modules
```bash
./gradlew build
```

### Specific Module
```bash
./gradlew :app:assembleDebug
./gradlew :app-updater:assembleDebug
./gradlew :camera:assembleDebug
```

## Testing

### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific module tests
./gradlew :app:testDefaultClientDebugUnitTest
```

### Instrumented Tests
```bash
# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific module instrumented tests
./gradlew :app:connectedDefaultClientDebugAndroidTest
```

### Test Coverage
```bash
# Generate test coverage report
./gradlew jacocoTestReport
```

## Development Workflow

### 1. Feature Development
1. Create feature branch from `main`
2. Implement changes with appropriate tests
3. Run full test suite
4. Update documentation if needed
5. Submit pull request

### 2. Code Review Process
- All changes require code review
- Ensure tests pass
- Verify build success
- Check for performance implications
- Update relevant documentation

### 3. Testing Strategy
- **Unit Tests**: Business logic and data layer
- **Integration Tests**: API integration and database operations
- **UI Tests**: User interface components
- **Performance Tests**: Performance critical components

## Configuration

### Build Variants
The application supports multiple client configurations:
- `defaultClient`: Standard client configuration
- Additional client variants can be added as needed

### Environment Configuration
- **Development**: Debug builds with logging enabled
- **Staging**: Release builds with staging server configuration
- **Production**: Release builds with production server configuration

### ProGuard Rules
Release builds use ProGuard for code optimization and obfuscation. Rules are defined in `proguard-rules.pro` files for each module.

### Signing Configuration
Release builds are signed using keystore configuration in `keystore.properties`.

## Deployment

### Debug Deployment
```bash
./gradlew installDefaultClientDebug
```

### Release Deployment
```bash
./gradlew installDefaultClientRelease
```

### APK Generation
```bash
# Generate debug APK
./gradlew assembleDefaultClientDebug

# Generate release APK
./gradlew assembleDefaultClientRelease
```

## CI/CD Pipeline

### Jenkins Configuration
The project includes Jenkins configuration for automated builds and deployments:

- **Build Pipeline**: Automated build and test execution
- **Quality Gates**: Code quality and test coverage checks
- **Deployment**: Automated deployment to staging and production
- **Monitoring**: Build and deployment monitoring

### Build Commands
See [BUILD_COMMANDS.md](BUILD_COMMANDS.md) for detailed build commands and scripts.

## Troubleshooting

### Common Issues

#### Build Failures
1. **Gradle Version**: Ensure Gradle version compatibility
2. **SDK Issues**: Verify Android SDK installation and configuration
3. **Dependencies**: Check dependency versions and conflicts
4. **Memory Issues**: Increase Gradle memory allocation if needed

#### Test Failures
1. **Unit Tests**: Check test configuration and dependencies
2. **Instrumented Tests**: Verify device/emulator setup
3. **Network Tests**: Check network configuration for API tests
4. **Database Tests**: Verify database configuration and migrations

#### Runtime Issues
1. **Permissions**: Ensure all required permissions are granted
2. **Network**: Check network connectivity and API endpoints
3. **Storage**: Verify storage permissions and space availability
4. **Memory**: Monitor memory usage and optimize if needed

### Debugging
- Enable debug logging in respective modules
- Use Android Studio's debugger for step-through debugging
- Check logcat for runtime errors and warnings
- Monitor performance metrics and memory usage

## Performance Optimization

### Build Performance
- **Gradle Optimization**: Parallel builds and build caching
- **Dependency Management**: Efficient dependency resolution
- **Incremental Builds**: Leverage incremental build capabilities
- **Build Variants**: Optimize build variant configuration

### Runtime Performance
- **Memory Management**: Efficient memory usage and garbage collection
- **Network Optimization**: Request/response optimization and caching
- **UI Performance**: Smooth UI rendering and interaction
- **Database Optimization**: Efficient database operations and queries

## Security

### Code Security
- **Input Validation**: Comprehensive input validation across all modules
- **Data Encryption**: Secure data storage and transmission
- **Authentication**: Secure authentication and session management
- **Authorization**: Proper access control and permissions

### Network Security
- **SSL/TLS**: Secure communication protocols
- **Certificate Pinning**: Certificate pinning for API communication
- **Request Signing**: Request signature verification
- **Data Protection**: End-to-end data protection

## Contributing

### Development Guidelines
- Follow Kotlin and Java coding conventions
- Use meaningful variable and function names
- Add comprehensive comments for complex logic
- Maintain consistent code formatting
- Write unit tests for all new functionality

### Git Workflow
1. Create feature branch from `main`
2. Implement changes with appropriate tests
3. Run full test suite and ensure all tests pass
4. Update documentation if needed
5. Submit pull request with detailed description

### Code Review Checklist
- [ ] Code follows project conventions
- [ ] All tests pass
- [ ] Build succeeds without errors
- [ ] Documentation is updated
- [ ] Performance implications considered
- [ ] Security implications reviewed

## Documentation

### Module Documentation
Each module has its own README file with detailed documentation:
- [Application Module](truckdoc-client-m2/application/README.md)
- [App Updater Module](app-updater/README.md)
- [Camera Library](mobile-modules/camera/README.md)
- [Network Library](mobile-modules/truckdocnetwork/app/README.md)
- [Checker Module](truckdoc-client-m2/checker/README.md)
- [Standout Module](truckdoc-client-m2/standout/README.md)
- [Common Modules](mobile-modules/truckdoccommon/README.md)

### Additional Documentation
- [Build Commands](BUILD_COMMANDS.md): Detailed build commands and scripts
- [Migration Notes](MIGRATION_NOTES.md): Migration guides and notes
- [Enhanced Features](app-updater/ENHANCED_FEATURES.md): App updater features

## Support

### Getting Help
- Check the troubleshooting section above
- Review module-specific documentation
- Check existing issues and pull requests
- Contact the development team

### Reporting Issues
When reporting issues, please include:
- Device information (model, OS version)
- App version and build variant
- Steps to reproduce the issue
- Logs and error messages
- Screenshots if applicable

## License

This project is proprietary software owned by TruckDoc. All rights reserved.

## Version History

### Current Version
- **Version**: 2.0
- **Release Date**: January 2024
- **Major Features**: Modular architecture, enhanced UI, improved performance

### Previous Versions
- **Version 1.0**: Initial release with basic functionality
- **Version 1.5**: Enhanced features and bug fixes

---

For more information about specific modules, please refer to their individual README files. 