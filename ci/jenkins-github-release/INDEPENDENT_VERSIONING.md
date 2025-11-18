# Independent Versioning for Client and Updater Apps

## Overview

The Jenkins pipeline now supports **independent versioning** for the TruckDoc Client app and the Updater app. This allows you to release different versions of each app as needed.

## Jenkins Build Parameters

### Version Parameters

**CLIENT_VERSION** (optional)
- Client app version (e.g., `1.2.3`)
- Leave empty to use git tag
- Example: `1.2.3` produces `truckdoc-client-v1.2.3.apk`

**UPDATER_VERSION** (optional)
- Updater app version (e.g., `1.0.5`)
- Leave empty to match CLIENT_VERSION
- Example: `1.0.5` produces `truckdoc-updater-v1.0.5.apk`

**RELEASE_TAG** (optional)
- Git tag for the release (e.g., `v1.2.3`)
- Leave empty to auto-generate from CLIENT_VERSION
- Used for GitHub release name

### GitHub Release Parameters

**PUBLISH_GITHUB_RELEASE** (boolean)
- Set to `true` to publish to GitHub
- Default: `false` (build only)

**RELEASE_TITLE** (string)
- Title for GitHub release
- Default: "TruckDoc Android Release"

**RELEASE_NOTES** (text)
- Release notes/changelog
- Auto-appended with version info

## Use Cases

### Use Case 1: Same Version for Both Apps

**Scenario:** Major release with both apps updated

**Parameters:**
```
CLIENT_VERSION: 2.0.0
UPDATER_VERSION: (empty - will use 2.0.0)
RELEASE_TAG: v2.0.0
PUBLISH_GITHUB_RELEASE: true
```

**Result:**
- Client: `truckdoc-client-v2.0.0.apk` (versionCode: 20000)
- Updater: `truckdoc-updater-v2.0.0.apk` (versionCode: 20000)
- GitHub Release: `v2.0.0` with both APKs

### Use Case 2: Client Update Only

**Scenario:** Client app bug fix, updater unchanged

**Parameters:**
```
CLIENT_VERSION: 1.2.4
UPDATER_VERSION: 1.0.0
RELEASE_TAG: v1.2.4
PUBLISH_GITHUB_RELEASE: true
```

**Result:**
- Client: `truckdoc-client-v1.2.4.apk` (versionCode: 10204)
- Updater: `truckdoc-updater-v1.0.0.apk` (versionCode: 10000)
- GitHub Release: `v1.2.4` with both APKs
- Updater detects client update, no self-update

### Use Case 3: Updater Update Only

**Scenario:** Updater improvement, client unchanged

**Parameters:**
```
CLIENT_VERSION: 1.2.3
UPDATER_VERSION: 1.1.0
RELEASE_TAG: v1.2.3-updater-1.1.0
PUBLISH_GITHUB_RELEASE: true
```

**Result:**
- Client: `truckdoc-client-v1.2.3.apk` (versionCode: 10203)
- Updater: `truckdoc-updater-v1.1.0.apk` (versionCode: 10100)
- GitHub Release: `v1.2.3-updater-1.1.0` with both APKs
- Updater detects self-update, no client update

### Use Case 4: Independent Versions

**Scenario:** Different release cycles

**Parameters:**
```
CLIENT_VERSION: 2.5.7
UPDATER_VERSION: 1.3.2
RELEASE_TAG: v2.5.7
PUBLISH_GITHUB_RELEASE: true
```

**Result:**
- Client: `truckdoc-client-v2.5.7.apk` (versionCode: 20507)
- Updater: `truckdoc-updater-v1.3.2.apk` (versionCode: 10302)
- GitHub Release: `v2.5.7` with both APKs

### Use Case 5: Use Git Tag (Default)

**Scenario:** Standard release from tagged commit

**Parameters:**
```
CLIENT_VERSION: (empty)
UPDATER_VERSION: (empty)
RELEASE_TAG: (empty)
```

