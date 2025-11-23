# Admin Settings - Custom GitHub Repository Configuration

## Overview

The TruckDoc Updater includes **Admin Settings** that allow authorized users to configure a custom GitHub repository for updates. This is useful for:

- Testing updates from a different repository
- Using a forked repository
- Enterprise deployments with private release repos
- Development and staging environments

## Accessing Admin Settings

### From Main App

1. Open TruckDoc Updater
2. Tap the **menu icon** (⋮) in the toolbar
3. Select **"Admin Settings"**
4. Enter admin password when prompted

### Default Password

**Default admin password:** `admin123`

⚠️ **IMPORTANT:** Change this password immediately after first use!

## Admin Settings Features

### 1. Custom GitHub Repository

Configure which GitHub repository to check for updates:

**Repository Owner**
- GitHub username or organization
- Example: `zhmura`, `mycompany`, `john-doe`

**Repository Name**
- Repository name
- Example: `truckdoc-mobile-all`, `truckdoc-releases`, `my-fork`

**Full URL Format:**
```
https://api.github.com/repos/{owner}/{repo}/releases/latest
```

### 2. Test Connection

Before saving, test that the repository is accessible:
- Verifies repository exists
- Checks API accessibility
- Shows test URL for debugging

### 3. Reset to Default

Revert to the default repository configured in the app:
```
Owner: zhmura
Repo: truckdoc-mobile-all
```

### 4. Change Admin Password

Update the admin password:
- Requires current password
- New password must be 6+ characters
- Confirm new password
- Hashed with SHA-256

## Use Cases

### Use Case 1: Testing Environment

**Scenario:** Test updates from a staging repository

**Steps:**
1. Access Admin Settings (password: `admin123`)
2. Enter:
   - Owner: `zhmura`
   - Repo: `truckdoc-mobile-staging`
3. Tap "Test Connection"
4. Tap "Save Settings"
5. Restart app
6. Updates now check staging repo

### Use Case 2: Enterprise Deployment

**Scenario:** Company wants to host releases in their own repo

**Steps:**
1. Fork or create: `mycompany/truckdoc-releases`
2. Publish APKs to this repo
3. In Admin Settings:
   - Owner: `mycompany`
   - Repo: `truckdoc-releases`
4. Save and restart
5. All devices check company repo

### Use Case 3: Development Testing

**Scenario:** Developer testing custom builds

**Steps:**
1. Create personal fork: `john-doe/truckdoc-mobile-all`
2. Build and publish test releases
3. Configure updater to check fork
4. Test update flow
5. Reset to default when done

### Use Case 4: Multiple Release Channels

**Scenario:** Separate stable and beta channels

**Stable Channel:**
- Repo: `zhmura/truckdoc-mobile-all`
- Production releases

**Beta Channel:**
- Repo: `zhmura/truckdoc-mobile-beta`
- Beta releases for testing

Users can switch between channels via Admin Settings.

## Security

### Password Protection

- ✅ SHA-256 hashed password
- ✅ Stored in SharedPreferences
- ✅ Cannot be extracted easily
- ✅ Must authenticate each time

### Default Password

**⚠️ Security Warning:**

The default password `admin123` is **intentionally weak** to encourage changing it. 

**Best Practices:**
1. Change password on first use
2. Use strong password (10+ chars, mixed case, numbers, symbols)
3. Don't share password
4. Change periodically

### Password Storage

```kotlin
// Password is hashed before storage
val hash = SHA256(password)
prefs.putString("admin_password_hash", hash)

// Verification
val inputHash = SHA256(inputPassword)
return inputHash == storedHash
```

## Configuration Storage

Settings stored in SharedPreferences:

```xml
<string name="custom_repo_owner">mycompany</string>
<string name="custom_repo_name">truckdoc-releases</string>
<string name="admin_password_hash">5e884898da...</string>
```

## Default Configuration

From `GitHubConfig.kt`:

```kotlin
object GitHubConfig {
    const val REPO_OWNER = "zhmura"
    const val REPO_NAME = "truckdoc-mobile-all"
}
```

## Version Resolution Logic

```kotlin
fun getGitHubRepoConfig(): Pair<String, String> {
    // 1. Check if custom repo configured
    if (customRepoOwner.isNotEmpty() && customRepoName.isNotEmpty()) {
        return Pair(customRepoOwner, customRepoName)
    }
    
    // 2. Fall back to default
    return Pair(GitHubConfig.REPO_OWNER, GitHubConfig.REPO_NAME)
}
```

## UI Flow

### Initial Access

```
Main Activity
  ↓ (tap menu)
Menu Options
  ↓ (select "Admin Settings")
Password Dialog
  ├─ Enter password
  ├─ Tap "Authenticate"
  └─ If correct → Admin Settings
      If incorrect → "Invalid password" → Close
```

### Admin Settings Screen

```
┌─────────────────────────────────┐
│ Admin Settings              [←] │
├─────────────────────────────────┤
│                                 │
│ Repository Configuration        │
│ Using default repository        │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Repository Owner            │ │
│ │ [zhmura              ]      │ │
│ │                             │ │
│ │ Repository Name             │ │
│ │ [truckdoc-mobile-all ]      │ │
│ │                             │ │
│ │ Example: owner/repo-name    │ │
│ │                             │ │
│ │ [Test Connection]           │ │
│ └─────────────────────────────┘ │
│                                 │
│ [Save Settings]                 │
│ [Reset to Default]              │
│ [Change Admin Password]         │
│                                 │
└─────────────────────────────────┘
```

## Validation

