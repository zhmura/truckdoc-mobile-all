// TruckDoc mobile client - delivery pipeline.
//
// Builds and validates the signed release APK that is delivered to the buyer.
// Tuned for the migrated project: AGP 8.5.2 / Gradle 8.7 / JDK 17, single `:app`
// module with the `defaultClient` product flavor (output:
// truckdoc-client-m2/application/build/outputs/apk/defaultClient/release/*.apk).
//
// Jenkins agent prerequisites:
//   - JDK 17 on PATH (AGP 8 requires it)
//   - Android SDK with platform-35 + build-tools 35.x; ANDROID_HOME pointing at it
//   - keystore.properties + the signing keystore present in the workspace (or injected
//     via credentials) so the release variant can be signed.

pipeline {
    agent any

    environment {
        // Point at the agent's Android SDK. Override in Jenkins if different.
        ANDROID_HOME  = "${env.ANDROID_HOME ?: '/opt/android-sdk'}"
        GRADLE_OPTS   = '-Dorg.gradle.daemon=false -Dfile.encoding=UTF-8'
        BUILD_VERSION = "${env.BUILD_NUMBER}"
    }

    options {
        timeout(time: 40, unit: 'MINUTES')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        pollSCM('H/15 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup Environment') {
            steps {
                sh '''
                    set -e
                    echo "=== Environment ==="
                    echo "ANDROID_HOME=$ANDROID_HOME"
                    java -version
                    chmod +x gradlew
                    ./gradlew --version
                    # Ensure the SDK location is known to Gradle if local.properties is absent.
                    if [ ! -f local.properties ]; then
                        echo "sdk.dir=$ANDROID_HOME" > local.properties
                    fi
                '''
            }
        }

        stage('Validate Project') {
            steps {
                sh './gradlew projects --no-daemon'
            }
        }

        stage('Clean') {
            steps {
                sh './gradlew clean --no-daemon'
            }
        }

        stage('Build Release APK') {
            steps {
                // `assembleRelease` resolves to the single `defaultClientRelease` variant.
                sh './gradlew :app:assembleRelease --stacktrace --no-daemon'
            }
        }

        stage('Verify APK & Signature') {
            steps {
                sh '''
                    set -e
                    APK=$(find truckdoc-client-m2/application/build/outputs/apk -name "*.apk" | head -1)
                    if [ -z "$APK" ]; then echo "No APK produced!"; exit 1; fi
                    echo "Built APK: $APK"

                    AAPT=$(ls $ANDROID_HOME/build-tools/*/aapt | sort -V | tail -1)
                    APKSIGNER=$(ls $ANDROID_HOME/build-tools/*/apksigner | sort -V | tail -1)

                    echo "=== APK info ==="
                    "$AAPT" dump badging "$APK" | grep -E "package:|sdkVersion:|targetSdkVersion:"

                    echo "=== Signature (apksigner; the release is v2-signed) ==="
                    "$APKSIGNER" verify --verbose "$APK"
                '''
            }
        }

        stage('Lint') {
            steps {
                // Non-fatal: lint report is informational (abortOnError is disabled in the module).
                sh './gradlew :app:lintDefaultClientRelease --no-daemon || true'
            }
        }

        stage('Package Deliverable') {
            steps {
                sh '''
                    set -e
                    APK=$(find truckdoc-client-m2/application/build/outputs/apk -name "*.apk" | head -1)
                    AAPT=$(ls $ANDROID_HOME/build-tools/*/aapt | sort -V | tail -1)

                    VN=$("$AAPT" dump badging "$APK" | grep -o "versionName='[^']*'" | head -1 | cut -d"'" -f2)
                    VC=$("$AAPT" dump badging "$APK" | grep -o "versionCode='[^']*'" | head -1 | cut -d"'" -f2)

                    mkdir -p deployment/${BUILD_VERSION}
                    cp "$APK" "deployment/${BUILD_VERSION}/truckdoc-client-${VN}-${BUILD_VERSION}.apk"

                    cat > "deployment/${BUILD_VERSION}/build-summary.txt" <<EOF
TruckDoc mobile client - delivery build
========================================
Build number : ${BUILD_VERSION}
versionName  : ${VN}
versionCode  : ${VC}
Git commit   : $(git rev-parse --short HEAD)
Git branch   : $(git rev-parse --abbrev-ref HEAD)
Built at     : $(date -u +"%Y-%m-%dT%H:%M:%SZ")
Artifact     : truckdoc-client-${VN}-${BUILD_VERSION}.apk
EOF
                    echo "=== Deliverable ==="
                    ls -lh "deployment/${BUILD_VERSION}/"
                    cat "deployment/${BUILD_VERSION}/build-summary.txt"
                '''
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'deployment/**/*', fingerprint: true
            echo 'Deliverable archived - ready for the buyer.'
        }
        failure {
            echo 'Pipeline failed - deliverable NOT produced.'
        }
    }
}
