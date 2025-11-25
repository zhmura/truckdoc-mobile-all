# TruckDoc Mobile - Complete Deployment Guide

## Overview

This guide covers the complete deployment process for TruckDoc Mobile, including Jenkins builds, GitHub releases, and app distribution.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    GitHub Repository                         │
│              zhmura/truckdoc-mobile-all                      │
│                                                              │
│  Source Code → Jenkins Build → GitHub Releases              │
└─────────────────────────────────────────────────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ GitHub Releases │
                    │   (Public)      │
                    ├─────────────────┤
                    │ truckdoc-client │
                    │ truckdoc-updater│
                    └─────────────────┘
                              ↓
                    ┌─────────────────┐
                    │  App Updater    │
                    │  (on device)    │
                    ├─────────────────┤
                    │ • Checks GitHub │
                    │ • Downloads APKs│
                    │ • Installs apps │
                    └─────────────────┘
                              ↓
                    ┌─────────────────┐
                    │ TruckDoc Client │
                    │  (main app)     │
                    └─────────────────┘
```

## Prerequisites

### Development Environment

- ✅ macOS with Android Studio
- ✅ JDK 17 (Amazon Corretto)
- ✅ Android SDK (API 34)
- ✅ Git
- ✅ Jenkins (Homebrew)
- ✅ GitHub CLI (`gh`)

### Jenkins Configuration

- ✅ JDK 17 configured
- ✅ Android SDK accessible
- ✅ GitHub credentials configured
- ✅ Pipeline job created

### GitHub Repository

- ✅ Repository: `zhmura/truckdoc-mobile-all`
- ✅ Branch: `truckdoc-39-android-31`
- ✅ Releases enabled
- ✅ Public or private (recommend public releases)

## Build Process

### Step 1: Prepare Release

```bash
# 1. Ensure code is ready
git checkout truckdoc-39-android-31
git pull origin truckdoc-39-android-31

# 2. Update version numbers in build.gradle (optional - Jenkins can override)
# Or use Jenkins parameters

# 3. Commit any final changes
git add .
git commit -m "Prepare release v1.2.3"
git push origin truckdoc-39-android-31

# 4. Create git tag (optional - can use Jenkins params instead)
git tag -a v1.2.3 -m "Release 1.2.3"
git push origin v1.2.3
```

### Step 2: Jenkins Build

#### Option A: Build with Git Tag

1. Push git tag: `v1.2.3`
2. Jenkins parameters:
   - `CLIENT_VERSION`: (empty - uses tag)
   - `UPDATER_VERSION`: (empty - matches client)
   - `PUBLISH_GITHUB_RELEASE`: `false` (build only)
3. Click "Build Now"

#### Option B: Build with Parameters

1. Jenkins parameters:
   - `CLIENT_VERSION`: `1.2.3`
   - `UPDATER_VERSION`: `1.0.5`
   - `PUBLISH_GITHUB_RELEASE`: `false`
2. Click "Build with Parameters"

#### Build Output

Jenkins will:
1. ✅ Extract versions
2. ✅ Build client APK: `truckdoc-client-v1.2.3.apk`
3. ✅ Build updater APK: `truckdoc-updater-v1.0.5.apk`
4. ✅ Verify versions match
5. ✅ Archive APKs as artifacts

### Step 3: Test APKs

Download artifacts from Jenkins and test:

```bash
# Install on test device
adb install truckdoc-updater-v1.0.5.apk
adb install truckdoc-client-v1.2.3.apk

# Verify versions
adb shell dumpsys package com.sanda.truckdoc.updater | grep versionName
adb shell dumpsys package com.sanda.truckdoc.client.default | grep versionName

