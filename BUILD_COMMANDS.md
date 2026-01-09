# TruckDoc Mobile - Build Commands Quick Reference

## 🚀 **Quick Build Commands**

### **Build Main App Only**
```bash
# Debug build
./gradlew :app:assembleDebug

# Release build
./gradlew :app:assembleRelease

# Clean and build
./gradlew clean :app:assembleRelease
```

### **Build App Updater Only**
```bash
# Debug build
./gradlew :app-updater:assembleDebug

# Release build
./gradlew :app-updater:assembleRelease

# Clean and build
./gradlew clean :app-updater:assembleRelease
```

### **Build Both Apps**
```bash
# Debug builds
./gradlew :app:assembleDebug :app-updater:assembleDebug

# Release builds
./gradlew :app:assembleRelease :app-updater:assembleRelease

# Clean and build both
./gradlew clean :app:assembleRelease :app-updater:assembleRelease
```

### **Build All Modules**
```bash
# Build everything
./gradlew build

# Build with tests
./gradlew build test

# Build without tests
./gradlew assemble
```

## 📱 **APK Locations**

### **Main App APK**
```
truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk
truckdoc-client-m2/application/build/outputs/apk/debug/application-debug.apk
```

### **App Updater APK**
```
app-updater/build/outputs/apk/release/app-updater-release.apk
app-updater/build/outputs/apk/debug/app-updater-debug.apk
```

## 🔧 **Development Commands**

### **Install on Device**
```bash
# Install main app
adb install truckdoc-client-m2/application/build/outputs/apk/debug/application-debug.apk

# Install updater app
adb install app-updater/build/outputs/apk/debug/app-updater-debug.apk

# Install both
adb install truckdoc-client-m2/application/build/outputs/apk/debug/application-debug.apk
adb install app-updater/build/outputs/apk/debug/app-updater-debug.apk
```

### **Uninstall Apps**
```bash
# Uninstall main app
adb uninstall com.sanda.truckdoc.client

# Uninstall updater app
adb uninstall com.sanda.truckdoc.updater
```

### **Run Tests**
```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :app:test
./gradlew :app-updater:test

# Run with coverage
./gradlew test jacocoTestReport
```

## 🛠️ **Troubleshooting Commands**

### **Clean Build**
```bash
# Clean everything
./gradlew clean

# Clean specific module
./gradlew :app:clean
./gradlew :app-updater:clean

# Clean and rebuild
./gradlew clean build
```

### **Check Dependencies**
```bash
# Show dependency tree
./gradlew dependencies

# Show specific module dependencies
./gradlew :app:dependencies
./gradlew :app-updater:dependencies

# Check for dependency conflicts
./gradlew dependencyInsight --dependency com.google.android.material
```

### **Verify APK**
```bash
# Check APK info
aapt dump badging truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk

# Verify APK signature
jarsigner -verify -verbose -certs truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk

# Check APK size
ls -lh truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk
```

### **Debug Build Issues**
```bash
# Build with stack trace
./gradlew :app:assembleRelease --stacktrace

# Build with debug info
./gradlew :app:assembleRelease --debug

# Build with info
./gradlew :app:assembleRelease --info

# Show build scan
./gradlew :app:assembleRelease --scan
```

## 🔍 **Inspection Commands**

### **Project Information**
```bash
# Show all projects
./gradlew projects

# Show project properties
./gradlew properties

# Show build environment
./gradlew buildEnvironment
```

### **Task Information**
```bash
# Show all tasks
./gradlew tasks

# Show specific module tasks
./gradlew :app:tasks
./gradlew :app-updater:tasks

# Show task dependencies
./gradlew :app:assembleRelease --dry-run
```

### **Version Information**
```bash
# Show Gradle version
./gradlew --version

# Show Android Gradle Plugin version
./gradlew :app:androidDependencies

# Show Kotlin version
./gradlew :app:kotlinVersion
```

## 📊 **Analysis Commands**

