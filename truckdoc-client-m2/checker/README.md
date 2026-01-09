# TruckDoc Checker Module

## Overview

The TruckDoc Checker Module is a specialized Android library module that provides validation, verification, and checking functionality for the TruckDoc mobile application. This module handles data validation, integrity checks, and verification processes to ensure data quality and system reliability.

## Features

### Core Functionality
- **Data Validation**: Comprehensive data validation for all input fields
- **Integrity Checks**: Data integrity verification and consistency checks
- **Format Validation**: File format and content validation
- **Business Rule Validation**: Application-specific business rule enforcement
- **Error Detection**: Automatic error detection and reporting
- **Quality Assurance**: Data quality assessment and improvement

### Advanced Features
- **Real-time Validation**: Live validation as users input data
- **Batch Validation**: Efficient validation of large datasets
- **Custom Validators**: Extensible validation framework
- **Validation Rules**: Configurable validation rules and constraints
- **Error Recovery**: Automatic error recovery and correction suggestions
- **Performance Monitoring**: Validation performance tracking and optimization

## Architecture

### Technology Stack
- **Language**: Java and Kotlin
- **Validation Framework**: Custom validation engine
- **Data Processing**: Efficient data processing algorithms
- **Error Handling**: Comprehensive error handling system
- **Testing**: JUnit, Mockito, comprehensive test coverage

### Key Components

#### Validation Engine
- **ValidatorManager**: Central validation management and coordination
- **ValidationRules**: Rule definitions and enforcement
- **ValidationContext**: Validation context and state management
- **ValidationResult**: Validation result processing and reporting

#### Data Validators
- **FieldValidator**: Individual field validation
- **FormValidator**: Complete form validation
- **FileValidator**: File format and content validation
- **BusinessValidator**: Business rule validation

#### Error Handling
- **ErrorManager**: Error collection and management
- **ErrorFormatter**: Error message formatting and localization
- **ErrorRecovery**: Error recovery and correction suggestions
- **ErrorReporting**: Error reporting and analytics

#### Utilities
- **ValidationUtils**: Validation utility functions
- **FormatUtils**: Format checking and validation utilities
- **ErrorUtils**: Error handling utilities
- **PerformanceUtils**: Performance monitoring utilities

## Project Structure

```
src/main/java/com/sanda/checker/
├── validation/             # Validation engine
│   ├── engine/            # Core validation engine
│   ├── rules/             # Validation rules
│   ├── context/           # Validation context
│   └── result/            # Validation results
├── validator/             # Validators
│   ├── field/             # Field validators
│   ├── form/              # Form validators
│   ├── file/              # File validators
│   └── business/          # Business validators
├── error/                 # Error handling
│   ├── manager/           # Error managers
│   ├── formatter/         # Error formatters
│   ├── recovery/          # Error recovery
│   └── reporting/         # Error reporting
├── util/                  # Utilities
│   ├── validation/        # Validation utilities
│   ├── format/            # Format utilities
│   ├── error/             # Error utilities
│   └── performance/       # Performance utilities
└── config/                # Configuration
    ├── validation/        # Validation configuration
    └── rules/             # Rule configuration
```

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Build Configuration
The checker module supports multiple build variants:
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

### Dependencies
Key dependencies include:
- **Android Support**: Support library components
- **JUnit & Mockito**: Testing frameworks
- **Custom Validation**: Internal validation framework

## Usage

### Basic Integration

#### 1. Add Dependency
```gradle
implementation project(':checker')
```

#### 2. Initialize Validator Manager
```kotlin
val validatorManager = ValidatorManager.Builder()
    .enableRealTimeValidation(true)
    .enableErrorRecovery(true)
    .setValidationMode(ValidationMode.STRICT)
    .build()

validatorManager.initialize()
```

#### 3. Create Validation Rules
```kotlin
val emailRule = ValidationRule.Builder()
    .field("email")
    .required(true)
    .pattern(Patterns.EMAIL_ADDRESS)
    .maxLength(100)
    .build()

val phoneRule = ValidationRule.Builder()
    .field("phone")
    .required(true)
    .pattern(Patterns.PHONE)
    .minLength(10)
    .maxLength(15)
    .build()
```

#### 4. Perform Validation
```kotlin
val formData = mapOf(
    "email" to "user@example.com",
    "phone" to "+1234567890"
)

val result = validatorManager.validate(formData, listOf(emailRule, phoneRule))

when (result) {
    is ValidationResult.Success -> {
        // Data is valid
        val validData = result.data
    }
    is ValidationResult.Error -> {
        // Handle validation errors
        val errors = result.errors
        errors.forEach { error ->
            // Process each error
        }
    }
}
```

### Field Validation

#### 1. Individual Field Validation
```kotlin
val emailValidator = FieldValidator()
emailValidator.setRule(emailRule)

val result = emailValidator.validate("user@example.com")
when (result) {
    is ValidationResult.Success -> {
        // Field is valid
    }
    is ValidationResult.Error -> {
        // Field has errors
        val fieldErrors = result.errors
    }
}
```

#### 2. Real-time Validation
```kotlin
emailValidator.validateRealTime("user@example.com") { result ->
    when (result) {
        is ValidationResult.Success -> {
            // Update UI to show valid state
        }
        is ValidationResult.Error -> {
            // Update UI to show error state
            val errorMessage = result.errors.first().message
        }
    }
}
```

### Form Validation

