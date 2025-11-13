# Local Jenkins Setup & APK Accessibility Test

This guide walks through spinning up a local Jenkins instance, wiring it to the pipeline located in `ci/jenkins-github-release/`, and verifying that the archived release APK can be downloaded (a prerequisite for the `app-updater` module).

## 1. Start Jenkins Locally

The easiest path is Docker. If you prefer a manual installation, ensure Jenkins LTS with JDK 17 and Android SDK toolchains are available.

```bash
# Pull the official Jenkins LTS controller image
docker pull jenkins/jenkins:lts

# Run Jenkins, mapping ports and a volume for persistence
# Adjust the host path "~/jenkins-home" if you want a different location
docker run -d \
  --name jenkins-local \
  -p 8080:8080 \
  -p 50000:50000 \
  -v ~/jenkins-home:/var/jenkins_home \
  jenkins/jenkins:lts
```

After the container starts, browse to `http://localhost:8080`. Unlock Jenkins using the initial admin password printed by `docker logs jenkins-local` or stored under `~/jenkins-home/secrets/initialAdminPassword`.

## 2. Install Required Jenkins Plugins

Install these plugins via **Manage Jenkins → Plugins**:

- _Pipeline_ (usually preinstalled)
- _Credentials Binding_
- _AnsiColor_
- _Git_ (for SCM checkout)

If you plan to publish to GitHub releases:

- _GitHub Branch Source_
- _GitHub_ (adds credential helpers)

## 3. Provide Tooling Inside the Jenkins Agent

The pipeline assumes:

- JDK 17 available (defaults to `/opt/android/jdk-17`).
- Android SDK available (defaults to `/opt/android/sdk`).
- `./gradlew` executable within the workspace.
- GitHub CLI (`gh`) when publishing releases.

For a Docker-based controller, install these tools inside the container or, preferably, attach a dedicated build agent with the Android toolchain preconfigured. Update the `environment` block inside the Jenkinsfile if your paths differ.

Example (inside container):

```bash
# Inside the Jenkins container
apt-get update && apt-get install -y unzip openjdk-17-jdk curl git
# Install Android command line tools to /opt/android/sdk (adjust as needed)
# ... download and unzip cmdline-tools, accept licenses, install platforms/build-tools
# Install GitHub CLI if publishing releases
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | \
  tee /usr/share/keyrings/githubcli-archive-keyring.gpg >/dev/null
apt-get install -y gh
```

## 4. Configure Credentials

Navigate to **Manage Jenkins → Credentials** and add:

- **GitHub PAT** (string credential) named `gh-cli-token` with `repo` scope — required only if you intend to publish releases (`PUBLISH_GITHUB_RELEASE=true`).

## 5. Create the Pipeline Job

1. From the Jenkins dashboard, select **New Item** → **Pipeline**.
2. Name it (e.g., `truckdoc-release-build`).
3. Under **Pipeline**, choose “Pipeline script from SCM”.
4. Set **SCM** to Git and supply the repository URL.
5. For **Script Path**, use `ci/jenkins-github-release/Jenkinsfile`.
6. Save the job.

## 6. Trigger a Build

- Click **Build with Parameters**.
- Choose whether to enable `PUBLISH_GITHUB_RELEASE`.
  - If true, fill in `RELEASE_TAG`, `RELEASE_TITLE`, and optionally `RELEASE_NOTES`.
- Start the build and monitor the console output for progress.

## 7. Locate the Archived APK

Upon success, Jenkins archives the release APK in:

```
truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/*.apk
```

These artifacts are downloadable from the Jenkins build page under **Artifacts**.

## 8. Test APK URL Accessibility

To emulate the `app-updater` fetching the APK:

1. Navigate to the build’s **Artifacts** section and copy the direct download URL for the release APK (e.g., `http://localhost:8080/job/truckdoc-release-build/lastSuccessfulBuild/artifact/...apk`).
2. Test with `curl` (or any HTTP client/as admin):

   ```bash
   curl -L -o /tmp/truckdoc-release.apk \
     "http://localhost:8080/job/truckdoc-release-build/lastSuccessfulBuild/artifact/truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/com.sanda.truckdoc.client-1.apk"
   ```

   Look for HTTP 200 status and confirm the downloaded file size is reasonable (tens of MBs). If Jenkins uses authentication, supply `--user USER:API_TOKEN` or configure anonymous read access.

3. Optionally ping the Jenkins artifact endpoint from a mobile device or emulator if you plan to supply the URL directly to the updater.

## 9. (Optional) Validate GitHub Release Publishing

If `PUBLISH_GITHUB_RELEASE` was enabled, verify the release exists on GitHub:

```bash
gh release view <TAG>
```

Ensure the APK asset appears under the release and is downloadable.

## 10. Clean Up

When finished experimenting locally:

```bash
docker stop jenkins-local && docker rm jenkins-local
```

Remove the `~/jenkins-home` directory if you no longer need the Jenkins state.

---
With this setup, you have a repeatable local Jenkins pipeline that mimics the GitHub Actions approach and exposes APK download URLs suitable for integration with the `app-updater` module.