**Prerequisites:**
```bash
git tag -a v1.5.0 -m "Release 1.5.0"
git push origin v1.5.0
```

**Result:**
- Jenkins detects tag `v1.5.0`
- Client: `truckdoc-client-v1.5.0.apk` (versionCode: 10500)
- Updater: `truckdoc-updater-v1.5.0.apk` (versionCode: 10500)
- GitHub Release: `v1.5.0`

## Version Resolution Logic

```groovy
// Client version priority:
1. CLIENT_VERSION parameter (if provided)
2. Git tag (if exists)
3. Default: "1.0.0"

// Updater version priority:
1. UPDATER_VERSION parameter (if provided)
2. CLIENT_VERSION (if provided)
3. Git tag (if exists)
4. Default: "1.0.0"

// Release tag priority:
1. RELEASE_TAG parameter (if provided)
2. "v" + CLIENT_VERSION
3. Git tag
4. "v1.0.0"
```

## Build Process

### Stage: Extract Versions

```groovy
stage('Extract Versions') {
  // Determines versions for both apps
  // Calculates version codes
  // Sets environment variables:
  // - CLIENT_VERSION_NAME, CLIENT_VERSION_CODE
  // - UPDATER_VERSION_NAME, UPDATER_VERSION_CODE
  // - RELEASE_TAG_NAME
}
```

### Stage: Assemble Release APKs

```bash
# Build client app
./gradlew :app:assembleRelease \
  -PversionName=$CLIENT_VERSION_NAME \
  -PversionCode=$CLIENT_VERSION_CODE

# Build updater app
./gradlew :app-updater:assembleRelease \
  -PversionName=$UPDATER_VERSION_NAME \
  -PversionCode=$UPDATER_VERSION_CODE
```

### Stage: Verify APK Versions

Verifies that APK internal versions match the parameters:
- Extracts actual version from built APK using `aapt`
- Compares with expected version
- Fails build if mismatch detected

### Stage: Publish GitHub Release

Creates release with both APKs and version info in notes:
```
Release Notes:

Client App: v1.2.3 (versionCode: 10203)
Updater App: v1.0.5 (versionCode: 10005)
```

## Examples

### Example 1: Hotfix Client Only

```bash
# Client has critical bug, updater is fine
# Current: Client 1.2.3, Updater 1.0.0

# Jenkins parameters:
CLIENT_VERSION: 1.2.4
UPDATER_VERSION: 1.0.0
RELEASE_TAG: v1.2.4
PUBLISH_GITHUB_RELEASE: true
RELEASE_NOTES: "Hotfix: Fixed critical crash in client app"

# Result:
# - truckdoc-client-v1.2.4.apk (new)
# - truckdoc-updater-v1.0.0.apk (unchanged version)
# - GitHub release v1.2.4
```

### Example 2: Updater Enhancement

```bash
# Updater gets new features, client unchanged
# Current: Client 2.0.0, Updater 1.0.0

# Jenkins parameters:
CLIENT_VERSION: 2.0.0
UPDATER_VERSION: 1.1.0
RELEASE_TAG: v2.0.0-updater-1.1.0
PUBLISH_GITHUB_RELEASE: true
RELEASE_NOTES: "Enhanced updater with better UI and error handling"

# Result:
# - truckdoc-client-v2.0.0.apk (same version)
# - truckdoc-updater-v1.1.0.apk (new)
# - GitHub release v2.0.0-updater-1.1.0
```

### Example 3: Both Apps Updated

```bash
# Major release with both apps updated

# Jenkins parameters:
CLIENT_VERSION: 3.0.0
UPDATER_VERSION: 2.0.0
RELEASE_TAG: v3.0.0
PUBLISH_GITHUB_RELEASE: true
RELEASE_NOTES: "Major update: New features in both apps"

# Result:
# - truckdoc-client-v3.0.0.apk (new)
# - truckdoc-updater-v2.0.0.apk (new)
# - GitHub release v3.0.0
```

## Versioning Strategy