### Repository Owner Validation

```kotlin
// Must match: letters, numbers, hyphens, underscores
val validOwner = owner.matches(Regex("[a-zA-Z0-9-_]+"))
```

### Repository Name Validation

```kotlin
// Must match: letters, numbers, hyphens, underscores, dots
val validRepo = repo.matches(Regex("[a-zA-Z0-9-_.]+"))
```

### Password Validation

```kotlin
// Minimum 6 characters
val validPassword = password.length >= 6
```

## Testing

### Test Custom Repo

```bash
# 1. Build and install updater
adb install truckdoc-updater-v1.0.0.apk

# 2. Open app, go to Admin Settings
# 3. Enter password: admin123
# 4. Configure custom repo
# 5. Save and restart

# 6. Check SharedPreferences
adb shell run-as com.sanda.truckdoc.updater \
  cat /data/data/com.sanda.truckdoc.updater/shared_prefs/app_updater_preferences.xml

# Should show:
# <string name="custom_repo_owner">mycompany</string>
# <string name="custom_repo_name">truckdoc-releases</string>
```

### Test Password Change

```bash
# 1. Access Admin Settings
# 2. Tap "Change Admin Password"
# 3. Enter:
#    - Current: admin123
#    - New: MySecurePass123!
#    - Confirm: MySecurePass123!
# 4. Save

# 5. Exit and re-enter Admin Settings
# 6. Try old password: admin123 → Fails ✅
# 7. Try new password: MySecurePass123! → Success ✅
```

### Test Reset to Default

```bash
# 1. Configure custom repo
# 2. Save and verify it works
# 3. Tap "Reset to Default"
# 4. Confirm reset
# 5. Restart app
# 6. Verify checking default repo again
```

## Troubleshooting

### Cannot Access Admin Settings

**Problem:** Password dialog doesn't appear or closes immediately

**Solution:**
- Ensure app has proper permissions
- Check if activity is registered in AndroidManifest
- Try force-stop and restart app

### Invalid Password

**Problem:** Default password doesn't work

**Possible causes:**
1. Password was already changed
2. SharedPreferences corrupted

**Solution:**
```bash
# Clear app data to reset password
adb shell pm clear com.sanda.truckdoc.updater

# Or manually delete preferences
adb shell run-as com.sanda.truckdoc.updater \
  rm /data/data/com.sanda.truckdoc.updater/shared_prefs/app_updater_preferences.xml
```

### Custom Repo Not Working

**Problem:** Updates not detected from custom repo

**Checklist:**
1. ✅ Repository is public (or has proper auth)
2. ✅ Repository has releases
3. ✅ APK filenames match patterns:
   - `truckdoc-client-v*.apk`
   - `truckdoc-updater-v*.apk`
4. ✅ App was restarted after saving settings
5. ✅ Network connection available

**Debug:**
```bash
# Test API manually
curl https://api.github.com/repos/{owner}/{repo}/releases/latest

# Should return JSON with:
# - tag_name
# - assets[]
```

## Best Practices

### 1. Change Default Password Immediately

```
First launch → Admin Settings → Change Password
```

### 2. Document Custom Repo

If using custom repo, document:
- Repository URL
- Purpose (staging, production, etc.)
- Who has access
- Update schedule

### 3. Test Before Production

Always test custom repo configuration:
1. Configure in test device
2. Verify updates detected
3. Test download and install
4. Confirm both apps update correctly

### 4. Backup Configuration

Document your settings:
```
Repository Owner: mycompany
Repository Name: truckdoc-releases
Admin Password: [stored securely]
```

### 5. Use Separate Repos for Channels

```
Production: company/truckdoc-releases
Staging: company/truckdoc-staging
Development: company/truckdoc-dev
```

## Security Considerations

### ✅ Secure

- Password hashed with SHA-256
- Settings require authentication
- Stored in app-private storage
- Cannot be accessed without password

### ⚠️ Limitations

- Default password is known (change it!)
- Password stored on device (can be extracted with root)
- No brute-force protection
- No password recovery mechanism

### 🔒 For High Security

If you need stronger security:

1. **Use MDM/EMM** - Manage settings via enterprise mobility management
2. **Build-time configuration** - Hard-code repo in BuildConfig
3. **Remote config** - Fetch repo URL from authenticated API
4. **Certificate-based auth** - Use client certificates

## API Reference

### PreferencesManager Methods

```kotlin
// Get repo configuration
fun getGitHubRepoConfig(): Pair<String, String>

// Check if custom repo set
fun hasCustomRepo(): Boolean

// Set custom repo
var customRepoOwner: String
var customRepoName: String

// Clear custom repo
fun clearCustomRepo()

// Password management
fun verifyAdminPassword(password: String): Boolean
fun setAdminPassword(newPassword: String)
```

### GitHubUpdateRepository

```kotlin
// Automatically uses custom repo if configured
suspend fun checkForUpdates(): SystemUpdateInfo {
    val (owner, repo) = preferencesManager.getGitHubRepoConfig()
    val release = gitHubApiService.getLatestRelease(owner, repo)
    // ...
}
```

## Summary

✅ **Admin Settings provide:**
- Custom GitHub repository configuration
- Password-protected access
- Test connection functionality
- Reset to default option
- Password change capability

✅ **Security features:**
- SHA-256 password hashing
- Authentication required
- App-private storage
- No cleartext passwords

✅ **Flexibility:**
- Switch between repositories
- Test different release channels
- Enterprise deployment support
- Development testing

**Default password: `admin123` - Change it immediately!** 🔒

