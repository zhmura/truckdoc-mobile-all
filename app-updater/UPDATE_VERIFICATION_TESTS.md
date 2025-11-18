# Update Verification Tests

## Test Setup

### Prerequisites
1. Android device or emulator (API 26+)
2. Both APKs built and ready:
   - `truckdoc-client-v1.0-defaultClient.apk`
   - `truckdoc-updater-v1.0.apk`
3. GitHub repository with releases configured
4. Internet connection

### Initial Installation
```bash
# Install both apps
adb install truckdoc-client-v1.0-defaultClient.apk
adb install truckdoc-updater-v1.0.apk

# Verify installation
adb shell pm list packages | grep truckdoc
# Should show:
# package:com.sanda.truckdoc.client.default
# package:com.sanda.truckdoc.updater
```

## Test 1: Manual Update Check

### Objective
Verify updater can check GitHub for updates on demand.

### Steps
1. Open TruckDoc Updater app
2. Tap "Check for Updates" button
3. Observe status messages

### Expected Results
✅ Shows "Checking for updates..."
✅ Displays current versions:
   - Client: 1.0
   - Updater: 1.0
✅ Shows "No updates available" OR "Update available" based on GitHub release
✅ Last check time updates

### Verification
```bash
# Check app versions
adb shell dumpsys package com.sanda.truckdoc.client.default | grep versionName
adb shell dumpsys package com.sanda.truckdoc.updater | grep versionName
```

## Test 2: Client App Update

### Setup
1. Create GitHub release `v1.1.0`
2. Upload `truckdoc-client-v1.1-defaultClient.apk`
3. Upload `truckdoc-updater-v1.0.apk` (same version)

### Steps
1. Open updater
2. Tap "Check for Updates"
3. Observe update detection
4. Tap "Download" or wait if auto-download enabled
5. When download completes, tap "Install"

### Expected Results
✅ Detects client app update (1.0 → 1.1)
✅ Shows "No update" for updater
✅ Downloads APK in background
✅ Shows download progress notification
✅ Auto-opens installation prompt
✅ After install, client app version is 1.1
✅ Updater continues running normally

### Verification
```bash
# Monitor download
adb logcat | grep "TruckDoc"

# Check downloaded APK
adb shell ls -la /data/data/com.sanda.truckdoc.updater/files/

# Verify new version after install
adb shell dumpsys package com.sanda.truckdoc.client.default | grep versionName
# Should show: versionName=1.1
```

## Test 3: Updater Self-Update

### Setup
1. Create GitHub release `v1.2.0`
2. Upload `truckdoc-client-v1.1-defaultClient.apk` (same as before)
3. Upload `truckdoc-updater-v1.2.apk` (new version)

### Steps
1. Open updater (currently v1.0)
2. Tap "Check for Updates"
3. Observe BOTH updates detected:
   - Client: 1.1 → 1.2 (if available)
   - Updater: 1.0 → 1.2
4. Download updater update
5. Install updater update
6. Observe updater restart

### Expected Results
✅ Detects updater self-update (1.0 → 1.2)
✅ Downloads updater APK
✅ Auto-opens installation prompt
✅ After install, updater RESTARTS automatically
✅ New updater version is 1.2
✅ Can still check/update client app

### Verification
```bash
# Check updater version after restart
adb shell dumpsys package com.sanda.truckdoc.updater | grep versionName
# Should show: versionName=1.2

# Verify updater is running
adb shell ps | grep truckdoc.updater
```

## Test 4: Automatic Background Checks

### Setup
1. Enable auto-check in Settings
2. Set interval to 6 hours (default)
3. Close updater app

### Steps
1. Wait or fast-forward time:
   ```bash
   # Fast-forward time (requires root)
   adb shell date -s "20251119.120000"
   ```
2. Trigger WorkManager:
   ```bash
   adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
   ```
3. Check notifications

### Expected Results
✅ UpdateCheckWorker runs after 6 hours
✅ Checks GitHub for updates
✅ Shows notification if update available
✅ No notification if no updates
✅ Updates SharedPreferences with last check time

### Verification
```bash
# Check WorkManager status
adb shell dumpsys jobscheduler | grep UpdateCheck

# Check SharedPreferences
adb shell run-as com.sanda.truckdoc.updater \
  cat /data/data/com.sanda.truckdoc.updater/shared_prefs/app_updater_preferences.xml
```

## Test 5: Auto-Download Feature

### Setup
1. Open updater Settings
2. Enable "Auto-download updates"
3. Ensure WiFi is connected (if WiFi-only enabled)

### Steps
1. Create new GitHub release with updates
2. Wait for background check OR tap "Check for Updates"
3. Do NOT manually tap download
4. Observe automatic download

