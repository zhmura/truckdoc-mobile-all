# Package Conflict Error - Solution Guide

## Error Message

```
Package conflict with an existing package by the same name
```

## Root Cause

An app with package name `com.sanda.truckdoc.client.default` is already installed, but it was signed with a **different signing key** than the APK you're trying to install.

Android security prevents installing an APK over an existing app unless:
1. Package names match exactly
2. **AND** signing keys match exactly

## Diagnosis

### Check Installed App

```bash
# Check if client app is installed
adb shell pm list packages | grep truckdoc

# Check installed app signature
adb shell pm dump com.sanda.truckdoc.client.default | grep -A5 "signatures"

# Check installed version
adb shell dumpsys package com.sanda.truckdoc.client.default | grep versionName
```

### Check New APK Signature

```bash
# Extract certificate from APK
unzip -p truckdoc-client-v1.0.3.apk META-INF/*.RSA | keytool -printcert

# Or use jarsigner
jarsigner -verify -verbose -certs truckdoc-client-v1.0.3.apk
```

## Common Scenarios

### Scenario 1: Debug vs Release Build

**Problem:** Installed debug build, trying to install release (or vice versa)

**Debug builds** are signed with:
- Android debug keystore (`~/.android/debug.keystore`)
- Common across all developers

**Release builds** are signed with:
- Your production keystore (`truckdoc-release-key.keystore.jks`)
- Unique to your organization

**Solution:** Uninstall the existing app first

```bash
# Uninstall via ADB
adb uninstall com.sanda.truckdoc.client.default

# Or on device
Settings → Apps → TruckDoc Client → Uninstall
```

### Scenario 2: Different Keystores

**Problem:** Previously installed with different release keystore

**Causes:**
- Keystore was regenerated
- Different keystore used for this build
- Keystore password changed
- Using wrong keystore file

**Solution:** 

**Option A: Uninstall and reinstall**
```bash
adb uninstall com.sanda.truckdoc.client.default
adb install truckdoc-client-v1.0.3.apk
```

**Option B: Verify using correct keystore**

Check `keystore.properties`:
```properties
storePassword=#26$&1ucI2ph
keyPassword=#26$&1ucI2ph
keyAlias=truckdoc-release-key
storeFile=truckdoc-release-key.keystore.jks
```

Verify keystore exists:
```bash
ls -la truckdoc-release-key.keystore.jks
```

### Scenario 3: Old Version from Different Source

**Problem:** Old version installed from different build/source

**Solution:** Uninstall old version first

```bash
# Backup app data if needed
adb backup -f backup.ab com.sanda.truckdoc.client.default

# Uninstall
adb uninstall com.sanda.truckdoc.client.default

# Install new version
adb install truckdoc-client-v1.0.3.apk

# Restore data if needed
adb restore backup.ab
```

## Solutions

### Solution 1: Uninstall Existing App (Quickest)

**Via ADB:**
```bash
adb uninstall com.sanda.truckdoc.client.default
```

**On Device:**
1. Settings → Apps
2. Find "TruckDoc Client"
3. Tap "Uninstall"
4. Confirm

**Then install new version:**
- Via updater: Tap download button again
- Via ADB: `adb install truckdoc-client-v1.0.3.apk`

### Solution 2: Use Correct Keystore

**Verify keystore configuration:**

1. Check `keystore.properties` has correct values
2. Verify keystore file exists at root: `truckdoc-release-key.keystore.jks`
3. Rebuild with correct keystore:
   ```bash
   ./gradlew clean :app:assembleRelease
   ```

4. Verify signature:
   ```bash
   jarsigner -verify truckdoc-client-v1.0.3.apk
   # Should show: jar verified.
   ```

### Solution 3: Fresh Installation

**For clean slate:**

