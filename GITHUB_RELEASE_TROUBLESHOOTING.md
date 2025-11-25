# GitHub Release API 404 Troubleshooting

## Problem

- ✅ Release visible at: `https://github.com/zhmura/truckdoc-mobile-all/releases/latest`
- ❌ API returns 404: `https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest`

## Common Causes

### Cause 1: Release is a Draft (Most Common)

**Problem:** Draft releases are not returned by the `/releases/latest` API endpoint.

**Check:**
1. Go to: https://github.com/zhmura/truckdoc-mobile-all/releases
2. Look for the release
3. Check if it says **"Draft"** badge

**Solution:**
1. Click on the draft release
2. Click **"Edit"**
3. **Uncheck** "Save as draft"
4. Click **"Publish release"**

**Verify:**
```bash
# Should now return 200
curl -I https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest

# Should show release data
curl https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest
```

### Cause 2: Repository is Private

**Problem:** Private repos require authentication for API access.

**Check:**
1. Go to: https://github.com/zhmura/truckdoc-mobile-all
2. Look for **"Private"** badge

**Solutions:**

#### Option A: Make Repository Public (Recommended)

1. Repository → Settings
2. Scroll to "Danger Zone"
3. Click "Change visibility"
4. Select "Make public"
5. Confirm

**Benefits:**
- ✅ No authentication needed in app
- ✅ Faster downloads (CDN)
- ✅ No rate limits on assets
- ✅ Simpler implementation

#### Option B: Make Releases Public Only

Unfortunately, **GitHub doesn't support this**. If repo is private, releases are private too.

**Workaround:** Create a separate public repo for releases:

```bash
# Create new public repo
gh repo create zhmura/truckdoc-mobile-releases --public

# Update Jenkins to publish there
# Update GitHubConfig.kt:
const val REPO_NAME = "truckdoc-mobile-releases"
```

#### Option C: Add Authentication to App (Not Recommended)

Add GitHub token to app:

```kotlin
// In NetworkModule.kt
@Provides
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ghp_YOUR_TOKEN")
                .build()
            chain.proceed(request)
        }
        .build()
}
```

**Problems:**
- ❌ Token can be extracted from APK
- ❌ Token expires
- ❌ Security risk
- ❌ Rate limits still apply

### Cause 3: Release is Marked as Pre-release

**Problem:** `/releases/latest` only returns stable releases, not pre-releases.

**Check:**
1. Go to release page
2. Look for **"Pre-release"** badge

**Solution:**

Either:
1. **Uncheck "Set as a pre-release"** when publishing
2. Or use `/releases` endpoint instead of `/releases/latest`

**Update API call:**
```kotlin
// Get all releases (includes pre-releases)
val releases = gitHubApiService.getAllReleases(owner, repo)
val latestRelease = releases.firstOrNull { !it.draft }
```

### Cause 4: No Releases Exist

**Problem:** Repository has no published releases yet.

**Check:**
```bash
curl https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases
# Returns: []
```

**Solution:** Create first release via Jenkins:

1. Jenkins job parameters:
   - `CLIENT_VERSION`: `1.0.0`
   - `UPDATER_VERSION`: `1.0.0`
   - `PUBLISH_GITHUB_RELEASE`: `true` ✅
   - `RELEASE_TAG`: `v1.0.0`
   - `RELEASE_TITLE`: `TruckDoc Mobile v1.0.0 - Initial Release`
   - `RELEASE_NOTES`: `Initial release`

2. Build → Publishes to GitHub

## Quick Diagnosis

### Test 1: Check API Response

```bash
# Test API endpoint
curl -v https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest

# Possible responses:
# 200 OK → Release exists and is public ✅
# 404 Not Found → Draft, private, or no releases ❌
# 403 Forbidden → Rate limited or auth required ❌
```

### Test 2: Check All Releases

```bash
# List all releases (includes drafts if authenticated)
curl https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases

# If returns [] → No releases exist
# If returns data → Check draft/prerelease status
```

### Test 3: Check Repository Visibility

```bash
# Try accessing repo
curl -I https://api.github.com/repos/zhmura/truckdoc-mobile-all

# 200 OK → Public repo ✅
# 404 Not Found → Private repo or doesn't exist ❌
```

### Test 4: Check Web UI

Visit: https://github.com/zhmura/truckdoc-mobile-all/releases

- If you see releases → Check draft/pre-release status
- If empty → No releases published yet
- If 404 → Repository doesn't exist or is private

## Solutions Summary

### Most Likely: Release is Draft

**Fix:**
1. Go to: https://github.com/zhmura/truckdoc-mobile-all/releases
2. Find your release
3. Click "Edit"
4. **Uncheck "Save as draft"**
5. Click **"Publish release"**

### If Repository is Private

**Best solution:** Make releases public via separate repo:

```bash
# 1. Create public releases repo
gh repo create zhmura/truckdoc-mobile-releases --public --description "TruckDoc Mobile Releases"

# 2. Update GitHubConfig.kt
const val REPO_NAME = "truckdoc-mobile-releases"

# 3. Configure Jenkins to publish there
# 4. Rebuild and publish
```

### If No Releases Exist

**Create first release via Jenkins:**

1. Run Jenkins build with `PUBLISH_GITHUB_RELEASE=true`
2. Or manually:
   ```bash
   gh release create v1.0.0 \
     truckdoc-client-v1.0.0.apk \
     truckdoc-updater-v1.0.0.apk \
     --title "TruckDoc Mobile v1.0.0" \
     --notes "Initial release"
   ```

## Verify Fix

After applying solution:

```bash
# 1. Test API
curl https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest

# Should return JSON with:
{
  "tag_name": "v1.0.0",
  "name": "TruckDoc Mobile v1.0.0",
  "draft": false,
  "prerelease": false,
  "assets": [
    {
      "name": "truckdoc-client-v1.0.0.apk",
      "browser_download_url": "https://github.com/.../download/v1.0.0/truckdoc-client-v1.0.0.apk"
    },
    {
      "name": "truckdoc-updater-v1.0.0.apk",
      "browser_download_url": "https://github.com/.../download/v1.0.0/truckdoc-updater-v1.0.0.apk"
    }
  ]
}

# 2. Test in updater app
# Open app → Check for Updates → Should work ✅
```

## Prevention

### Always Publish (Not Draft)

When creating releases:
- ✅ Uncheck "Save as draft"
- ✅ Check "Publish release"
- ✅ Verify API returns 200

### Jenkins Configuration

Ensure Jenkins publishes correctly:

```groovy
// In Jenkinsfile
$GH_CLI release create "$RELEASE_TAG_NAME" \
  "$MAIN_APK" \
  "$UPDATER_APK" \
  --title "$RELEASE_TITLE" \
  --notes "$RELEASE_NOTES"
  # No --draft flag = published immediately ✅
```

### Test After Publishing

Always verify:
```bash
# 1. Check API
curl https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest

# 2. Check updater app
# Open → Check for Updates → Should detect release
```

## Current Status

Based on your error:
```
404 https://api.github.com/repos/zhmura/truckdoc-mobile-all/releases/latest
```

**Most likely cause:** Release is a **draft**

**Quick fix:**
1. Go to: https://github.com/zhmura/truckdoc-mobile-all/releases
2. Find your release
3. Click "Edit"
4. **Uncheck "Save as draft"**
5. Click **"Publish release"**
6. Test updater again

**After publishing, the updater will work!** ✅

