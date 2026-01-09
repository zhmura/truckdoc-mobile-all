# Jenkins Build & Nginx Deployment Guide

## Overview

This guide provides step-by-step instructions for setting up Jenkins CI/CD pipeline for the TruckDoc mobile application and configuring a private nginx file server to host APK files for the updater app.

## Table of Contents

1. [Jenkins Server Setup](#jenkins-server-setup)
2. [Jenkins Pipeline Configuration](#jenkins-pipeline-configuration)
3. [Nginx File Server Setup](#nginx-file-server-setup)
4. [APK Distribution Configuration](#apk-distribution-configuration)
5. [Updater App Integration](#updater-app-integration)
6. [Security Considerations](#security-considerations)
7. [Monitoring and Maintenance](#monitoring-and-maintenance)
8. [Troubleshooting](#troubleshooting)

## Jenkins Server Setup

### Prerequisites

- Ubuntu 20.04+ or CentOS 8+ server (Production)
- Windows 10/11 (Development/Local setup)
- Minimum 4GB RAM, 20GB storage
- Java 11 or 17
- Docker (optional, for containerized deployment)

### Installation

#### 1. Install Java
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel
```

#### 2. Install Jenkins
```bash
# Add Jenkins repository
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee \
  /usr/share/keyrings/jenkins-keyring.asc > /dev/null

echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

# Install Jenkins
sudo apt update
sudo apt install jenkins

# Start and enable Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins
```

#### 3. Initial Setup
```bash
# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# Access Jenkins at http://your-server-ip:8080
# Follow the setup wizard to install recommended plugins
```

### Windows Local Development Setup

For local development and testing on Windows, follow these steps:

#### 1. Install Java on Windows
```powershell
# Download OpenJDK 17 from Adoptium
# https://adoptium.net/temurin/releases/?version=17

# Or use Chocolatey (if installed)
choco install temurin17

# Or use winget
winget install EclipseAdoptium.Temurin.17.JDK

# Verify installation
java -version
javac -version
```

#### 2. Install Jenkins on Windows

##### Method A: Using Windows Installer
```powershell
# Download Jenkins Windows installer
# https://www.jenkins.io/download/lts/windows/

# Run the installer as Administrator
# Follow the installation wizard
# Default installation path: C:\Program Files\Jenkins
```

##### Method B: Using Chocolatey
```powershell
# Install Chocolatey first (if not installed)
# https://chocolatey.org/install

# Install Jenkins
choco install jenkins

# Start Jenkins service
Start-Service jenkins
```

##### Method C: Manual Installation
```powershell
# Create Jenkins directory
New-Item -ItemType Directory -Path "C:\Jenkins" -Force

# Download Jenkins WAR file
Invoke-WebRequest -Uri "https://get.jenkins.io/war-stable/latest/jenkins.war" -OutFile "C:\Jenkins\jenkins.war"

# Create batch file to start Jenkins
@"
@echo off
cd /d C:\Jenkins
java -jar jenkins.war --httpPort=8080
"@ | Out-File -FilePath "C:\Jenkins\start-jenkins.bat" -Encoding ASCII

# Create Windows Service (optional)
# Download and install NSSM (Non-Sucking Service Manager)
# nssm install Jenkins "C:\Program Files\Java\jdk-17\bin\java.exe" "-jar C:\Jenkins\jenkins.war --httpPort=8080"
# nssm set Jenkins AppDirectory C:\Jenkins
# nssm start Jenkins
```

#### 3. Windows-Specific Configuration

##### Environment Variables
```powershell
# Set JAVA_HOME
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")

# Set ANDROID_HOME (for Android builds)
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android\Sdk", "Machine")

# Set GRADLE_HOME
[Environment]::SetEnvironmentVariable("GRADLE_HOME", "C:\Gradle\gradle-8.0", "Machine")

# Add to PATH
$path = [Environment]::GetEnvironmentVariable("PATH", "Machine")
[Environment]::SetEnvironmentVariable("PATH", "$path;C:\Android\Sdk\platform-tools;C:\Gradle\gradle-8.0\bin", "Machine")
```

##### Jenkins Home Directory
```powershell
# Set JENKINS_HOME environment variable
[Environment]::SetEnvironmentVariable("JENKINS_HOME", "C:\Jenkins\jenkins_home", "Machine")

# Create Jenkins home directory
New-Item -ItemType Directory -Path "C:\Jenkins\jenkins_home" -Force
```

#### 4. Start Jenkins on Windows
```powershell
# Method 1: Using batch file
C:\Jenkins\start-jenkins.bat

# Method 2: Direct command
cd C:\Jenkins
java -jar jenkins.war --httpPort=8080

# Method 3: As Windows Service (if configured)
Start-Service jenkins

# Method 4: Using Chocolatey
jenkins start
```

#### 5. Access Jenkins
```powershell
# Open browser and navigate to
# http://localhost:8080

# Get initial admin password
Get-Content "C:\Jenkins\jenkins_home\secrets\initialAdminPassword"
# or
Get-Content "$env:JENKINS_HOME\secrets\initialAdminPassword"
```

#### 6. Windows-Specific Setup

##### Install Required Tools
```powershell
# Install Git for Windows
winget install Git.Git
# or
choco install git

# Install Android Studio (for Android SDK)
winget install Google.AndroidStudio
# or download from https://developer.android.com/studio

# Install Gradle
winget install Gradle.Gradle
# or
choco install gradle

# Install 7-Zip (for file operations)
winget install 7zip.7zip
# or
choco install 7zip
```

##### Configure Android SDK
```powershell
# Set Android SDK location
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

# Install required SDK components
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" --update
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools" "platforms;android-33" "build-tools;33.0.0"

# Accept licenses
& "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
```

#### 7. Windows-Specific Jenkinsfile

For Windows local development, create a `Jenkinsfile.windows`:

```groovy
pipeline {
    agent any
    
    environment {
        ANDROID_HOME = 'C:\\Android\\Sdk'
        GRADLE_HOME = 'C:\\Gradle\\gradle-8.0'
        PATH = "${GRADLE_HOME}\\bin;${ANDROID_HOME}\\tools;${ANDROID_HOME}\\platform-tools;${PATH}"
        
        // Build configuration
        BUILD_VARIANT = 'defaultClientRelease'
        APK_NAME = 'truckdoc-mobile'
        
        // Local development paths
        LOCAL_APK_PATH = 'C:\\Jenkins\\apks'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Setup Environment') {
            steps {
                script {
                    // Create local APK directory
                    bat '''
                        if not exist "%LOCAL_APK_PATH%" mkdir "%LOCAL_APK_PATH%"
                        echo "Local APK directory created: %LOCAL_APK_PATH%"
                    '''
                }
            }
        }
        
        stage('Build Application') {
            steps {
                script {
                    bat '''
                        echo "Building TruckDoc application on Windows..."
                        gradlew.bat clean
                        gradlew.bat assemble%BUILD_VARIANT%
                        echo "Build completed successfully"
                    '''
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    bat '''
                        echo "Running unit tests..."
                        gradlew.bat test%BUILD_VARIANT%UnitTest
                        
                        echo "Running instrumented tests..."
                        gradlew.bat connected%BUILD_VARIANT%AndroidTest
                    '''
                }
            }
        }
        
        stage('Sign APK') {
            steps {
                script {
                    bat '''
                        echo "Signing APK on Windows..."
                        
                        REM Create keystore if it doesn't exist
                        if not exist "keystore.jks" (
                            keytool -genkey -v -keystore keystore.jks ^
                                -keyalg RSA -keysize 2048 -validity 10000 ^
                                -alias truckdoc -storepass truckdoc123 ^
                                -keypass truckdoc123 ^
                                -dname "CN=TruckDoc, OU=Development, O=TruckDoc, L=City, S=State, C=US"
                        )
                        
                        REM Sign the APK
                        jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 ^
                            -keystore keystore.jks ^
                            -storepass truckdoc123 ^
                            -keypass truckdoc123 ^
                            truckdoc-client-m2\\application\\build\\outputs\\apk\\%BUILD_VARIANT%\\application-%BUILD_VARIANT%.apk ^
                            truckdoc
                        
                        REM Optimize APK
                        "%ANDROID_HOME%\\build-tools\\33.0.0\\zipalign.exe" -v 4 ^
                            truckdoc-client-m2\\application\\build\\outputs\\apk\\%BUILD_VARIANT%\\application-%BUILD_VARIANT%.apk ^
                            truckdoc-client-m2\\application\\build\\outputs\\apk\\%BUILD_VARIANT%\\application-%BUILD_VARIANT%-aligned.apk
                    '''
                }
            }
        }
        
        stage('Deploy Locally') {
            steps {
                script {
                    bat '''
                        echo "Deploying APK to local directory..."
                        
                        REM Copy APK to local directory
                        copy "truckdoc-client-m2\\application\\build\\outputs\\apk\\%BUILD_VARIANT%\\application-%BUILD_VARIANT%-aligned.apk" ^
                             "%LOCAL_APK_PATH%\\%APK_NAME%-%VERSION_NAME%.apk"
                        
                        REM Generate build info
                        echo { > "%LOCAL_APK_PATH%\\build-info-%VERSION_NAME%.json"
                        echo   "versionName": "%VERSION_NAME%", >> "%LOCAL_APK_PATH%\\build-info-%VERSION_NAME%.json"
                        echo   "versionCode": %VERSION_CODE%, >> "%LOCAL_APK_PATH%\\build-info-%VERSION_NAME%.json"
                        echo   "buildNumber": %BUILD_NUMBER%, >> "%LOCAL_APK_PATH%\\build-info-%VERSION_NAME%.json"
                        echo   "buildDate": "%date% %time%", >> "%LOCAL_APK_PATH%\\build-info-%VERSION_NAME%.json"
                        echo   "downloadUrl": "file://%LOCAL_APK_PATH%\\%APK_NAME%-%VERSION_NAME%.apk" >> "%LOCAL_APK_PATH%\\build-info-%VERSION_NAME%.json"
                        echo } >> "%LOCAL_APK_PATH%\\build-info-%VERSION_NAME%.json"
                        
                        echo "Local deployment completed"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            // Clean workspace
            cleanWs()
        }
        success {
            echo "Build completed successfully on Windows!"
        }
        failure {
            echo "Build failed on Windows!"
        }
    }
}
```

#### 8. Windows Troubleshooting

##### Common Windows Issues
```powershell
# Issue: Jenkins won't start
# Solution: Check Java installation
java -version

# Issue: Permission denied
# Solution: Run as Administrator
Start-Process powershell -Verb RunAs

# Issue: Port 8080 already in use
# Solution: Change port or kill process
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Issue: Android SDK not found
# Solution: Set environment variables
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

# Issue: Gradle not found
# Solution: Install Gradle or use wrapper
./gradlew --version

# Issue: Git not found
# Solution: Install Git for Windows
winget install Git.Git
```

##### Windows Performance Optimization
```powershell
# Disable Windows Defender for Jenkins directories (development only)
Add-MpPreference -ExclusionPath "C:\Jenkins"
Add-MpPreference -ExclusionPath "C:\Users\$env:USERNAME\.gradle"

# Increase Jenkins heap size
# Edit C:\Jenkins\start-jenkins.bat
# Add: set JAVA_OPTS=-Xmx2048m -Xms1024m

# Use SSD for Jenkins workspace
# Set JENKINS_HOME to SSD drive
[Environment]::SetEnvironmentVariable("JENKINS_HOME", "D:\Jenkins\jenkins_home", "Machine")
```

### Required Jenkins Plugins

Install the following plugins via Jenkins Plugin Manager:

- **Android Lint Plugin**: Android code analysis
- **Gradle Plugin**: Gradle build support
- **Git Plugin**: Git integration
- **Credentials Plugin**: Secure credential management
- **Pipeline Plugin**: Pipeline as code support
- **Workspace Cleanup Plugin**: Clean workspace after builds
- **Copy Artifact Plugin**: Copy build artifacts
- **SSH Agent Plugin**: SSH operations
- **HTTP Request Plugin**: HTTP operations for deployment

## Jenkins Pipeline Configuration

### 1. Create Jenkinsfile

Create a `Jenkinsfile` in the root of your project:

```groovy
pipeline {
    agent any
    
    environment {
        ANDROID_HOME = '/opt/android-sdk'
        GRADLE_HOME = '/opt/gradle'
        PATH = "${GRADLE_HOME}/bin:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${PATH}"
        
        // Build configuration
        BUILD_VARIANT = 'defaultClientRelease'
        APK_NAME = 'truckdoc-mobile'
        
        // Nginx server configuration
        NGINX_SERVER = 'your-nginx-server.com'
        NGINX_USER = 'jenkins'
        NGINX_PATH = '/var/www/apks'
        
        // Version management
        VERSION_CODE = "${env.BUILD_NUMBER}"
        VERSION_NAME = "1.0.${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Setup Environment') {
            steps {
                script {
                    // Setup Android SDK and Gradle
                    sh '''
                        echo "Setting up Android SDK..."
                        if [ ! -d "$ANDROID_HOME" ]; then
                            mkdir -p $ANDROID_HOME
                            wget https://dl.google.com/android/repository/commandlinetools-linux-8512546_latest.zip
                            unzip commandlinetools-linux-8512546_latest.zip -d $ANDROID_HOME
                            rm commandlinetools-linux-8512546_latest.zip
                        fi
                        
                        echo "Setting up Gradle..."
                        if [ ! -d "$GRADLE_HOME" ]; then
                            mkdir -p $GRADLE_HOME
                            wget https://services.gradle.org/distributions/gradle-8.0-bin.zip
                            unzip gradle-8.0-bin.zip -d /opt
                            mv /opt/gradle-8.0 $GRADLE_HOME
                            rm gradle-8.0-bin.zip
                        fi
                    '''
                }
            }
        }
        
        stage('Install Dependencies') {
            steps {
                script {
                    sh '''
                        echo "Installing Android SDK components..."
                        $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --update
                        $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.0"
                        
                        echo "Accepting licenses..."
                        yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
                    '''
                }
            }
        }
        
        stage('Build Application') {
            steps {
                script {
                    sh '''
                        echo "Building TruckDoc application..."
                        chmod +x ./gradlew
                        ./gradlew clean
                        ./gradlew assemble${BUILD_VARIANT}
                        
                        echo "Build completed successfully"
                    '''
                }
            }
        }
        
        stage('Run Tests') {
            steps {
                script {
                    sh '''
                        echo "Running unit tests..."
                        ./gradlew test${BUILD_VARIANT}UnitTest
                        
                        echo "Running instrumented tests..."
                        ./gradlew connected${BUILD_VARIANT}AndroidTest
                    '''
                }
            }
            post {
                always {
                    // Publish test results
                    publishTestResults testResultsPattern: '**/test-results/**/*.xml'
                    publishCoverage adapters: [jacocoAdapter('**/build/reports/jacoco/**/*.xml')]
                }
            }
        }
        
        stage('Code Quality') {
            steps {
                script {
                    sh '''
                        echo "Running Android Lint..."
                        ./gradlew lint${BUILD_VARIANT}
                        
                        echo "Running code analysis..."
                        ./gradlew check
                    '''
                }
            }
            post {
                always {
                    // Publish lint results
                    publishIssues tools: [androidLint(pattern: '**/build/reports/lint-results.xml')]
                }
            }
        }
        
        stage('Sign APK') {
            steps {
                script {
                    sh '''
                        echo "Signing APK..."
                        
                        # Create keystore if it doesn't exist
                        if [ ! -f "keystore.jks" ]; then
                            keytool -genkey -v -keystore keystore.jks \
                                -keyalg RSA -keysize 2048 -validity 10000 \
                                -alias truckdoc -storepass truckdoc123 \
                                -keypass truckdoc123 \
                                -dname "CN=TruckDoc, OU=Development, O=TruckDoc, L=City, S=State, C=US"
                        fi
                        
                        # Sign the APK
                        jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
                            -keystore keystore.jks \
                            -storepass truckdoc123 \
                            -keypass truckdoc123 \
                            truckdoc-client-m2/application/build/outputs/apk/${BUILD_VARIANT}/application-${BUILD_VARIANT}.apk \
                            truckdoc
                        
                        # Optimize APK
                        $ANDROID_HOME/build-tools/33.0.0/zipalign -v 4 \
                            truckdoc-client-m2/application/build/outputs/apk/${BUILD_VARIANT}/application-${BUILD_VARIANT}.apk \
                            truckdoc-client-m2/application/build/outputs/apk/${BUILD_VARIANT}/application-${BUILD_VARIANT}-aligned.apk
                    '''
                }
            }
        }
        
        stage('Generate Build Info') {
            steps {
                script {
                    sh '''
                        echo "Generating build information..."
                        
                        # Create build info file
                        cat > build-info.json << EOF
                        {
                            "versionName": "${VERSION_NAME}",
                            "versionCode": ${VERSION_CODE},
                            "buildNumber": ${env.BUILD_NUMBER},
                            "buildDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
                            "commitHash": "${env.GIT_COMMIT}",
                            "branch": "${env.GIT_BRANCH}",
                            "downloadUrl": "https://${NGINX_SERVER}/apks/${APK_NAME}-${VERSION_NAME}.apk",
                            "releaseNotes": "Build ${env.BUILD_NUMBER} - ${env.GIT_COMMIT}"
                        }
                        EOF
                        
                        # Generate checksum
                        sha256sum truckdoc-client-m2/application/build/outputs/apk/${BUILD_VARIANT}/application-${BUILD_VARIANT}-aligned.apk > checksum.sha256
                    '''
                }
            }
        }
        
        stage('Deploy to Nginx') {
            steps {
                script {
                    sshagent(['nginx-ssh-key']) {
                        sh '''
                            echo "Deploying APK to nginx server..."
                            
                            # Create remote directory if it doesn't exist
                            ssh ${NGINX_USER}@${NGINX_SERVER} "mkdir -p ${NGINX_PATH}"
                            
                            # Copy APK to nginx server
                            scp truckdoc-client-m2/application/build/outputs/apk/${BUILD_VARIANT}/application-${BUILD_VARIANT}-aligned.apk \
                                ${NGINX_USER}@${NGINX_SERVER}:${NGINX_PATH}/${APK_NAME}-${VERSION_NAME}.apk
                            
                            # Copy build info
                            scp build-info.json \
                                ${NGINX_USER}@${NGINX_SERVER}:${NGINX_PATH}/build-info-${VERSION_NAME}.json
                            
                            # Copy checksum
                            scp checksum.sha256 \
                                ${NGINX_USER}@${NGINX_SERVER}:${NGINX_PATH}/${APK_NAME}-${VERSION_NAME}.sha256
                            
                            # Update latest build info
                            ssh ${NGINX_USER}@${NGINX_SERVER} "cp ${NGINX_PATH}/build-info-${VERSION_NAME}.json ${NGINX_PATH}/latest-build.json"
                            
                            echo "Deployment completed successfully"
                        '''
                    }
                }
            }
        }
        
        stage('Update Updater App') {
            steps {
                script {
                    sh '''
                        echo "Updating updater app configuration..."
                        
                        # Update updater app with new build info
                        ssh ${NGINX_USER}@${NGINX_SERVER} "echo '${VERSION_NAME}' > ${NGINX_PATH}/latest-version.txt"
                        
                        # Create update notification
                        cat > update-notification.json << EOF
                        {
                            "version": "${VERSION_NAME}",
                            "buildNumber": ${env.BUILD_NUMBER},
                            "downloadUrl": "https://${NGINX_SERVER}/apks/${APK_NAME}-${VERSION_NAME}.apk",
                            "releaseDate": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
                            "mandatory": false,
                            "description": "New version available"
                        }
                        EOF
                        
                        scp update-notification.json \
                            ${NGINX_USER}@${NGINX_SERVER}:${NGINX_PATH}/update-notification.json
                    '''
                }
            }
        }
    }
    
    post {
        always {
            // Clean workspace
            cleanWs()
        }
        success {
            script {
                // Send success notification
                emailext (
                    subject: "TruckDoc Build #${env.BUILD_NUMBER} - SUCCESS",
                    body: """
                    Build completed successfully!
                    
                    Version: ${VERSION_NAME}
                    Build Number: ${env.BUILD_NUMBER}
                    Download URL: https://${NGINX_SERVER}/apks/${APK_NAME}-${VERSION_NAME}.apk
                    
                    Build URL: ${env.BUILD_URL}
                    """,
                    to: 'dev-team@truckdoc.com'
                )
            }
        }
        failure {
            script {
                // Send failure notification
                emailext (
                    subject: "TruckDoc Build #${env.BUILD_NUMBER} - FAILED",
                    body: """
                    Build failed!
                    
                    Build Number: ${env.BUILD_NUMBER}
                    Build URL: ${env.BUILD_URL}
                    
                    Please check the build logs for details.
                    """,
                    to: 'dev-team@truckdoc.com'
                )
            }
        }
    }
}
```

### 2. Configure Jenkins Credentials

1. **Go to Jenkins Dashboard > Manage Jenkins > Manage Credentials**
2. **Add SSH Credentials**:
   - Kind: SSH Username with private key
   - ID: `nginx-ssh-key`
   - Username: `jenkins`
   - Private Key: Upload your SSH private key
   - Description: SSH key for nginx server

3. **Add Email Credentials** (optional):
   - Kind: Username with password
   - ID: `email-credentials`
   - Username: Your email username
   - Password: Your email password

### 3. Create Jenkins Job

1. **Create New Pipeline Job**:
   - Go to Jenkins Dashboard
   - Click "New Item"
   - Enter job name: `truckdoc-mobile-build`
   - Select "Pipeline"
   - Click "OK"

2. **Configure Pipeline**:
   - Definition: Pipeline script from SCM
   - SCM: Git
   - Repository URL: Your Git repository URL
   - Credentials: Add Git credentials if needed
   - Branch: `*/main` (or your main branch)
   - Script Path: `Jenkinsfile`

3. **Configure Build Triggers**:
   - Poll SCM: `H/15 * * * *` (check every 15 minutes)
   - Or use webhooks for automatic builds

## Nginx File Server Setup

### 1. Install Nginx

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx
```

### 2. Configure Nginx

Create nginx configuration file `/etc/nginx/sites-available/truckdoc-apks`:

```nginx
server {
    listen 80;
    server_name your-nginx-server.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-nginx-server.com;
    
    # SSL Configuration
    ssl_certificate /etc/ssl/certs/truckdoc.crt;
    ssl_certificate_key /etc/ssl/private/truckdoc.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    
    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    
    # APK files directory
    location /apks/ {
        alias /var/www/apks/;
        autoindex on;
        autoindex_exact_size off;
        autoindex_localtime on;
        
        # Allow APK downloads
        location ~* \.apk$ {
            add_header Content-Type application/vnd.android.package-archive;
            add_header Content-Disposition "attachment; filename=$basename";
        }
        
        # Allow JSON and text files
        location ~* \.(json|txt|sha256)$ {
            add_header Content-Type application/json;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
        
        # Basic authentication (optional)
        auth_basic "Restricted Access";
        auth_basic_user_file /etc/nginx/.htpasswd;
    }
    
    # API endpoints for updater app
    location /api/updates/ {
        alias /var/www/apks/;
        
        # Latest build info
        location = /api/updates/latest {
            try_files /latest-build.json =404;
            add_header Content-Type application/json;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
        
        # Update notification
        location = /api/updates/notification {
            try_files /update-notification.json =404;
            add_header Content-Type application/json;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
        
        # Version check
        location = /api/updates/version {
            try_files /latest-version.txt =404;
            add_header Content-Type text/plain;
            add_header Cache-Control "no-cache, no-store, must-revalidate";
        }
    }
    
    # Health check endpoint
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
    
    # Default error pages
    error_page 404 /404.html;
    error_page 500 502 503 504 /50x.html;
    
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

### 3. Setup SSL Certificate

```bash
# Generate self-signed certificate (for testing)
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/truckdoc.key \
    -out /etc/ssl/certs/truckdoc.crt \
    -subj "/C=US/ST=State/L=City/O=TruckDoc/CN=your-nginx-server.com"

# For production, use Let's Encrypt or commercial certificate
```

### 4. Setup Basic Authentication (Optional)

```bash
# Create password file
sudo htpasswd -c /etc/nginx/.htpasswd jenkins
# Enter password when prompted
```

### 5. Create Directory Structure

```bash
# Create APK storage directory
sudo mkdir -p /var/www/apks
sudo chown -R jenkins:jenkins /var/www/apks
sudo chmod 755 /var/www/apks

# Create initial files
sudo -u jenkins touch /var/www/apks/latest-build.json
sudo -u jenkins touch /var/www/apks/update-notification.json
sudo -u jenkins touch /var/www/apks/latest-version.txt
```

### 6. Enable Site and Restart Nginx

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/truckdoc-apks /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Restart nginx
sudo systemctl restart nginx
sudo systemctl enable nginx
```

## APK Distribution Configuration

### 1. Directory Structure

```
/var/www/apks/
├── truckdoc-mobile-1.0.1.apk
├── truckdoc-mobile-1.0.2.apk
├── truckdoc-mobile-1.0.3.apk
├── build-info-1.0.1.json
├── build-info-1.0.2.json
├── build-info-1.0.3.json
├── truckdoc-mobile-1.0.1.sha256
├── truckdoc-mobile-1.0.2.sha256
├── truckdoc-mobile-1.0.3.sha256
├── latest-build.json
├── update-notification.json
└── latest-version.txt
```

### 2. Build Info JSON Format

```json
{
    "versionName": "1.0.3",
    "versionCode": 3,
    "buildNumber": 123,
    "buildDate": "2024-01-15T10:30:00Z",
    "commitHash": "abc123def456",
    "branch": "main",
    "downloadUrl": "https://your-nginx-server.com/apks/truckdoc-mobile-1.0.3.apk",
    "releaseNotes": "Build 123 - abc123def456",
    "fileSize": 52428800,
    "checksum": "sha256:abc123def456..."
}
```

### 3. Update Notification Format

```json
{
    "version": "1.0.3",
    "buildNumber": 123,
    "downloadUrl": "https://your-nginx-server.com/apks/truckdoc-mobile-1.0.3.apk",
    "releaseDate": "2024-01-15T10:30:00Z",
    "mandatory": false,
    "description": "New version available with bug fixes and improvements",
    "minVersion": "1.0.0"
}
```

## Updater App Integration

### 1. Update JenkinsConfig.kt

```kotlin
object JenkinsConfig {
    const val BASE_URL = "https://your-nginx-server.com"
    const val API_BASE_URL = "$BASE_URL/api/updates"
    const val APK_BASE_URL = "$BASE_URL/apks"
    
    // API endpoints
    const val LATEST_BUILD_ENDPOINT = "/latest"
    const val UPDATE_NOTIFICATION_ENDPOINT = "/notification"
    const val VERSION_CHECK_ENDPOINT = "/version"
    
    // Jenkins credentials (if needed)
    const val JENKINS_USERNAME = "jenkins"
    const val JENKINS_API_TOKEN = "your-api-token"
}
```

### 2. Update API Service

```kotlin
interface UpdateApiService {
    @GET("latest")
    suspend fun getLatestBuild(): Response<BuildInfo>
    
    @GET("notification")
    suspend fun getUpdateNotification(): Response<UpdateNotification>
    
    @GET("version")
    suspend fun getLatestVersion(): Response<String>
    
    @GET("apks/{fileName}")
    suspend fun downloadApk(@Path("fileName") fileName: String): Response<ResponseBody>
}
```

### 3. Update Repository

```kotlin
class UpdateRepository @Inject constructor(
    private val apiService: UpdateApiService,
    private val downloadManager: DownloadManager,
    private val preferencesManager: PreferencesManager
) {
    
    suspend fun checkForUpdates(): UpdateResult {
        return try {
            val latestBuild = apiService.getLatestBuild()
            val currentVersion = getCurrentAppVersion()
            
            if (latestBuild.versionCode > currentVersion.versionCode) {
                UpdateResult.UpdateAvailable(latestBuild)
            } else {
                UpdateResult.NoUpdateAvailable
            }
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun downloadUpdate(buildInfo: BuildInfo): DownloadResult {
        return try {
            val fileName = "truckdoc-mobile-${buildInfo.versionName}.apk"
            val downloadUrl = "${JenkinsConfig.APK_BASE_URL}/$fileName"
            
            // Download APK
            val downloadId = downloadManager.downloadApk(downloadUrl, fileName)
            
            // Verify checksum
            val checksum = downloadManager.getFileChecksum(fileName)
            if (checksum == buildInfo.checksum) {
                DownloadResult.Success(fileName)
            } else {
                DownloadResult.Error("Checksum verification failed")
            }
        } catch (e: Exception) {
            DownloadResult.Error(e.message ?: "Download failed")
        }
    }
}
```

## Security Considerations

### 1. Jenkins Security

```bash
# Secure Jenkins installation
sudo ufw allow 8080/tcp
sudo ufw enable

# Use reverse proxy for HTTPS
sudo apt install apache2
sudo a2enmod proxy
sudo a2enmod proxy_http
```

### 2. Nginx Security

```nginx
# Additional security headers
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';";
add_header Referrer-Policy "strict-origin-when-cross-origin";
add_header Permissions-Policy "geolocation=(), microphone=(), camera=()";

# Rate limiting
limit_req_zone $binary_remote_addr zone=apk_downloads:10m rate=10r/s;
location /apks/ {
    limit_req zone=apk_downloads burst=20 nodelay;
    # ... other configuration
}
```

### 3. Access Control

```bash
# IP whitelist (optional)
sudo ufw allow from 192.168.1.0/24 to any port 22
sudo ufw allow from 192.168.1.0/24 to any port 80
sudo ufw allow from 192.168.1.0/24 to any port 443
```

## Monitoring and Maintenance

### 1. Jenkins Monitoring

```bash
# Monitor Jenkins logs
sudo tail -f /var/log/jenkins/jenkins.log

# Monitor disk space
df -h /var/lib/jenkins

# Monitor build history
ls -la /var/lib/jenkins/jobs/truckdoc-mobile-build/builds/
```

### 2. Nginx Monitoring

```bash
# Monitor nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# Monitor disk space
df -h /var/www/apks

# Check nginx status
sudo systemctl status nginx
```

### 3. Automated Cleanup

Create cleanup script `/usr/local/bin/cleanup-old-apks.sh`:

```bash
#!/bin/bash

# Cleanup old APK files (keep last 10 versions)
APK_DIR="/var/www/apks"
KEEP_COUNT=10

echo "Cleaning up old APK files..."

# Remove old APK files
cd $APK_DIR
ls -t *.apk | tail -n +$((KEEP_COUNT + 1)) | xargs -r rm

# Remove corresponding build info files
ls -t build-info-*.json | tail -n +$((KEEP_COUNT + 1)) | xargs -r rm

# Remove corresponding checksum files
ls -t *.sha256 | tail -n +$((KEEP_COUNT + 1)) | xargs -r rm

echo "Cleanup completed"
```

Make it executable and add to crontab:

```bash
chmod +x /usr/local/bin/cleanup-old-apks.sh

# Add to crontab (run daily at 2 AM)
crontab -e
0 2 * * * /usr/local/bin/cleanup-old-apks.sh
```

### 4. Health Checks

Create health check script `/usr/local/bin/health-check.sh`:

```bash
#!/bin/bash

# Check Jenkins
if ! systemctl is-active --quiet jenkins; then
    echo "Jenkins is down!"
    systemctl restart jenkins
fi

# Check nginx
if ! systemctl is-active --quiet nginx; then
    echo "Nginx is down!"
    systemctl restart nginx
fi

# Check disk space
DISK_USAGE=$(df /var/www/apks | tail -1 | awk '{print $5}' | sed 's/%//')
if [ $DISK_USAGE -gt 90 ]; then
    echo "Disk space is low: ${DISK_USAGE}%"
    /usr/local/bin/cleanup-old-apks.sh
fi
```

## Troubleshooting

### Common Issues

#### 1. Jenkins Build Failures

```bash
# Check Jenkins logs
sudo tail -f /var/log/jenkins/jenkins.log

# Check build workspace
ls -la /var/lib/jenkins/workspace/truckdoc-mobile-build/

# Check Gradle cache
rm -rf /var/lib/jenkins/.gradle/caches/
```

#### 2. Nginx Issues

```bash
# Test nginx configuration
sudo nginx -t

# Check nginx error logs
sudo tail -f /var/log/nginx/error.log

# Check file permissions
ls -la /var/www/apks/
sudo chown -R jenkins:jenkins /var/www/apks/
```

#### 3. SSL Certificate Issues

```bash
# Check certificate validity
openssl x509 -in /etc/ssl/certs/truckdoc.crt -text -noout

# Renew Let's Encrypt certificate
sudo certbot renew --nginx
```

#### 4. Network Connectivity

```bash
# Test connectivity to Jenkins
curl -I http://your-jenkins-server:8080

# Test connectivity to nginx
curl -I https://your-nginx-server.com/health

# Check firewall rules
sudo ufw status
```

### Debug Commands

```bash
# Check Jenkins build status
curl -u username:api-token http://jenkins-server:8080/job/truckdoc-mobile-build/lastBuild/api/json

# Check latest build info
curl https://your-nginx-server.com/api/updates/latest

# Check APK file availability
curl -I https://your-nginx-server.com/apks/truckdoc-mobile-1.0.3.apk

# Monitor real-time logs
sudo journalctl -f -u jenkins
sudo journalctl -f -u nginx
```

### Performance Optimization

```bash
# Optimize nginx performance
sudo nano /etc/nginx/nginx.conf

# Add to http block:
gzip on;
gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
client_max_body_size 100M;
```

This comprehensive guide provides everything needed to set up a complete CI/CD pipeline with Jenkins and nginx file server for the TruckDoc mobile application. The setup includes security considerations, monitoring, and troubleshooting to ensure a robust deployment system. 