# Test updater
# - Open updater app
# - Tap "Check for Updates"
# - Verify it connects to GitHub
```

### Step 4: Publish to GitHub

Once testing passes, publish the release:

1. Jenkins parameters:
   - `CLIENT_VERSION`: `1.2.3`
   - `UPDATER_VERSION`: `1.0.5`
   - `PUBLISH_GITHUB_RELEASE`: `true` ✅
   - `RELEASE_TAG`: `v1.2.3`
   - `RELEASE_TITLE`: `TruckDoc Mobile v1.2.3`
   - `RELEASE_NOTES`: Your changelog

2. Click "Build with Parameters"

3. Jenkins will:
   - Build both APKs
   - Create GitHub release `v1.2.3`
   - Upload both APKs
   - Add version info to release notes

### Step 5: Verify GitHub Release

```bash
# Check release exists
gh release view v1.2.3

# Or visit:
https://github.com/zhmura/truckdoc-mobile-all/releases/tag/v1.2.3

# Verify assets:
# - truckdoc-client-v1.2.3.apk
# - truckdoc-updater-v1.0.5.apk
```

## Distribution Methods

### Method 1: Updater-Only Distribution (Recommended)

**Distribute only the updater APK:**

1. **Download from Jenkins/GitHub:**
   - `truckdoc-updater-v1.0.5.apk`

2. **Distribute to users via:**
   - Email attachment
   - Company portal
   - USB transfer
   - MDM/EMM system

3. **User installs updater:**
   ```bash
   # User side
   # 1. Enable "Install from unknown sources"
   # 2. Install truckdoc-updater-v1.0.5.apk
   # 3. Open updater
   # 4. Updater downloads and installs client automatically
   ```

4. **Benefits:**
   - ✅ Single APK to distribute
   - ✅ Always installs latest client
   - ✅ Simplified deployment
   - ✅ Automatic updates

### Method 2: Both APKs Distribution

**Distribute both APKs together:**

1. **Package both APKs:**
   ```bash
   # Create distribution package
   mkdir truckdoc-mobile-v1.2.3
   cp truckdoc-client-v1.2.3.apk truckdoc-mobile-v1.2.3/
   cp truckdoc-updater-v1.0.5.apk truckdoc-mobile-v1.2.3/
   cp INSTALL_INSTRUCTIONS.md truckdoc-mobile-v1.2.3/
   zip -r truckdoc-mobile-v1.2.3.zip truckdoc-mobile-v1.2.3/
   ```

2. **Distribute package**

3. **Installation order:**
   - Install updater first
   - Then install client
   - Or let updater install client

### Method 3: MDM/EMM Deployment

**For enterprise with Mobile Device Management:**

1. **Upload APKs to MDM:**
   - Add both APKs to MDM console
   - Configure deployment policies

2. **Silent deployment:**
   - MDM pushes APKs to devices
   - Installs silently (if Device Owner)
   - Updater keeps apps updated

3. **Configure updater:**
   - Use Admin Settings to point to company repo
   - Or use default public releases

### Method 4: Direct GitHub Links

**Share download links:**

```
Client: https://github.com/zhmura/truckdoc-mobile-all/releases/download/v1.2.3/truckdoc-client-v1.2.3.apk

Updater: https://github.com/zhmura/truckdoc-mobile-all/releases/download/v1.2.3/truckdoc-updater-v1.0.5.apk
```

Users download and install from browser.

## Update Process

### Automatic Updates (Recommended)

**Once deployed, updates are automatic:**

1. **Build new version in Jenkins:**
   - CLIENT_VERSION: `1.2.4`
   - UPDATER_VERSION: `1.0.5` (unchanged)
   - PUBLISH_GITHUB_RELEASE: `true`

2. **Jenkins publishes to GitHub**

3. **Updater checks automatically:**
   - Every 6 hours (background)
   - Or when user opens app
   - Or manual "Check for Updates"

4. **User experience:**
   - Notification: "Update available"
   - Tap notification → Opens updater
   - Tap "Download Client App Update"
   - Downloads in background
   - Installer opens automatically
   - User taps "Install"
   - Done!

### Manual Updates

Users can manually check:

1. Open TruckDoc Updater
2. Tap "Check for Updates"
3. See available updates
4. Tap download buttons
5. Install when prompted

## Version Management

### Independent Versioning

Client and updater can have different versions:

```
Release v1.2.3:
- Client: 1.2.3 (new features)
- Updater: 1.0.0 (unchanged)

