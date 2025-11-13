# Jenkins Pipeline for App Updater Release (GHA style)

This folder contains a self-contained Jenkins pipeline inspired by a typical GitHub Actions (GHA) workflow. It assembles the `app-updater` module in release mode and can optionally publish the generated APK to a GitHub Release using the `gh` CLI.

## Folder Contents

- `Jenkinsfile` – Declarative pipeline that:
  - checks out the repository
  - prints environment diagnostics similar to the "setup" steps in GHA
  - resolves Gradle dependencies
  - runs `./gradlew :app-updater:assembleRelease`
  - archives `app-updater/build/outputs/apk/release/app-updater-release.apk`
  - optionally publishes the release artifact with GitHub CLI

## Jenkins Requirements

1. **Android-ready agent** with:
   - JDK 17 available at `/opt/android/jdk-17`
   - Android SDK at `/opt/android/sdk`
   - (Optional) Android NDK, if native builds are needed
   - Gradle wrapper execution (`./gradlew`) allowed
2. **GitHub CLI (`gh`)** installed on the agent when publishing releases.
3. **Credentials**
   - Create a Jenkins Credential named `gh-cli-token` containing a GitHub personal access token with `repo` scope. The pipeline exports this as `GH_TOKEN` for the `gh` CLI.

If your environment uses different paths or credentials, adjust the `environment` block inside the Jenkinsfile.

## Parameters

The pipeline exposes parameters to mirror flexible release jobs:

| Parameter | Description |
|-----------|-------------|
| `GH_RELEASE_TAG` | Git tag used for the GitHub Release. Leave blank to skip publishing. |
| `GH_RELEASE_TITLE` | Title for the GitHub Release (default: `TruckDoc App Updater Release`). |
| `GH_RELEASE_NOTES` | Body text for the GitHub Release notes. |

## Publishing Flow

1. Provide a value for `GH_RELEASE_TAG` (e.g., `app-updater-v1.2.3`).
2. Ensure Jenkins can authenticate to GitHub via the `gh-cli-token` credential.
3. The pipeline removes any existing release with the same tag, recreates it, and uploads the release APK.
4. All generated artifacts are archived in Jenkins for traceability.

## Usage

1. In Jenkins, create a new Pipeline job.
2. Point the job to this repository and set the script path to `ci/jenkins-gha/Jenkinsfile`.
3. Configure the required credentials and environment.
4. Trigger the job manually or via webhook/SCM polling.

The pipeline retains a structure that can be easily translated back to a GitHub Actions workflow if needed, making it a convenient bridge between GHA-style automation and Jenkins infrastructure.
