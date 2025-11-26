# Local Jenkins Publishing & Updater Test Guide

## Goal

Run the Jenkins pipeline locally inside Docker, publish APKs plus `release_manifest.json` directly from Jenkins, and then point the TruckDoc App Updater at that Jenkins instance by entering its local network address in Admin Settings.

---

## 1. Prerequisites

- Docker Desktop / Docker Engine
- ≥60 GB free disk (Jenkins home + Android SDK)
- Android SDK downloaded on the host (recommended) or willingness to install it inside the container
- Android device/emulator on the same network as your workstation (emulator can use `10.0.2.2`)

---

## 2. Start a Local Jenkins (Docker)

```bash
# Launch Jenkins with the repo + SDK mounted into the container
docker run -d --name truckdoc-jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /Users/sanda/truckdoc-cursor:/var/jenkins_home/workspace/truckdoc-mobile-all \
  -v $ANDROID_SDK_ROOT:/opt/android-sdk \
  jenkins/jenkins:lts-jdk17
```

1.  Go to `http://localhost:8080`, finish the first‑run wizard, and install suggested plugins.
2.  Provision Android tools once:
    ```bash
    docker exec -u root -it truckdoc-jenkins bash
    apt-get update && apt-get install -y unzip wget git
    chown -R jenkins:jenkins /opt/android-sdk
    exit
    ```
3.  In **Manage Jenkins → Global Tool Configuration**, register:
    - **JDK 17** (`/opt/java/openjdk`)
    - **Android SDK** (`/opt/android-sdk`)

---

## 3. Create the Pipeline Job

1.  New item → **Pipeline** → name it `truckdoc-android`.
2.  Under *Pipeline*, choose “Pipeline script from SCM” and point to the mounted repo (`truckdoc-mobile-all`).
3.  Script path: `ci/jenkins-github-release/Jenkinsfile`.
4.  Set environment overrides (if not already present):
    ```
    JAVA_HOME=/opt/java/openjdk
    ANDROID_HOME=/opt/android-sdk
    ANDROID_SDK_ROOT=/opt/android-sdk
    ```
5.  Leave `PUBLISH_GITHUB_RELEASE` unchecked unless you still want to post to GitHub.

---

## 4. Trigger a Local Release

Use **Build with Parameters**:

- `CLIENT_VERSION` / `UPDATER_VERSION`: e.g., `1.4.0`
- `RELEASE_NOTES`: any text
- `PUBLISH_GITHUB_RELEASE`: `false`

The pipeline now:

1.  Builds both APKs.
2.  Copies them into `release-bundle/`.
3.  Generates `release_manifest.json` with download URLs pointing at Jenkins artifacts.

Artifact URLs follow:

```
http://<jenkins-host>:8080/job/<job-name>/<build-number>/artifact/release-bundle/truckdoc-client-vX.apk
http://<jenkins-host>:8080/job/<job-name>/<build-number>/artifact/release-bundle/truckdoc-updater-vY.apk
http://<jenkins-host>:8080/job/<job-name>/<build-number>/artifact/release-bundle/release_manifest.json
```

`env.BUILD_URL` in the Jenkins log already contains `http://<host>:8080/job/<job-name>/<build-number>/`.

---

## 5. Configure TruckDoc App Updater

1.  Install the freshly built updater:
    ```bash
    adb install -r release-bundle/truckdoc-updater-v1.4.0.apk
    ```
2.  Confirm the device can reach Jenkins:
    - Emulator → `http://10.0.2.2:8080`
    - Physical device → your workstation IP (e.g., `http://192.168.0.42:8080`)
3.  In the Updater app:
    - Menu → **Admin Settings** (password `admin123` unless changed)
    - The Jenkins mode is enabled by default; just confirm the toggle is on.
    - Paste or confirm the manifest URL, e.g.,
      ```
      http://192.168.0.42:8080/job/truckdoc-android/lastSuccessfulBuild/artifact/release-bundle/release_manifest.json
      ```
    - Tap **“Test Manifest Endpoint.”** A success toast confirms connectivity.
    - Save Settings (status text switches to “Using Jenkins server: …”).

> Cleartext traffic for local IPs (10.0.2.2 / LAN) is already enabled in `network_security_config.xml`.

---

## 6. Run the Update Flow

1.  Back on the main screen, tap **Check for Updates**.
2.  The Updater downloads `release_manifest.json`, compares versions, and displays any available updates.
3.  Download links inside the manifest already point at Jenkins, so APKs stream straight from your local CI.
4.  Tap **Download** / **Install** as usual to verify end-to-end behavior.

---

## 7. Troubleshooting

| Issue | Fix |
| --- | --- |
| Manifest test fails | Ensure the phone/emulator can hit `http://<jenkins-ip>:8080`, Jenkins build finished successfully, and the manifest path is correct. |
| 404 on artifacts | Confirm the build number exists and the pipeline completed the “Prepare Release Bundle” stage. |
| SSL or cleartext errors | Stick to HTTP for local testing or add your host to `network_security_config.xml` with `cleartextTrafficPermitted="true"`. |
| Still checking GitHub | Verify the Jenkins toggle is enabled, the manifest URL saved, and restart the Updater if necessary. |

---

With this setup, Jenkins itself is the distribution point: no GitHub releases are required. Just replace the manifest URL with your production Jenkins address when moving beyond local testing.