### **Lint Analysis**
```bash
# Run lint on main app
./gradlew :app:lintRelease

# Run lint on updater app
./gradlew :app-updater:lintRelease

# Run lint on all modules
./gradlew lint
```

### **Code Coverage**
```bash
# Generate coverage report
./gradlew jacocoTestReport

# View coverage report
open truckdoc-client-m2/application/build/reports/jacoco/test/html/index.html
```

### **APK Analysis**
```bash
# Analyze APK size
./gradlew :app:assembleRelease
./gradlew :app:analyzeReleaseApk

# Show APK contents
unzip -l truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk
```

## 🚀 **Jenkins Commands**

### **Local Jenkins Build**
```bash
# Run Jenkins build script
/var/lib/jenkins/build-truckdoc.sh

# Health check
/var/lib/jenkins/health-check.sh

# Check Jenkins environment
sudo -u jenkins /var/lib/jenkins/health-check.sh
```

### **Jenkins Pipeline**
```bash
# Run pipeline locally (if Jenkins CLI available)
java -jar jenkins-cli.jar -s http://localhost:8080 build truckdoc-mobile-build

# Check pipeline status
java -jar jenkins-cli.jar -s http://localhost:8080 console truckdoc-mobile-build
```

## 🔧 **Configuration Commands**

### **Update Dependencies**
```bash
# Check for dependency updates
./gradlew dependencyUpdates

# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.6

# Update Android Gradle Plugin
# Edit build.gradle files manually
```

### **Keystore Management**
```bash
# List keystore contents
keytool -list -v -keystore truckdoc-release-key.keystore.jks

# Create new keystore
keytool -genkey -v -keystore truckdoc-release-key.keystore.jks \
  -alias truckdoc-key \
  -keyalg RSA -keysize 2048 -validity 10000

# Change keystore password
keytool -keypasswd -alias truckdoc-key -keystore truckdoc-release-key.keystore.jks
```

## 📱 **Device Commands**

### **Device Management**
```bash
# List connected devices
adb devices

# Install APK
adb install -r path/to/app.apk

# Uninstall app
adb uninstall com.package.name

# Clear app data
adb shell pm clear com.package.name
```

### **Logs**
```bash
# View app logs
adb logcat | grep "TruckDoc"

# View updater logs
adb logcat | grep "TruckDocUpdater"

# Clear logs
adb logcat -c
```

## 🎯 **Common Build Scenarios**

### **Quick Development Build**
```bash
./gradlew :app:assembleDebug :app-updater:assembleDebug
adb install truckdoc-client-m2/application/build/outputs/apk/debug/application-debug.apk
adb install app-updater/build/outputs/apk/debug/app-updater-debug.apk
```

### **Production Release Build**
```bash
./gradlew clean
./gradlew :app:assembleRelease :app-updater:assembleRelease
./gradlew :app:lintRelease :app-updater:lintRelease
```

### **Jenkins Build**
```bash
./gradlew clean
./gradlew :app:assembleRelease :app-updater:assembleRelease
# Jenkins will handle artifact archiving and deployment
```

### **Emergency Hotfix**
```bash
# Quick build for hotfix
./gradlew :app:assembleRelease --parallel
adb install -r truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk
```

## ⚠️ **Troubleshooting Tips**

### **Build Fails**
1. Clean and rebuild: `./gradlew clean build`
2. Check dependencies: `./gradlew dependencies`
3. Update Gradle: `./gradlew wrapper --gradle-version 8.6`
4. Check Android SDK: `sdkmanager --list`

### **APK Won't Install**
1. Check device compatibility
2. Verify APK signature: `jarsigner -verify app.apk`
3. Check APK info: `aapt dump badging app.apk`
4. Clear device cache: `adb shell pm clear com.package.name`

### **Performance Issues**
1. Use parallel builds: `./gradlew build --parallel`
2. Enable build cache: `./gradlew build --build-cache`
3. Use Gradle daemon: `./gradlew --daemon`
4. Profile build: `./gradlew build --profile`

This quick reference covers the most common build scenarios and troubleshooting steps for the TruckDoc mobile project. 
 