### Recommended: Semantic Versioning

**Client App:** Major.Minor.Patch
- Major: Breaking changes, major features
- Minor: New features, backward compatible
- Patch: Bug fixes, minor improvements

**Updater App:** Major.Minor.Patch
- Major: Breaking changes to update mechanism
- Minor: New updater features
- Patch: Bug fixes

### Independent Release Cycles

Client and updater can evolve independently:

| Release | Client Version | Updater Version | Notes |
|---------|---------------|-----------------|-------|
| v1.0.0  | 1.0.0         | 1.0.0          | Initial release |
| v1.0.1  | 1.0.1         | 1.0.0          | Client hotfix |
| v1.1.0  | 1.1.0         | 1.0.0          | Client features |
| v1.1.0-u1 | 1.1.0       | 1.0.1          | Updater fix |
| v2.0.0  | 2.0.0         | 1.1.0          | Client major |
| v2.0.0-u2 | 2.0.0       | 2.0.0          | Updater major |

## Build Description

Jenkins sets the build description to show both versions:
```
Client: v1.2.3 | Updater: v1.0.5
```

This appears in:
- Jenkins build list
- Build history
- API responses

## GitHub Release Notes

Automatically includes version info:
```
Your custom release notes here...

Client App: v1.2.3 (versionCode: 10203)
Updater App: v1.0.5 (versionCode: 10005)
```

## Verification

The pipeline verifies:
1. ✅ Client APK version matches CLIENT_VERSION parameter
2. ✅ Updater APK version matches UPDATER_VERSION parameter
3. ✅ APK filenames include correct versions
4. ✅ Version codes calculated correctly
5. ❌ Build fails if version mismatch detected

## Best Practices

### 1. Keep Updater Version Lower
```
Client: v2.5.7
Updater: v1.3.2
```
Updater changes less frequently, so lower version numbers make sense.

### 2. Use Descriptive Release Tags
```
v1.2.3              # Both apps updated to 1.2.3
v1.2.3-updater-1.1  # Client 1.2.3, Updater 1.1
v2.0.0-hotfix       # Hotfix for 2.0.0
```

### 3. Document Version Changes
Always include in release notes:
- What changed in client app
- What changed in updater app
- Which app(s) need updating

### 4. Test Both Apps
After building with different versions:
- Install both APKs
- Verify versions in app info
- Test updater detects correct versions
- Test update flow works

## Troubleshooting

### Version Mismatch Error

**Error:**
```
Client APK version mismatch! Expected: 1.2.3, Got: 1.0.0
```

**Cause:** Gradle didn't receive version parameters

**Solution:** Check that parameters are passed correctly in build stage

### Updater Not Detecting Update

**Problem:** Updater shows "No update" but new version exists

**Check:**
1. Version code is higher: `10203 > 10200`
2. APK filename matches pattern: `truckdoc-client-v*.apk`
3. GitHub release is published (not draft)
4. Updater is checking correct repo

### Both Apps Same Version

**Problem:** Want different versions but both show same

**Solution:** Explicitly set UPDATER_VERSION parameter:
```
CLIENT_VERSION: 2.0.0
UPDATER_VERSION: 1.5.0  # Don't leave empty!
```

## Migration from Single Version

### Before (Old Pipeline)
```
VERSION_NAME: 1.2.3
VERSION_CODE: 10203
# Both apps got same version
```

### After (New Pipeline)
```
CLIENT_VERSION: 1.2.3
UPDATER_VERSION: 1.0.5
# Apps can have different versions
```

### Backward Compatible

Leave parameters empty to get old behavior:
```
CLIENT_VERSION: (empty)
UPDATER_VERSION: (empty)
# Both use git tag, same as before
```

## Summary

✅ **Independent versioning** for client and updater
✅ **Flexible parameters** with smart defaults
✅ **Automatic verification** of APK versions
✅ **Clear build descriptions** showing both versions
✅ **Enhanced release notes** with version details
✅ **Backward compatible** with git tag approach

