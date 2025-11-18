# TruckDoc Mobile Versioning Strategy

## Overview

The TruckDoc mobile apps use **Git tags** as the single source of truth for versioning. The Jenkins build pipeline automatically extracts the version from the Git tag and applies it to the APKs.

## Version Format

### Git Tag Format
```
v<major>.<minor>.<patch>
```

Examples:
- `v1.0.0` - Initial release
- `v1.2.3` - Version 1.2.3
- `v2.0.0` - Major version 2

### Version Code Calculation

Version code is automatically calculated from the version name:

```
versionCode = (major * 10000) + (minor * 100) + patch
```

Examples:
| Tag     | Version Name | Version Code | Calculation        |
|---------|--------------|--------------|-------------------|
| v1.0.0  | 1.0.0        | 10000        | 1*10000 + 0*100 + 0 |
| v1.2.3  | 1.2.3        | 10203        | 1*10000 + 2*100 + 3 |
| v2.5.7  | 2.5.7        | 20507        | 2*10000 + 5*100 + 7 |
| v10.0.0 | 10.0.0       | 100000       | 10*10000 + 0*100 + 0 |

This scheme supports:
- Major versions: 0-99
- Minor versions: 0-99
- Patch versions: 0-99
- Maximum version code: 999999 (v99.99.99)

## Release Process

### 1. Create Git Tag

```bash
# Create and push tag
git tag -a v1.2.3 -m "Release version 1.2.3"
git push origin v1.2.3
```

### 2. Jenkins Build

The Jenkins pipeline automatically:
1. Checks out the tagged commit
2. Extracts version from tag: `v1.2.3` → `1.2.3`
3. Calculates version code: `1.2.3` → `10203`
4. Builds APKs with these versions
5. Names APKs: `truckdoc-client-v1.2.3.apk`

### 3. GitHub Release

When `PUBLISH_GITHUB_RELEASE=true`:
1. Creates GitHub release with tag `v1.2.3`
2. Uploads both APKs:
   - `truckdoc-client-v1.2.3.apk`
   - `truckdoc-updater-v1.2.3.apk`

### 4. App Updater Detection

The updater app:
1. Fetches latest GitHub release
2. Finds APKs by pattern: `truckdoc-client-v*.apk`
3. Extracts version from filename: `v1.2.3` → `1.2.3`
4. Compares with installed app version
5. Shows update if newer version available

## APK Naming Convention

### Production Release APKs

```
truckdoc-client-v{version}.apk
truckdoc-updater-v{version}.apk
```

Examples:
- `truckdoc-client-v1.2.3.apk`
- `truckdoc-updater-v1.2.3.apk`

### Debug APKs

```
truckdoc-client-v{version}-{flavor}-debug.apk
truckdoc-updater-v{version}-debug.apk
```

Note: Debug builds include the flavor name for debugging purposes, but release builds omit it for cleaner naming.

## Version Verification

### Check APK Version

```bash
# Using aapt
aapt dump badging app.apk | grep version

# Output example:
# versionCode='10203' versionName='1.2.3'
```

### Check Installed App Version

```bash
# Check client app
adb shell dumpsys package com.sanda.truckdoc.client.default | grep versionName

# Check updater app
adb shell dumpsys package com.sanda.truckdoc.updater | grep versionName
```

## Local Development Builds

### Default Versions (No Tag)

If building without a Git tag:
- Version Name: `1.0`
- Version Code: `1`

### Build with Custom Version

```bash
# Build with specific version
./gradlew assembleRelease -PversionName=1.2.3 -PversionCode=10203

# Output:
# truckdoc-client-v1.2.3.apk (versionCode=10203)
```

## Version Comparison Logic

### In App Updater

The updater compares versions in this order:

1. **Version Code** (primary)
   - If codes differ, higher code wins
   - Example: 10203 > 10200

2. **Version Name** (fallback)
   - Semantic version comparison
   - Example: "1.2.3" > "1.2.2"

### Semantic Version Comparison

```
1.2.3 vs 1.2.2 → 1.2.3 is newer (patch)
1.3.0 vs 1.2.9 → 1.3.0 is newer (minor)
2.0.0 vs 1.9.9 → 2.0.0 is newer (major)
```

## Hotfix Process

### Creating a Hotfix

```bash
# From main branch
git checkout -b hotfix/1.2.4

# Make fixes
git commit -m "Fix critical bug"

# Create tag
git tag -a v1.2.4 -m "Hotfix: Critical bug fix"

# Push
git push origin hotfix/1.2.4
git push origin v1.2.4

# Trigger Jenkins build with tag v1.2.4
```

## Version Bump Guidelines

### Patch Version (x.x.X)
- Bug fixes
- Minor improvements
- No API changes
- Example: v1.2.3 → v1.2.4

### Minor Version (x.X.0)
- New features
- Backward compatible changes
- Non-breaking API updates
- Example: v1.2.4 → v1.3.0

### Major Version (X.0.0)
- Breaking changes
- Major redesigns
- API incompatibilities
- Example: v1.9.9 → v2.0.0

## Troubleshooting

### Version Mismatch

**Problem**: APK filename shows v1.2.3 but internal version is different

**Solution**:
```bash
# Verify tag exists
git tag -l "v1.2.3"

# Check Jenkins build parameters
# Ensure VERSION_NAME and VERSION_CODE are set correctly

# Rebuild from clean state
./gradlew clean
./gradlew assembleRelease -PversionName=1.2.3 -PversionCode=10203
```

### Updater Not Detecting Update

**Problem**: New version released but updater doesn't show update

**Causes**:
1. Version code not higher than installed version
2. APK naming doesn't match pattern
3. GitHub release not published (still draft)
4. Network/API issues

**Solution**:
```bash
# Verify version codes
# Installed: versionCode=10200
# New release: versionCode=10203 (must be higher)

# Check APK naming
# Must match: truckdoc-client-v*.apk

# Verify GitHub release
curl https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest
```

## Best Practices

1. **Always tag releases**: Never release without a Git tag
2. **Use semantic versioning**: Follow major.minor.patch convention
3. **Tag before building**: Ensure tag exists before Jenkins build
4. **Verify APK versions**: Check internal version matches tag
5. **Test updater**: Verify updater detects new version correctly
6. **Document changes**: Use meaningful tag messages

## Examples

### Standard Release Flow

```bash
# 1. Finish development on branch
git checkout truckdoc-39-android-31

# 2. Create and push tag
git tag -a v1.3.0 -m "Release 1.3.0: New features and bug fixes"
git push origin v1.3.0

# 3. Trigger Jenkins build
# - Jenkins extracts v1.3.0 → version 1.3.0, code 10300
# - Builds APKs with correct versions
# - Names: truckdoc-client-v1.3.0.apk

# 4. Publish to GitHub (if PUBLISH_GITHUB_RELEASE=true)
# - Creates release v1.3.0
# - Uploads both APKs

# 5. Updater automatically detects
# - Checks GitHub releases/latest
# - Finds v1.3.0 APKs
# - Compares with installed version
# - Shows update available
```

## Version History Example

| Version | Code  | Date       | Changes                    |
|---------|-------|------------|----------------------------|
| v1.0.0  | 10000 | 2024-01-15 | Initial release            |
| v1.0.1  | 10001 | 2024-01-20 | Bug fixes                  |
| v1.1.0  | 10100 | 2024-02-01 | New features               |
| v1.1.1  | 10101 | 2024-02-05 | Hotfix                     |
| v1.2.0  | 10200 | 2024-03-01 | Major feature update       |
| v2.0.0  | 20000 | 2024-06-01 | Breaking changes, redesign |