#### 1. Complete Form Validation
```kotlin
val formValidator = FormValidator()
formValidator.addRules(listOf(emailRule, phoneRule, nameRule))

val formData = mapOf(
    "email" to "user@example.com",
    "phone" to "+1234567890",
    "name" to "John Doe"
)

val result = formValidator.validate(formData)
when (result) {
    is ValidationResult.Success -> {
        // Form is valid, proceed with submission
    }
    is ValidationResult.Error -> {
        // Form has errors, display to user
        val formErrors = result.errors
    }
}
```

#### 2. Batch Validation
```kotlin
val batchValidator = BatchValidator()
batchValidator.addValidator(formValidator)

val batchData = listOf(formData1, formData2, formData3)
val results = batchValidator.validateBatch(batchData)

results.forEach { result ->
    when (result) {
        is ValidationResult.Success -> {
            // Handle valid data
        }
        is ValidationResult.Error -> {
            // Handle invalid data
        }
    }
}
```

## API Reference

### ValidatorManager
Main class for validation operations.

#### Methods
- `initialize()`: Initialize validation manager
- `validate(data: Map<String, Any>, rules: List<ValidationRule>)`: Validate data
- `validateRealTime(value: String, callback: ValidationCallback)`: Real-time validation
- `addRule(rule: ValidationRule)`: Add validation rule
- `removeRule(ruleId: String)`: Remove validation rule

### ValidationRule
Validation rule definition.

#### Properties
- `field: String`: Field name to validate
- `required: Boolean`: Whether field is required
- `pattern: Pattern`: Regex pattern for validation
- `minLength: Int`: Minimum length
- `maxLength: Int`: Maximum length
- `customValidator: CustomValidator`: Custom validation function

### ValidationResult
Validation result container.

#### Types
- `Success(data: Map<String, Any>)`: Validation successful
- `Error(errors: List<ValidationError>)`: Validation failed with errors

### ValidationError
Individual validation error.

#### Properties
- `field: String`: Field that failed validation
- `message: String`: Error message
- `code: String`: Error code
- `suggestion: String?`: Correction suggestion

## Configuration

### Validation Configuration
```kotlin
val config = ValidationConfig.Builder()
    .enableRealTimeValidation(true)
    .enableErrorRecovery(true)
    .setValidationMode(ValidationMode.STRICT)
    .setErrorFormat(ErrorFormat.USER_FRIENDLY)
    .build()
```

### Rule Configuration
```kotlin
val ruleConfig = RuleConfig.Builder()
    .setDefaultRequired(true)
    .setDefaultMaxLength(255)
    .setCustomValidators(customValidators)
    .build()
```

### Error Configuration
```kotlin
val errorConfig = ErrorConfig.Builder()
    .setErrorFormat(ErrorFormat.DETAILED)
    .setLocalization(true)
    .setErrorRecovery(true)
    .build()
```

## Testing

### Unit Tests
- **Location**: `src/test/java/`
- **Coverage**: Business logic and validation rules
- **Frameworks**: JUnit 4, Mockito

### Integration Tests
- **Location**: `src/androidTest/java/`
- **Coverage**: Validation integration and error handling
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

## Error Handling

### Validation Errors
```kotlin
sealed class ValidationError {
    data class RequiredError(val field: String) : ValidationError()
    data class PatternError(val field: String, val pattern: String) : ValidationError()
    data class LengthError(val field: String, val min: Int, val max: Int) : ValidationError()
    data class CustomError(val field: String, val message: String) : ValidationError()
}
```

### Error Handling Example
```kotlin
when (result) {
    is ValidationResult.Error -> {
        result.errors.forEach { error ->
            when (error) {
                is ValidationError.RequiredError -> {
                    // Handle required field error
                }
                is ValidationError.PatternError -> {
                    // Handle pattern validation error
                }
                is ValidationError.LengthError -> {
                    // Handle length validation error
                }
                is ValidationError.CustomError -> {
                    // Handle custom validation error
                }
            }
        }
    }
}
```

## Performance Optimization

### Validation Optimization
- **Lazy Validation**: Validate only when needed
- **Caching**: Cache validation results
- **Batch Processing**: Efficient batch validation
- **Parallel Processing**: Parallel validation for large datasets

### Memory Management
- **Object Pooling**: Reuse validation objects
- **Stream Processing**: Stream-based data processing
- **Garbage Collection**: Proper cleanup and garbage collection

### Performance Monitoring
- **Validation Metrics**: Track validation performance
- **Error Analytics**: Monitor error patterns
- **Performance Profiling**: Identify performance bottlenecks

## Customization

### Custom Validators
```kotlin
class CustomEmailValidator : CustomValidator {
    override fun validate(value: String): ValidationResult {
        return if (isValidEmail(value)) {
            ValidationResult.Success(value)
        } else {
            ValidationResult.Error(listOf(
                ValidationError.CustomError("email", "Invalid email format")
            ))
        }
    }
}
```

### Custom Error Messages
```kotlin
val customMessages = mapOf(
    "email.required" to "Email address is required",
    "email.invalid" to "Please enter a valid email address",
    "phone.required" to "Phone number is required",
    "phone.invalid" to "Please enter a valid phone number"
)
```

## Troubleshooting

### Common Issues
1. **Validation Not Working**: Check validation rules and configuration
2. **Performance Issues**: Monitor validation performance and optimize
3. **Error Messages**: Verify error message configuration and localization
4. **Custom Validators**: Ensure custom validators are properly implemented

### Debugging
- Enable validation logging in `ValidatorManager.kt`
- Check validation rules and configuration
- Monitor validation performance metrics
- Verify error handling and recovery

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