```bash
# 1. Uninstall all TruckDoc apps
adb uninstall com.sanda.truckdoc.client.default
adb uninstall com.sanda.truckdoc.updater

# 2. Install updater
adb install truckdoc-updater-v1.0.3.apk

# 3. Open updater
adb shell am start -n com.sanda.truckdoc.updater/.ui.MainActivity

# 4. Tap "Check for Updates"
# 5. Download and install client
# Both apps now have matching signatures ✅
```

## Prevention

### 1. Always Use Same Keystore

**For all builds:**
- ✅ Use `truckdoc-release-key.keystore.jks`
- ✅ Keep keystore secure and backed up
- ✅ Never regenerate keystore
- ✅ Use same keystore for all releases

### 2. Document Keystore Info

```
Keystore: truckdoc-release-key.keystore.jks
Alias: truckdoc-release-key
Location: Project root
Backup: [Secure location]
```

### 3. Verify Before Distribution

```bash
# Check signature before distributing
jarsigner -verify -verbose -certs truckdoc-client-v1.0.3.apk

# Compare with previous version
jarsigner -verify -verbose -certs truckdoc-client-v1.0.2.apk

# Certificates should match!
```

### 4. Test on Clean Device

Before distributing:
1. Test on device without app installed
2. Install via updater
3. Test update flow
4. Verify no conflicts

## Enhanced Error Handling in Updater

### Add Better Error Message

The updater should detect this error and show helpful message:

```kotlin
// In MainActivity.installApk()
try {
    startActivity(installIntent)
} catch (e: Exception) {
    val message = when {
        e.message?.contains("INSTALL_FAILED_UPDATE_INCOMPATIBLE") == true ||
        e.message?.contains("Package conflict") == true -> {
            "Installation failed: Signature mismatch.\n\n" +
            "An older version with different signature is installed.\n\n" +
            "Solution: Uninstall the existing TruckDoc Client app first, " +
            "then try downloading again."
        }
        else -> "Failed to install: ${e.message}"
    }
    
    Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
}
```

### Add Uninstall Helper

```kotlin
fun showUninstallDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle("Uninstall Required")
        .setMessage("To install this update, you need to uninstall the existing app first.\n\nNote: This will delete app data.")
        .setPositiveButton("Open Settings") { _, _ ->
            // Open app settings to uninstall
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:com.sanda.truckdoc.client.default")
            startActivity(intent)
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

## For Users

### User-Friendly Instructions

**If you see "Package conflict" error:**

1. **Uninstall the old TruckDoc Client:**
   - Go to Settings → Apps
   - Find "TruckDoc Client"
   - Tap "Uninstall"
   - Confirm

2. **Reinstall via Updater:**
   - Open TruckDoc Updater
   - Tap "Check for Updates"
   - Tap "Download Client App"
   - Install when prompted

3. **Done!** Both apps now have matching signatures.

**Note:** You may need to log in again and reconfigure settings.

## Technical Details

### Package Name

```
com.sanda.truckdoc.client.default
```

### Signing Configuration

From `application/build.gradle`:
```groovy
signingConfigs {
    truckdoc_sign_config {
        keyAlias 'truckdoc-release-key'
        keyPassword '#26$&1ucI2ph'
        storeFile rootProject.file('truckdoc-release-key.keystore.jks')
        storePassword '#26$&1ucI2ph'
    }
}
```

### Android Security

Android enforces:
1. **Package name uniqueness** - Only one app per package name
2. **Signature verification** - Updates must have same signature
3. **No signature changes** - Cannot change signature for installed app
4. **Uninstall required** - Must uninstall to change signature

## Summary

### Quick Fix

```bash
# Uninstall existing app
adb uninstall com.sanda.truckdoc.client.default

# Install new version
# Via updater or ADB
```

### Root Cause

- Different signing keys between installed app and new APK
- Common when switching between debug and release
- Or when keystore was regenerated

### Prevention

- ✅ Always use same keystore for all builds
- ✅ Never regenerate keystore
- ✅ Backup keystore securely
- ✅ Test on clean devices

**After uninstalling the old app, the new one will install successfully!** ✅

