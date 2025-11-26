# Local Testing Guide with Docker

## Overview

This guide explains how to test the release process and app update flow locally using Docker to simulate the GitHub API and host APK artifacts. This allows you to verify the full update cycle without publishing to the real GitHub repository.

## Prerequisites

- Docker and Docker Compose installed
- Built APKs from local Gradle build
- Android device/emulator on the same network

## Step 1: Prepare Local Release Artifacts

1.  **Build Release APKs:**
    ```bash
    ./gradlew clean :app:assembleRelease :app-updater:assembleRelease \
      -PversionName=1.0.5 -PversionCode=10005
    ```

2.  **Create Artifact Directory:**
    ```bash
    mkdir -p local-release/files
    ```

3.  **Copy APKs:**
    ```bash
    cp truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/*.apk local-release/files/
    cp app-updater/build/outputs/apk/release/*.apk local-release/files/
    ```

## Step 2: Setup Mock GitHub API Server

We will use Nginx to serve the APKs and a static JSON response mimicking the GitHub API.

1.  **Create `local-release/nginx.conf`:**

    ```nginx
    events {}
    http {
        server {
            listen 80;
            
            # Serve APK files
            location /files/ {
                root /usr/share/nginx/html;
                autoindex on;
            }

            # Mock GitHub API: /repos/{owner}/{repo}/releases/latest
            # Matches any owner/repo combination
            location ~ ^/repos/[^/]+/[^/]+/releases/latest$ {
                default_type application/json;
                return 200 '{
                    "tag_name": "v1.0.5",
                    "name": "Local Test Release v1.0.5",
                    "draft": false,
                    "prerelease": false,
                    "assets": [
                        {
                            "name": "truckdoc-client-v1.0.5.apk",
                            "browser_download_url": "http://10.0.2.2:8080/files/truckdoc-client-v1.0.5.apk",
                            "size": 1000000,
                            "content_type": "application/vnd.android.package-archive"
                        },
                        {
                            "name": "truckdoc-updater-v1.0.5.apk",
                            "browser_download_url": "http://10.0.2.2:8080/files/truckdoc-updater-v1.0.5.apk",
                            "size": 1000000,
                            "content_type": "application/vnd.android.package-archive"
                        }
                    ]
                }';
            }
        }
    }
    ```
    *Note: `10.0.2.2` is the host loopback address for Android Emulator. If using a physical device, use your computer's local IP (e.g., `192.168.1.x`).*

2.  **Create `local-release/docker-compose.yml`:**

    ```yaml
    version: '3'
    services:
      github-mock:
        image: nginx:alpine
        ports:
          - "8080:80"
        volumes:
          - ./files:/usr/share/nginx/html/files
          - ./nginx.conf:/etc/nginx/nginx.conf:ro
    ```

## Step 3: Run the Mock Server

```bash
cd local-release
docker-compose up -d
```

Verify it's working:
- API: `http://localhost:8080/repos/test/test/releases/latest`
- File: `http://localhost:8080/files/truckdoc-client-v1.0.5.apk`

## Step 4: Configure App Updater

1.  **Install Updater App** (if not already installed):
    ```bash
    adb install local-release/files/truckdoc-updater-v1.0.5.apk
    ```

2.  **Configure Admin Settings:**
    - Open TruckDoc Updater
    - Go to **Menu -> Admin Settings**
    - Password: `admin123`
    - **Change Repository Configuration:**
        - Since we are mocking the API, the app needs to point to our local server instead of `api.github.com`.
        - **Wait!** The app is hardcoded to use `https://api.github.com` base URL in `NetworkModule.kt`.
        
        **To test locally, we need a way to override the Base URL.**

### Enable Base URL Override (Required for Local Testing)

To fully support local testing without rebuilding code for the base URL change, we should add a "Custom Base URL" option to Admin Settings.

**Quick Workaround for Testing:**
Since we haven't implemented dynamic Base URL yet, you can modify `GitHubConfig.kt` temporarily:

```kotlin
// Temporary change for local testing
const val GITHUB_BASE_URL = "http://10.0.2.2:8080/"
// Make sure to allow cleartext traffic in network_security_config.xml for this IP
```

**Enable Cleartext for Local IP:**
Edit `app-updater/src/main/res/xml/network_security_config.xml`:
```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="true">10.0.2.2</domain>
    <domain includeSubdomains="true">192.168.1.100</domain> <!-- Your local IP -->
</domain-config>
```

## Step 5: Run the Test

1.  Build app with temporary config changes.
2.  Install on Emulator.
3.  Open Updater App.
4.  Tap **Check for Updates**.
5.  It should hit `http://10.0.2.2:8080/repos/.../releases/latest`.
6.  Receive JSON response.
7.  Show "Update Available" (if version > installed).
8.  Download from `http://10.0.2.2:8080/files/...`.

## Cleanup

```bash
cd local-release
docker-compose down
rm -rf local-release
```