Release v1.2.4:
- Client: 1.2.4 (hotfix)
- Updater: 1.0.0 (unchanged)

Release v1.2.4-updater-1.1:
- Client: 1.2.4 (unchanged)
- Updater: 1.1.0 (new features)
```

### Version Code Formula

```
versionCode = (major * 10000) + (minor * 100) + patch

Examples:
- 1.0.0 → 10000
- 1.2.3 → 10203
- 2.5.7 → 20507
```

## Security

### APK Signing

- ✅ Both APKs signed with release keystore
- ✅ Keystore: `truckdoc-release-key.keystore.jks`
- ✅ Signature verified by Android before installation
- ✅ Only signed updates can be installed

### GitHub Releases

- ✅ HTTPS downloads (encrypted)
- ✅ GitHub CDN (DDoS protected)
- ✅ Public releases are safe (APK signing protects integrity)
- ✅ No authentication needed

### Network Security

- ✅ HTTPS-only enforced
- ✅ Cleartext traffic blocked
- ✅ Certificate validation

## Monitoring

### Check Update Status

```bash
# Check if updater is installed
adb shell pm list packages | grep updater

# Check updater version
adb shell dumpsys package com.sanda.truckdoc.updater | grep versionName

# Check client version
adb shell dumpsys package com.sanda.truckdoc.client.default | grep versionName

# Check last update check time
adb shell run-as com.sanda.truckdoc.updater \
  cat /data/data/com.sanda.truckdoc.updater/shared_prefs/app_updater_preferences.xml | grep last_check
```

### Monitor Update Activity

```bash
# Watch updater logs
adb logcat | grep -E "TruckDoc|Updater|GitHub"

# Check WorkManager status
adb shell dumpsys jobscheduler | grep UpdateCheck

# Check notifications
adb shell dumpsys notification | grep truckdoc
```

## Troubleshooting

### Build Failures

**Problem:** Jenkins build fails

**Check:**
1. JDK 17 configured
2. Android SDK accessible
3. Keystore file present
4. Network connectivity
5. Gradle daemon not stuck

**Solution:**
```bash
# On Jenkins agent
./gradlew --stop
./gradlew clean
```

### Update Not Detected

**Problem:** Updater doesn't see new release

**Check:**
1. ✅ GitHub release published (not draft)
2. ✅ APK filenames match patterns
3. ✅ Version codes are higher
4. ✅ Network connectivity
5. ✅ GitHub API accessible

**Debug:**
```bash
# Test GitHub API
curl https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest

# Check rate limits
curl https://api.github.com/rate_limit
```

### Installation Fails

**Problem:** APK won't install

**Check:**
1. ✅ "Install from unknown sources" enabled
2. ✅ Enough storage space
3. ✅ APK not corrupted
4. ✅ Signature matches (for updates)

**Solution:**
```bash
# Verify APK
aapt dump badging app.apk

# Check signature
jarsigner -verify -verbose app.apk

# Check storage
adb shell df -h
```

## Rollback Process

### Rollback to Previous Version

If new version has issues:

1. **Find previous release:**
   ```bash
   gh release list
   ```

2. **Download previous APKs:**
   ```bash
   gh release download v1.2.2
   ```

3. **Distribute previous version:**
   - Users can install older version over newer
   - Android allows downgrade if signatures match
   - Or uninstall and reinstall

4. **Or delete bad release:**
   ```bash
   gh release delete v1.2.3 --yes
   ```
   Updater will then see previous version as latest.

## Best Practices

### 1. Version Naming

```
v{major}.{minor}.{patch}
v1.0.0  - Initial release
v1.0.1  - Patch/hotfix
v1.1.0  - Minor update
v2.0.0  - Major release
```

### 2. Release Notes

Always include:
- What's new
- Bug fixes
- Known issues
- Version numbers for both apps

Example:
```markdown
## TruckDoc Mobile v1.2.3

