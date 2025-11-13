# Jenkins Release Pipeline (GHA-inspired)

This folder contains a Jenkins declarative pipeline that mirrors a multi-stage GitHub Actions workflow. Its goal is to produce signed release APKs for the primary TruckDoc Android app and publish them in a location that the `app-updater` module can consume later.

## What the Pipeline Does

1. **Checks out** the repository (same as GHA `actions/checkout`).
2. **Prints environment diagnostics** (Java, Android SDK, Gradle versions).
3. **Warm ups the Gradle cache** via `./gradlew help` to download dependencies once per build.
4. **Builds the release variant** using `./gradlew :app:assembleDefaultClientRelease`.
5. **Archives the APKs** produced at `truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/` so that Jenkins build artifacts expose them for the `app-updater`.
6. **Optionally publishes** the freshest APK to a GitHub Release using the `gh` CLI, keeping distribution aligned with existing GHA-based flows.

## Jenkins Requirements

- **Agent tooling**
  - JDK 17 available (defaults to `/opt/android/jdk-17`).
  - Android SDK installed (defaults to `/opt/android/sdk`).
  - Gradle wrapper (`./gradlew`) executable.
  - GitHub CLI (`gh`) when publishing releases.
- **Credentials**
  - A Jenkins string credential named `gh-cli-token` that holds a GitHub Personal Access Token with `repo` scope (used when `PUBLISH_GITHUB_RELEASE=true`).

Update the `environment` block in the `Jenkinsfile` if your paths or credential IDs differ.

## Parameters

| Parameter | Description |
|-----------|-------------|
| `PUBLISH_GITHUB_RELEASE` | Toggle GitHub Release publishing. False archives artifacts only. |
| `RELEASE_TAG` | Git tag for the GitHub Release (required when publishing). |
| `RELEASE_TITLE` | Release title shown on GitHub. |
| `RELEASE_NOTES` | Release body/notes. |

## Usage Steps

1. Create a new Pipeline job in Jenkins.
2. Point **Script Path** to `ci/jenkins-github-release/Jenkinsfile`.
3. Configure agent labels or nodes that satisfy the tooling requirements.
4. (Optional) Add the `gh-cli-token` credential for GitHub publishing.
5. Trigger the job manually or via SCM webhooks. After the build finishes, download the archived APKs directly from Jenkins or fetch them from the GitHub Release.

## Artifact Availability for `app-updater`

The APKs are archived with `archiveArtifacts` and can therefore be accessed through Jenkins' artifact download URLs. These URLs can be consumed by the `app-updater` mobile module (or its backend) to deliver updates to end users.

## Local Jenkins Setup on macOS (and APK URL Smoke Test)

The steps below spin up a disposable Jenkins controller on macOS, run the release pipeline, and verify that the archived APK is reachable via HTTP—matching what the `app-updater` will consume in production.

### 1. Install Jenkins

1. Install Java 17 if it is not already available:
   ```bash
   brew install openjdk@17
   ```
   Then export `JAVA_HOME` (add this to `~/.zshrc` or `~/.bashrc` if desired):
   ```bash
   export JAVA_HOME="$(/opt/homebrew/bin/brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
   export PATH="$JAVA_HOME/bin:$PATH"
   ```
2. Install Jenkins LTS via Homebrew:
   ```bash
   brew install jenkins-lts
   ```
3. Start Jenkins:
   ```bash
   brew services start jenkins-lts
   ```
4. Open <http://localhost:8080> and follow the unlock wizard (initial admin password is printed during service start and stored in `/opt/homebrew/var/jenkins/home/secrets/initialAdminPassword`).
5. Install the suggested plugins, create an admin user, and finish the setup flow.

### 2. Prepare Android Tooling on the Jenkins Host

1. Install Android command-line tools:
   ```bash
   brew install --cask android-commandlinetools
   ```
2. Install the required SDK components (API 34 shown here):
   ```bash
   yes | sdkmanager --sdk_root="${HOME}/Library/Android/sdk" \
     "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```
3. Make these paths available to Jenkins by adding them to the Jenkins **Manage Jenkins → System → Global properties → Environment variables** section (matching the defaults in the Jenkinsfile):
   - `JAVA_HOME = $(/opt/homebrew/bin/brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home`
   - `ANDROID_SDK_ROOT = $HOME/Library/Android/sdk`
   - `ANDROID_HOME = $HOME/Library/Android/sdk`

Alternatively, modify the `environment { ... }` block in `Jenkinsfile` to point to your local paths.

### 3. Create the Pipeline Job

1. In Jenkins, create a **Pipeline** job named, for example, `truckdoc-release-local`.
2. Set *Definition* to **Pipeline script from SCM**.
3. Select **Git**, point the repository URL to your local checkout (e.g., `file:///Users/<you>/truckdoc Cursor`), and set the branch as needed.
4. Set **Script Path** to `ci/jenkins-github-release/Jenkinsfile`.
5. Save the job.

### 4. Run the Job and Retrieve the APK URL

1. Trigger **Build Now** on the new job. The first run will download dependencies and generate the release APK under `truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/`.
2. When the build succeeds, open the build page → **Artifacts**. Jenkins exposes each APK with an HTTP link similar to:
   ```
   http://localhost:8080/job/truckdoc-release-local/<build-number>/artifact/truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/com.sanda.truckdoc.client-1.apk
   ```
3. Copy the artifact URL; this is the endpoint the `app-updater` can use in a staging environment.

### 5. Smoke-Test APK Accessibility

Run any of the following commands (replace the URL with your artifact link) to confirm the APK is downloadable:

```bash
curl -I "http://localhost:8080/job/truckdoc-release-local/1/artifact/.../com.sanda.truckdoc.client-1.apk"

# or download a temporary copy
curl -L -o /tmp/truckdoc-client.apk "http://localhost:8080/job/truckdoc-release-local/1/artifact/.../com.sanda.truckdoc.client-1.apk"
```

A successful `curl` response with status `200 OK` (and a non-empty file if you download it) validates that the Jenkins-hosted URL is ready for the `app-updater` module to consume.

