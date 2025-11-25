# Create Public Releases Repository

## Why Separate Releases Repo?

- ✅ Keep source code private
- ✅ Make releases public
- ✅ No authentication needed in app
- ✅ Clean separation of concerns

## Step-by-Step Setup

### Step 1: Create Public Releases Repository

```bash
# Create new public repo (no source code, releases only)
gh repo create zhmura/truckdoc-mobile-releases \
  --public \
  --description "TruckDoc Mobile - Public Releases" \
  --disable-wiki \
  --disable-issues

# Or via GitHub web UI:
# 1. Go to: https://github.com/new
# 2. Repository name: truckdoc-mobile-releases
# 3. Visibility: Public ✅
# 4. Click "Create repository"
```

### Step 2: Add README to Releases Repo

```bash
# Clone the new repo
git clone https://github.com/zhmura/truckdoc-mobile-releases.git
cd truckdoc-mobile-releases

# Create README
cat > README.md << 'EOF'
# TruckDoc Mobile - Releases

This repository hosts public releases for TruckDoc Mobile applications.

## Latest Release

Download the latest version from [Releases](https://github.com/zhmura/truckdoc-mobile-releases/releases/latest).

## Applications

### TruckDoc Client
Main TruckDoc mobile application for drivers.

### TruckDoc Updater
Automatic update manager for TruckDoc applications.

## Installation

1. Download `truckdoc-updater-v{version}.apk`
2. Install the updater
3. Open updater - it will download and install the client app
4. Both apps will stay updated automatically

## Support

For issues and support, contact your administrator.
EOF

# Commit and push
git add README.md
git commit -m "Initial commit"
git push origin main
```

### Step 3: Update App Configuration

Update `GitHubConfig.kt`:

```kotlin
object GitHubConfig {
    const val GITHUB_BASE_URL = "https://api.github.com/"
    const val REPO_OWNER = "zhmura"
    const val REPO_NAME = "truckdoc-mobile-releases"  // Changed!
    
    // ... rest unchanged
}
```

### Step 4: Update Jenkins to Publish to New Repo

**Option A: Update Jenkinsfile**

Change the GitHub CLI commands to use new repo:

```groovy
// In Publish GitHub Release stage
sh '''
  # Set repo for gh CLI
  export GH_REPO="zhmura/truckdoc-mobile-releases"
  
  # Create release in new repo
  $GH_CLI release create "$RELEASE_TAG_NAME" \
    "$MAIN_APK" \
    "$UPDATER_APK" \
    --repo "$GH_REPO" \
    --title "$RELEASE_TITLE" \
    --notes "$RELEASE_NOTES"
'''
```

**Option B: Use Separate Jenkins Job**

Create new job for releases repo:
- Same build process
- Different publish target
- Keeps source repo private

### Step 5: Migrate Existing Release

Copy existing release to new repo:

```bash
# Download assets from current release
cd /tmp
gh release download v1.0.3 --repo zhmura/truckdoc-mobile-all

# Create release in new public repo
gh release create v1.0.3 \
  --repo zhmura/truckdoc-mobile-releases \
  --title "TruckDoc Android Release" \
  --notes "Automated release from Jenkins.

Client App: v1.0.3 (versionCode: 10003)
Updater App: v1.0.3 (versionCode: 10003)" \
  truckdoc-client-v1.0.3.apk \
  truckdoc-updater-v1.0.3.apk
```

### Step 6: Test the Setup

```bash
# Test API (should return 200 now)
curl -I https://api.github.com/repos/zhmura/truckdoc-mobile-releases/releases/latest

# Should show release data
curl https://api.github.com/repos/zhmura/truckdoc-mobile-releases/releases/latest | jq '.tag_name'
# Output: "v1.0.3"
```

### Step 7: Update and Test App

```bash
# 1. Update GitHubConfig.kt with new repo name
# 2. Rebuild updater
./gradlew :app-updater:assembleRelease -PversionName=1.0.4 -PversionCode=10004

# 3. Install on device
adb install app-updater/build/outputs/apk/release/truckdoc-updater-v1.0.4.apk

# 4. Open app and check for updates
# Should now work! ✅
```

## Architecture

```
┌─────────────────────────────────────────┐
│   truckdoc-mobile-all (Private)         │
│   - Source code                         │
│   - Development                         │
│   - Jenkins builds here                 │
└─────────────────────────────────────────┘
                 ↓ (Jenkins publishes)
┌─────────────────────────────────────────┐
│   truckdoc-mobile-releases (Public)     │
│   - Releases only                       │
│   - No source code                      │
│   - APKs downloadable                   │
└─────────────────────────────────────────┘
                 ↓ (App checks)
┌─────────────────────────────────────────┐
│   TruckDoc Updater (on device)          │
│   - Checks public releases repo         │
│   - Downloads APKs                      │
│   - Installs updates                    │
└─────────────────────────────────────────┘
```

## Benefits

### Source Code Protection
- ✅ Source code stays private
- ✅ Only releases are public
- ✅ Intellectual property protected
- ✅ Development process private

### Public Distribution
- ✅ No authentication needed
- ✅ Fast CDN downloads
- ✅ No rate limits
- ✅ Simple updater implementation

### Clean Separation
- ✅ Development repo separate from releases
- ✅ Clear purpose for each repo
- ✅ Better organization
- ✅ Easier access control

## Alternative: Make Current Repo Public

If you don't mind open-sourcing:

**Pros:**
- ✅ Simpler (one repo)
- ✅ Community can contribute
- ✅ Transparency
- ✅ No migration needed

**Cons:**
- ⚠️ Source code public
- ⚠️ Can't easily go back to private
- ⚠️ All history visible

## Recommendation

**Use separate public releases repo:**

1. **Create:** `zhmura/truckdoc-mobile-releases` (public)
2. **Update:** `GitHubConfig.REPO_NAME`
3. **Migrate:** Copy v1.0.3 release
4. **Configure:** Jenkins to publish there
5. **Test:** Updater detects releases

**Time required:** ~15 minutes

**Benefits:** Best of both worlds - private source, public releases

## Quick Start Commands

```bash
# 1. Create repo
gh repo create zhmura/truckdoc-mobile-releases --public

# 2. Copy current release
gh release download v1.0.3 --repo zhmura/truckdoc-mobile-all
gh release create v1.0.3 --repo zhmura/truckdoc-mobile-releases \
  truckdoc-client-v1.0.3.apk truckdoc-updater-v1.0.3.apk

# 3. Update app config
# Edit: app-updater/src/main/java/.../config/GitHubConfig.kt
# Change: REPO_NAME = "truckdoc-mobile-releases"

# 4. Test
curl https://api.github.com/repos/zhmura/truckdoc-mobile-releases/releases/latest
```

Done! 🎉