### Expected Results
✅ Update detected
✅ Download starts automatically
✅ Progress notification shown
✅ Download completes
✅ Installation prompt auto-opens
✅ User only needs to tap "Install"

### Verification
```bash
# Monitor auto-download
adb logcat | grep -E "Download|UpdateCheck"

# Check notification
adb shell dumpsys notification | grep truckdoc
```

## Test 6: Both Apps Update Simultaneously

### Setup
1. Create GitHub release with BOTH apps updated:
   - `truckdoc-client-v2.0-defaultClient.apk`
   - `truckdoc-updater-v2.0.apk`

### Steps
1. Check for updates
2. Observe both updates detected
3. Choose which to update first (or auto-download both)
4. Install client app update first
5. Then install updater update

### Expected Results
✅ Both updates shown in UI:
   - Client: current → 2.0
   - Updater: current → 2.0
✅ Can download both simultaneously
✅ Can install in any order
✅ Both apps work after updates
✅ Updater can still update client after self-update

## Test 7: Network Failure Handling

### Setup
1. Enable airplane mode or disable internet

### Steps
1. Open updater
2. Tap "Check for Updates"
3. Observe error handling

### Expected Results
✅ Shows "Failed to check for updates" error
✅ Does not crash
✅ Can retry when network restored
✅ Previous version info still displayed

### Verification
```bash
# Disable network
adb shell svc wifi disable
adb shell svc data disable

# Check updater behavior
# (Open app and test)

# Re-enable network
adb shell svc wifi enable
adb shell svc data enable
```

## Test 8: Download Interruption Recovery

### Setup
1. Start large APK download
2. Interrupt download (kill app, disable network, etc.)

### Steps
1. Start download
2. Wait for partial download (check notification)
3. Force stop app: `adb shell am force-stop com.sanda.truckdoc.updater`
4. Restart app
5. Check for updates again

### Expected Results
✅ Shows update still available
✅ Can restart download
✅ No corrupted APK files left
✅ New download completes successfully

## Test 9: Install Permission Check

### Setup
1. Fresh install of updater
2. First-time launch

### Steps
1. Open updater
2. Check for updates
3. Download update
4. Attempt install

### Expected Results
✅ If "Install from this source" not granted:
   - Installation fails with permission error
   - Shows helpful error message
✅ If permission granted:
   - Installation prompt appears normally

### Verification
```bash
# Check if permission granted
adb shell appops get com.sanda.truckdoc.updater REQUEST_INSTALL_PACKAGES
# Should show: REQUEST_INSTALL_PACKAGES: allow
```

## Test 10: GitHub API Rate Limiting

### Setup
1. Make multiple rapid update checks (>60 in an hour)

### Steps
1. Repeatedly tap "Check for Updates"
2. Monitor for rate limit errors

### Expected Results
✅ First 60 requests succeed (GitHub unauthenticated limit)
✅ After rate limit:
   - Shows appropriate error message
   - Handles gracefully
   - Retries after rate limit reset

### Verification
```bash
# Check rate limit status
curl -I https://api.github.com/rate_limit
```

## Success Criteria Summary

### ✅ Updater CAN:
- [x] Check for updates from GitHub Releases
- [x] Detect client app updates
- [x] Detect updater self-updates
- [x] Detect both apps need updates simultaneously
- [x] Download APKs in background
- [x] Show download progress
- [x] Auto-launch installer when complete
- [x] Install client app updates (with user tap)
- [x] Install updater self-updates (with user tap)
- [x] Run automatic background checks
- [x] Auto-download when enabled
- [x] Handle network failures gracefully
- [x] Continue working after self-update
- [x] Update both apps independently

### ⚠️ Requires User Action:
- [ ] Tapping "Install" on system prompt
- [ ] Granting "Install from this source" permission (one-time)

### ❌ Cannot Do (Without Root/Device Owner):
- [ ] Install without ANY user interaction
- [ ] Bypass Android security confirmation
- [ ] Install while device is locked

## Performance Metrics

- **Update check time**: < 3 seconds
- **Download speed**: Network dependent
- **Background check interval**: 6 hours (configurable)
- **Battery impact**: Minimal (WorkManager optimized)
- **Storage usage**: ~10MB per APK temporarily

## Troubleshooting

### Updates Not Detected
- Check internet connection
- Verify GitHub release exists
- Check APK naming matches patterns
- Verify GitHub API accessible

### Download Fails
- Check storage space
- Verify network stability
- Check download URL valid
- Try manual download to verify

### Install Fails
- Check "Install from this source" permission
- Verify APK not corrupted
- Check APK signature valid
- Ensure enough storage space

### Updater Doesn't Restart After Self-Update
- Normal behavior - user reopens app
- Android doesn't auto-restart apps after install
- User launches updater to continue using it

