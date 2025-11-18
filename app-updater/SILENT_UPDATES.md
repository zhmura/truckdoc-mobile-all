# Silent Updates Implementation

## Overview

The TruckDoc Updater supports **silent updates** for both itself and the TruckDoc Client app. However, Android's security model has restrictions on silent APK installation.

## Android Silent Installation Options

### Option 1: System App (Requires Root/System Partition)
- App installed in `/system/app` or `/system/priv-app`
- Has `INSTALL_PACKAGES` permission
- ✅ Can install APKs silently
- ❌ Requires device rooting or OEM cooperation

### Option 2: Device Owner / Profile Owner (MDM)
- App is set as Device Owner via ADB or EMM
- Can use `PackageInstaller` API
- ✅ Completely silent installation
- ❌ Requires enterprise setup or factory reset

### Option 3: User Interaction (Current Implementation)
- Uses `ACTION_VIEW` intent
- Shows system installation prompt
- ✅ Works on all devices
- ❌ Requires user to tap "Install"

### Option 4: Accessibility Service (Not Recommended)
- Use accessibility to auto-click install
- ✅ No root required
- ❌ Security risk, against Play Store policies

## Current Implementation: Semi-Silent

Our updater implements a **semi-silent** approach:

### ✅ What IS Silent:
1. **Background checking** - Updates checked automatically via WorkManager
2. **Silent download** - APKs downloaded in background with notification
3. **Auto-launch installer** - Installation prompt opens automatically

### ⚠️ What Requires User Action:
1. **Install confirmation** - User must tap "Install" on system dialog
2. **Permission grant** - First-time "Install from this source" permission

## Verification: Current Capabilities

### Self-Update Flow
```kotlin
// 1. Check for updater self-update
val systemUpdate = gitHubUpdateRepository.checkForUpdates()
if (systemUpdate.updaterAppUpdate.updateAvailable) {
    // 2. Download silently in background
    downloadManager.downloadApkWithFlow(downloadUrl)
    
    // 3. Auto-launch installer (user must tap Install)
    installApk(apkFile)  // Opens system installer
    
    // 4. After installation, updater restarts automatically
}
```

### Client App Update Flow
```kotlin
// 1. Check for client app update
if (systemUpdate.clientAppUpdate.updateAvailable) {
    // 2. Download silently in background
    downloadManager.downloadApkWithFlow(downloadUrl)
    
    // 3. Auto-launch installer (user must tap Install)
    installApk(apkFile)  // Opens system installer
    
    // 4. Client app updates, updater continues running
}
```

## Making It More Silent (Options)

### Option A: Request "Install Unknown Apps" Permission Upfront

Add permission request flow in first launch:
```kotlin
// Check if we have permission
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    if (!packageManager.canRequestPackageInstalls()) {
        // Request permission
        startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            .setData(Uri.parse("package:$packageName")))
    }
}
```

### Option B: Use PackageInstaller with User Confirmation

More modern approach (API 21+):
```kotlin
fun installApkWithConfirmation(apkFile: File) {
    val packageInstaller = packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(
        PackageInstaller.SessionParams.MODE_FULL_INSTALL
    )
    
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)
    
    session.openWrite("package", 0, -1).use { output ->
        apkFile.inputStream().use { input ->
            input.copyTo(output)
        }
    }
    
    // This still shows system confirmation dialog
    session.commit(confirmationIntent)
}
```

### Option C: Enterprise Deployment with Device Owner

For enterprise/fleet deployment:
1. Set updater as Device Owner during provisioning
2. Use `PackageInstaller` with `INSTALL_PACKAGES` permission
3. Completely silent, no user interaction

```bash
# During device setup (before adding Google account)
adb shell dpm set-device-owner com.sanda.truckdoc.updater/.DeviceAdminReceiver
```

## Automatic Update Schedule

The updater already implements automatic checking:

### Background Check Schedule
```kotlin
// UpdateCheckWorker runs periodically
PeriodicWorkRequestBuilder<UpdateCheckWorker>(
    repeatInterval = 6,
    repeatIntervalTimeUnit = TimeUnit.HOURS
).build()
```

### Auto-Download Settings
- User can enable auto-download in Settings
- When enabled, downloads happen automatically
- Notification shows download progress
- Installation prompt auto-opens when complete

## Testing Silent Updates

### Test Self-Update:
1. Install `truckdoc-updater-v1.0.apk`
2. Create GitHub release with `truckdoc-updater-v1.1.apk`
3. Open updater or wait for background check
4. Update downloads automatically
5. Installation prompt opens (user taps Install)
6. Updater restarts with new version

### Test Client Update:
1. Install `truckdoc-client-v1.0.apk` and updater
2. Create GitHub release with `truckdoc-client-v1.1.apk`
3. Open updater or wait for background check
4. Update downloads automatically
5. Installation prompt opens (user taps Install)
6. Client app updates, updater stays running

## Permissions Required

### Minimal (Current):
- `INTERNET` - Check/download updates
- `REQUEST_INSTALL_PACKAGES` - Launch installer
- `ACCESS_NETWORK_STATE` - Check connectivity
- `RECEIVE_BOOT_COMPLETED` - Auto-start after reboot
- `FOREGROUND_SERVICE` - Background downloads

### For True Silent (Enterprise):
- `INSTALL_PACKAGES` (system permission)
- Device Owner or Profile Owner status

## Verification Summary

✅ **Updater CAN:**
- Check for updates automatically in background
- Download APKs silently with progress notification
- Update itself (with user tapping "Install")
- Update client app (with user tapping "Install")
- Auto-launch installer when download completes
- Continue checking and updating both apps independently

⚠️ **Updater CANNOT (without root/Device Owner):**
- Install APKs without any user interaction
- Bypass Android's security confirmation dialog
- Install updates while device is locked

## Conclusion

The updater provides the **maximum level of automation possible** without requiring:
- Device rooting
- System app privileges
- Enterprise MDM setup

For true silent updates (zero user interaction), you would need one of:
1. **System app status** (requires OEM cooperation)
2. **Device Owner/Profile Owner** (enterprise/MDM deployment)
3. **Root access** (not recommended for production)

The current implementation is the **best approach for standard consumer devices** while maintaining Android security compliance.

