# TruckDoc Mobile — Production Build & Setup Guide

This guide covers two things:

1. **[Part 1 — Jenkins production build](#part-1--jenkins-production-build)** for the **main app** and the **updater**.
2. **[Part 2 — Installing & configuring](#part-2--android-installation--configuration)** the main app and updater on Android devices.

---

## System overview

The product ships as **two APKs** that work together:

| App | Module | Package (applicationId) | Role |
|---|---|---|---|
| **Main client** | `:app` (`truckdoc-client-m2/application`) | `com.sanda.truckdoc.client.default` | The TruckDoc driver app (messaging, docs, maintenance, GPS). Talks to the mobile API. |
| **Updater** | `:truckdoc-client-updater` (`truckdoc-client-m2/truckdoc-client-updater`) | `com.sanda.truckdoc.client.updater` | Background app that keeps the main client up to date by polling the update server (TCUS), downloading and installing new APKs. |

**How they cooperate:**
1. On launch, the main app writes `/sdcard/TruckDoc/.shared.properties` with `clientTargetPackage=com.sanda.truckdoc.client.default`.
2. The updater reads that file to learn which package to update.
3. The updater polls `http://tcus.truckdoc.ru/check_update` and, if a newer version exists, downloads the APK and launches the system installer.

**Toolchain (both apps):** AGP 8.5.2 · Gradle 8.7 · JDK 17 · Kotlin 1.9.24
**SDK range (both apps):** `minSdk 26` (Android 8.0) → `targetSdk 35` (Android 15), `compileSdk 35`.

---

# Part 1 — Jenkins production build

## 1.1 Build agent prerequisites

The Jenkins agent (controller built-in node or a dedicated agent) must have:

| Requirement | Notes |
|---|---|
| **JDK 17** | Required by AGP 8. Must be on `PATH` / `JAVA_HOME`. |
| **Android SDK** | Platform **android-35** (and/or 36) + **build-tools 35.0.0** (or 36.1.0). Set `ANDROID_HOME`. |
| **SDK licenses accepted** | `yes \| sdkmanager --licenses` |
| **Git** | For SCM checkout and the build-metadata steps. |
| **Internet access** | First build downloads Gradle 8.7 + dependencies. |
| **Memory** | ≥ 4 GB free for the Gradle/R8 build. |

Install the SDK packages on a fresh agent:

```bash
export ANDROID_HOME=/opt/android-sdk
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
yes | sdkmanager --licenses
```

## 1.2 Signing & secrets (do this before the first prod build)

Release APKs are signed using:

- `keystore.properties` (repo root) — keystore coordinates
- `truckdoc-release-key.keystore.jks` (repo root) — the keystore

`keystore.properties` format:

```properties
storePassword=********
keyPassword=********
keyAlias=truckdoc-release-key
storeFile=../../truckdoc-release-key.keystore.jks
```

> **IMPORTANT — production hardening:** the keystore and its passwords are currently committed to the repository. For production, **remove them from git** and inject them from **Jenkins Credentials** instead:
>
> 1. Jenkins → *Manage Jenkins* → *Credentials* → add:
>    - `truckdoc-keystore` → **Secret file** = the `.jks`
>    - `truckdoc-keystore-props` → **Secret file** = a `keystore.properties` whose `storeFile` points at the injected keystore path
> 2. Bind them in the pipeline with `withCredentials` (see the prod Jenkinsfile below) so they are written into the workspace only at build time and never stored in SCM.

Both the main app and the updater read this same `keystore.properties` (the updater was migrated to use it instead of its old hard-coded passwords). Confirm with the product owner whether the updater must be signed with a **different** key than the main app; if so, give it its own credentials and signing config.

## 1.3 Production Jenkinsfile (main app + updater)

The repository already contains a root `Jenkinsfile` that builds the main app. Below is the **production** version that builds, verifies, and packages **both** apps and injects signing material from credentials. Replace the root `Jenkinsfile` with this for production, or keep it as `jenkins/Jenkinsfile.prod`.

```groovy
pipeline {
    agent any

    environment {
        ANDROID_HOME  = "${env.ANDROID_HOME ?: '/opt/android-sdk'}"
        GRADLE_OPTS   = '-Dorg.gradle.daemon=false -Dfile.encoding=UTF-8'
        BUILD_VERSION = "${env.BUILD_NUMBER}"
    }

    options {
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    triggers { pollSCM('H/15 * * * *') }

    stages {
        stage('Checkout') { steps { checkout scm } }

        stage('Inject signing material') {
            steps {
                withCredentials([
                    file(credentialsId: 'truckdoc-keystore',       variable: 'KS_FILE'),
                    file(credentialsId: 'truckdoc-keystore-props', variable: 'KS_PROPS')
                ]) {
                    sh '''
                        set -e
                        cp "$KS_FILE"  truckdoc-release-key.keystore.jks
                        cp "$KS_PROPS" keystore.properties
                    '''
                }
            }
        }

        stage('Setup Environment') {
            steps {
                sh '''
                    set -e
                    echo "ANDROID_HOME=$ANDROID_HOME"
                    java -version
                    chmod +x gradlew
                    [ -f local.properties ] || echo "sdk.dir=$ANDROID_HOME" > local.properties
                    ./gradlew --version
                '''
            }
        }

        stage('Clean') { steps { sh './gradlew clean --no-daemon' } }

        stage('Build Main App')  { steps { sh './gradlew :app:assembleRelease --stacktrace --no-daemon' } }
        stage('Build Updater')   { steps { sh './gradlew :truckdoc-client-updater:assembleRelease --stacktrace --no-daemon' } }

        stage('Verify & Sign-check') {
            steps {
                sh '''
                    set -e
                    AAPT=$(ls $ANDROID_HOME/build-tools/*/aapt | sort -V | tail -1)
                    APKSIGNER=$(ls $ANDROID_HOME/build-tools/*/apksigner | sort -V | tail -1)

                    APP_APK=$(find truckdoc-client-m2/application/build/outputs/apk -name "*.apk" | head -1)
                    UPD_APK=$(find truckdoc-client-m2/truckdoc-client-updater/build/outputs/apk -name "*.apk" | head -1)

                    for APK in "$APP_APK" "$UPD_APK"; do
                        echo "=== $APK ==="
                        "$AAPT" dump badging "$APK" | grep -E "package:|sdkVersion:|targetSdkVersion:"
                        "$APKSIGNER" verify --verbose "$APK" | grep -E "Verifies|v2 scheme"
                    done
                '''
            }
        }

        stage('Package Deliverables') {
            steps {
                sh '''
                    set -e
                    AAPT=$(ls $ANDROID_HOME/build-tools/*/aapt | sort -V | tail -1)
                    APP_APK=$(find truckdoc-client-m2/application/build/outputs/apk -name "*.apk" | head -1)
                    UPD_APK=$(find truckdoc-client-m2/truckdoc-client-updater/build/outputs/apk -name "*.apk" | head -1)

                    APP_VN=$("$AAPT" dump badging "$APP_APK" | grep -o "versionName='[^']*'" | head -1 | cut -d"'" -f2)
                    UPD_VN=$("$AAPT" dump badging "$UPD_APK" | grep -o "versionName='[^']*'" | head -1 | cut -d"'" -f2)

                    mkdir -p deployment/${BUILD_VERSION}
                    cp "$APP_APK" "deployment/${BUILD_VERSION}/truckdoc-main-${APP_VN}-${BUILD_VERSION}.apk"
                    cp "$UPD_APK" "deployment/${BUILD_VERSION}/truckdoc-updater-${UPD_VN}-${BUILD_VERSION}.apk"

                    {
                      echo "TruckDoc delivery build #${BUILD_VERSION}"
                      echo "Main app : $APP_VN  ($APP_APK)"
                      echo "Updater  : $UPD_VN  ($UPD_APK)"
                      echo "Git      : $(git rev-parse --short HEAD) ($(git rev-parse --abbrev-ref HEAD))"
                      echo "Built    : $(date -u +%Y-%m-%dT%H:%M:%SZ)"
                    } > deployment/${BUILD_VERSION}/build-summary.txt
                    cat deployment/${BUILD_VERSION}/build-summary.txt
                '''
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'deployment/**/*', fingerprint: true
            echo 'Both deliverables archived — ready for the buyer.'
        }
        always {
            // Remove injected secrets from the workspace.
            sh 'rm -f keystore.properties truckdoc-release-key.keystore.jks || true'
        }
        failure { echo 'Build failed — deliverables NOT produced.' }
    }
}
```

## 1.4 Create the Jenkins job

1. *New Item* → **Multibranch Pipeline** (recommended) or **Pipeline**.
2. **Branch Sources / SCM:** the Git repo URL; credentials for the repo.
3. **Build configuration:** *by Jenkinsfile*, script path `Jenkinsfile`.
4. Add the two **Credentials** from [§1.2](#12-signing--secrets-do-this-before-the-first-prod-build).
5. (Optional) **Build triggers:** poll SCM `H/15 * * * *`, or a Git webhook.
6. *Save* → *Build Now*.

## 1.5 Build outputs

| App | Task | Output APK |
|---|---|---|
| Main app | `:app:assembleRelease` | `truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/com.sanda.truckdoc.client-<versionCode>.apk` |
| Updater | `:truckdoc-client-updater:assembleRelease` | `truckdoc-client-m2/truckdoc-client-updater/build/outputs/apk/release/com.sanda.truckdoc.client.updater-<versionCode>.apk` |

Both are **v2-signed**, `targetSdk 35`. The pipeline copies them (renamed with versionName + build number) into `deployment/<build>/` and archives them as Jenkins artifacts.

Version numbers live in each module's `build.gradle` `defaultConfig` (`versionCode` / `versionName`): main app `39 / 3.9`, updater `7 / 2.5`.

## 1.6 Build times & caching

| Scenario | Approx. time |
|---|---|
| **Cold agent** (downloads Gradle + all deps) | ~4–5 min |
| **Warm agent** (populated `GRADLE_USER_HOME`) | ~1–2 min |

To keep builds fast, give the agent a **persistent Gradle cache** (e.g. a dedicated, non-shared `GRADLE_USER_HOME` volume per agent). Do **not** share a live Gradle cache between concurrent builds — it causes journal-lock contention.

## 1.7 (Optional) Verify the pipeline locally with Docker

```bash
docker build -t truckdoc-jenkins:test -f jenkins/Dockerfile .   # FROM jenkins/jenkins:lts-jdk17 + workflow-aggregator plugin
docker run -d --name jenkins -p 8080:8080 \
  -v $PWD:/workspace ... -v <android-sdk>:/opt/android-sdk:ro \
  truckdoc-jenkins:test
```
Seed a job that points at the `Jenkinsfile`, trigger it, and confirm `Finished: SUCCESS`. (A clean run was validated this way: both APKs built, signed, and archived.)

---

# Part 2 — Android installation & configuration

## 2.1 Supported devices

- **Android 8.0 (API 26) and newer** — both apps (`minSdk 26`).
- Built and tested against **Android 15 (API 35)**.

## 2.2 Installation order

1. Install the **main app** (`com.sanda.truckdoc.client.default`).
2. Install the **updater** (`com.sanda.truckdoc.client.updater`).
3. Launch the **main app once** so it writes `/sdcard/TruckDoc/.shared.properties` (this tells the updater which package to manage).

Both APKs are signed and can be installed via `adb install`, MDM push, or sideload:

```bash
adb install -r truckdoc-main-3.9-<build>.apk
adb install -r truckdoc-updater-2.5-<build>.apk
```

## 2.3 Main app configuration

### API endpoint
The mobile API base URL is in `truckdoc-client-m2/application/src/main/res/raw/service.properties`:

```properties
api_service_path=https://mobile.aps-solver.com/mobile-api/
```

For production this should point at the production API host. (Change it before building if a different environment is required.)

### Registration
The app is device-registered against the server using a **registration code** issued by the back office:
1. Launch the app → it detects the device is unregistered.
2. Enter the **registration code** (e.g. scanned or typed).
3. On success the server returns the device's login/secret keys, stored on-device; the app then syncs contacts, messages, maintenance config, and instructions.

### Runtime permissions to grant
Grant these when prompted (or pre-grant via MDM):

| Permission | Why |
|---|---|
| Location (fine/coarse) | GPS reporting / route features |
| Camera | Document & scene photos, scanning |
| SMS (receive/read/send) | SMS backup channel |
| Phone state | Device identifiers / registration |
| **All files access** (`MANAGE_EXTERNAL_STORAGE`) | Reads/writes `/sdcard/TruckDoc/` (shared with the updater) |
| Notifications (Android 13+) | Foreground sync & new-message notifications |
| Alarms & reminders / exact alarm | Scheduled sync checks |
| Display over other apps | "No connection" floating help overlays |
| Disable battery optimization (recommended) | Keeps background sync reliable |

## 2.4 Updater configuration

### Update server
The updater polls **`http://tcus.truckdoc.ru/check_update`** (TruckDoc Client Update Server). Cleartext HTTP to this host is explicitly allowed via the app's network-security config; everything else stays HTTPS-only.

### Target package (automatic)
The updater learns which app to update from `/sdcard/TruckDoc/.shared.properties` (`clientTargetPackage`), which the **main app writes on launch** — so no manual configuration is required as long as the main app has been opened once.

### Updater settings screen
The updater's launcher activity (*TruckDoc Updater* → Settings) exposes:
- **Use Wi‑Fi only** for downloads (recommended for metered SIMs).

### Required permission — install unknown apps
To install updates, the user must allow the updater to install apps:
- **Settings → Apps → TruckDoc Updater → Install unknown apps → Allow** (this backs the `REQUEST_INSTALL_PACKAGES` permission).
- Also grant **All files access** (shared `/sdcard/TruckDoc/`) and allow it to run in the background.

### How updating works
1. `BootAndUpdateReceiver` fires on **boot** and on **app-replaced** events; the app can also check on a schedule.
2. `UpdaterIntentService` (a foreground service, type `dataSync`) calls `check_update` with the current versionCode/versionName, device IDs, etc.
3. If the server reports a newer build, the updater downloads the APK to `/sdcard/TruckDoc/` and launches the system installer (via a `FileProvider` content URI).
4. The user confirms installation; the new main-app version is installed.

## 2.5 Shared-storage dependency (important)

Both apps coordinate through **`/sdcard/TruckDoc/`** (the `.shared.properties` handshake and the downloaded APK). On modern Android this requires **All files access** for both apps. If updates "don't find the target package," verify:
- the main app was launched at least once, and
- both apps have **All files access** granted.

> Roadmap note: this broad-storage model (`MANAGE_EXTERNAL_STORAGE`) is increasingly restricted by Google Play. For a Play-distributed build, plan to move the handoff to an app-to-app mechanism (content provider / intent) instead of shared external storage. For **sideloaded/MDM** distribution it works as-is.

## 2.6 First-run checklist (per device)

- [ ] Install main app, then updater.
- [ ] Launch main app once (writes `.shared.properties`).
- [ ] Grant main app: location, camera, SMS, all-files, notifications, exact alarm, overlay.
- [ ] Register the device with its registration code; confirm contacts/messages sync.
- [ ] Grant updater: install-unknown-apps, all-files, background run.
- [ ] (Optional) set updater "Wi‑Fi only".
- [ ] Trigger/await an update check and confirm it reaches `tcus.truckdoc.ru`.

---

# Part 3 — Releasing a new version

1. Bump `versionCode` (and `versionName`) in the relevant module's `build.gradle`:
   - main app → `truckdoc-client-m2/application/build.gradle`
   - updater → `truckdoc-client-m2/truckdoc-client-updater/build.gradle`
2. Merge to the release branch → Jenkins builds and archives both signed APKs.
3. Publish the **main app** APK to the **TCUS** update server so `check_update` advertises the new `applicationCode`; deliver the **updater** APK only when the updater itself changes.
4. Devices pick up the new main-app version automatically via the updater.

---

# Appendix — Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| Gradle build fails: *Java home supplied is invalid* | A stale `org.gradle.java.home` in `GRADLE_USER_HOME/gradle.properties`. Remove it or pass `-Dorg.gradle.java.home=$JAVA_HOME`. |
| *Timeout waiting to lock journal cache* | A concurrent Gradle process shares the cache. Use a per-agent `GRADLE_USER_HOME`; don't share live caches. |
| R8 *Missing class* errors | Add `-dontwarn`/keep rules in the module `proguard-rules.pro` (see the existing rules). |
| APK won't sign in Jenkins | Signing credentials not injected; verify `keystore.properties` + `.jks` exist in the workspace at build time. |
| Updater never updates | Main app not launched (no `.shared.properties`), updater lacks *All files access* or *Install unknown apps*, or `tcus.truckdoc.ru` unreachable. |
| Cleartext error in updater | Ensure the network-security config (`res/xml/network_security_config.xml`) is present and the manifest references it. |
| Foreground-service crash on Android 14+ | Confirm the updater service declares `android:foregroundServiceType="dataSync"` and holds `FOREGROUND_SERVICE_DATA_SYNC`. |