### Client App (v1.2.3)
- New feature: Route optimization
- Fixed crash on startup
- Improved performance

### Updater App (v1.0.5)
- Enhanced UI
- Better error handling
- Fixed download progress

### Technical Details
- Client versionCode: 10203
- Updater versionCode: 10005
```

### 3. Testing Checklist

Before publishing:
- [ ] Build succeeds in Jenkins
- [ ] APK versions correct
- [ ] Client app installs and runs
- [ ] Updater installs and runs
- [ ] Updater detects updates correctly
- [ ] Download works
- [ ] Installation works
- [ ] Both apps work after update

### 4. Deployment Schedule

Recommended schedule:
- **Hotfixes:** As needed (critical bugs)
- **Patch releases:** Weekly or bi-weekly
- **Minor releases:** Monthly
- **Major releases:** Quarterly

### 5. Communication

Notify users before major updates:
- Email notification
- In-app message
- Release notes
- Training if needed

## Quick Reference

### Build Commands

```bash
# Local build with version
./gradlew clean assembleRelease \
  -PversionName=1.2.3 \
  -PversionCode=10203

# Check APK version
aapt dump badging app.apk | grep version
```

### Jenkins Build

```
Parameters:
- CLIENT_VERSION: 1.2.3
- UPDATER_VERSION: 1.0.5
- PUBLISH_GITHUB_RELEASE: true
- RELEASE_TAG: v1.2.3
```

### GitHub Release

```bash
# Create release manually
gh release create v1.2.3 \
  truckdoc-client-v1.2.3.apk \
  truckdoc-updater-v1.0.5.apk \
  --title "TruckDoc Mobile v1.2.3" \
  --notes "Release notes here"

# List releases
gh release list

# Delete release
gh release delete v1.2.3 --yes
```

### Installation

```bash
# Install via ADB
adb install truckdoc-updater-v1.0.5.apk
adb install truckdoc-client-v1.2.3.apk

# Or on device
# Settings → Security → Install from unknown sources
# Download APK → Tap to install
```

## Support

### Documentation

- `VERSIONING.md` - Version strategy
- `INDEPENDENT_VERSIONING.md` - Separate app versions
- `app-updater/GITHUB_INTEGRATION.md` - GitHub integration
- `app-updater/SILENT_UPDATES.md` - Update mechanisms
- `app-updater/SECURITY_GITHUB_RELEASES.md` - Security analysis
- `app-updater/ADMIN_SETTINGS.md` - Admin configuration
- `app-updater/UPDATE_VERIFICATION_TESTS.md` - Test suite

### Key Files

- `ci/jenkins-github-release/Jenkinsfile` - Build pipeline
- `keystore.properties` - Signing configuration
- `gradle.properties` - Build configuration
- `app-updater/src/main/java/com/sanda/truckdoc/updater/config/GitHubConfig.kt` - Update config

## Summary

### Complete Workflow

```
1. Code → Commit → Push
2. Jenkins → Build → Verify
3. GitHub → Publish → Release
4. Updater → Check → Download
5. User → Install → Done
```

### Key Features

✅ **Automated builds** - Jenkins pipeline
✅ **GitHub releases** - Public distribution
✅ **Automatic updates** - Background checks
✅ **Manual updates** - User-initiated
✅ **Self-updates** - Updater updates itself
✅ **Bootstrap install** - Updater installs client
✅ **Independent versions** - Apps version separately
✅ **Admin settings** - Custom repo configuration
✅ **Secure** - APK signing + HTTPS

### Current Status

- ✅ Jenkins pipeline working
- ✅ Both APKs building
- ✅ Version extraction from tags
- ✅ GitHub integration complete
- ✅ Updater UI functional
- ✅ Admin settings implemented
- ✅ Client download when not installed
- ✅ Ready for production deployment

**The complete deployment system is operational!** 🚀

