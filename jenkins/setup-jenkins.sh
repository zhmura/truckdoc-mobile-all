#!/bin/bash

# TruckDoc Mobile - Jenkins Setup Script
# This script sets up the Jenkins environment for building TruckDoc mobile apps

set -e

echo "🚀 Setting up Jenkins environment for TruckDoc Mobile..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -eq 0 ]]; then
   print_error "This script should not be run as root"
   exit 1
fi

# Update system packages
print_status "Updating system packages..."
sudo apt-get update

# Install required packages
print_status "Installing required packages..."
sudo apt-get install -y \
    openjdk-17-jdk \
    curl \
    wget \
    unzip \
    git \
    build-essential \
    lib32stdc++6 \
    lib32z1 \
    lib32ncurses5 \
    libc6-dev-i386 \
    libncurses5-dev \
    libncursesw5-dev \
    libpng-dev \
    libssl-dev \
    libxml2-dev \
    libxslt1-dev \
    zlib1g-dev

# Set up Java environment
print_status "Setting up Java environment..."
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java installation
java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
print_success "Java version: $java_version"

# Install Gradle
print_status "Installing Gradle..."
GRADLE_VERSION="8.6"
GRADLE_HOME="/opt/gradle"

if [ ! -d "$GRADLE_HOME" ]; then
    sudo mkdir -p $GRADLE_HOME
    cd /tmp
    wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip
    sudo unzip -d /opt/gradle gradle-${GRADLE_VERSION}-bin.zip
    sudo ln -s /opt/gradle/gradle-${GRADLE_VERSION} /opt/gradle/latest
    rm gradle-${GRADLE_VERSION}-bin.zip
fi

export GRADLE_HOME=/opt/gradle/latest
export PATH=$GRADLE_HOME/bin:$PATH

# Verify Gradle installation
gradle_version=$(gradle --version | grep "Gradle" | head -n 1 | awk '{print $2}')
print_success "Gradle version: $gradle_version"

# Install Android SDK
print_status "Setting up Android SDK..."
ANDROID_HOME="/opt/android-sdk"
ANDROID_SDK_VERSION="34.0.0"

if [ ! -d "$ANDROID_HOME" ]; then
    sudo mkdir -p $ANDROID_HOME
    sudo chown $USER:$USER $ANDROID_HOME
    
    # Download Android SDK Command Line Tools
    cd /tmp
    wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
    unzip commandlinetools-linux-11076708_latest.zip
    mkdir -p $ANDROID_HOME/cmdline-tools/latest
    mv cmdline-tools/* $ANDROID_HOME/cmdline-tools/latest/
    rm -rf cmdline-tools commandlinetools-linux-11076708_latest.zip
fi

export ANDROID_HOME=/opt/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$PATH

# Accept Android SDK licenses
print_status "Accepting Android SDK licenses..."
yes | sdkmanager --licenses

# Install required Android SDK components
print_status "Installing Android SDK components..."
sdkmanager --update
sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "extras;android;m2repository" \
    "extras;google;m2repository" \
    "extras;google;google_play_services" \
    "cmake;3.22.1" \
    "ndk;25.2.9519653"

# Verify Android SDK installation
print_success "Android SDK installed at: $ANDROID_HOME"
print_success "Android SDK components installed"

# Create Jenkins user (if not exists)
print_status "Setting up Jenkins user..."
if ! id "jenkins" &>/dev/null; then
    sudo useradd -m -s /bin/bash jenkins
    sudo usermod -aG sudo jenkins
    print_success "Jenkins user created"
else
    print_warning "Jenkins user already exists"
fi

# Set up SSH keys for Jenkins
print_status "Setting up SSH keys..."
JENKINS_HOME="/var/lib/jenkins"
if [ ! -d "$JENKINS_HOME/.ssh" ]; then
    sudo mkdir -p $JENKINS_HOME/.ssh
    sudo chown jenkins:jenkins $JENKINS_HOME/.ssh
    sudo chmod 700 $JENKINS_HOME/.ssh
fi

# Generate SSH key for Jenkins (if not exists)
if [ ! -f "$JENKINS_HOME/.ssh/id_rsa" ]; then
    sudo -u jenkins ssh-keygen -t rsa -b 4096 -f $JENKINS_HOME/.ssh/id_rsa -N ""
    print_success "SSH key generated for Jenkins"
else
    print_warning "SSH key already exists for Jenkins"
fi

# Create environment file for Jenkins
print_status "Creating environment configuration..."
sudo tee /etc/environment.d/jenkins.conf > /dev/null <<EOF
JAVA_HOME=$JAVA_HOME
GRADLE_HOME=$GRADLE_HOME
ANDROID_HOME=$ANDROID_HOME
PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:\$PATH
EOF

# Create Jenkins workspace directory
print_status "Setting up Jenkins workspace..."
JENKINS_WORKSPACE="/var/lib/jenkins/workspace"
sudo mkdir -p $JENKINS_WORKSPACE
sudo chown jenkins:jenkins $JENKINS_WORKSPACE

# Create build artifacts directory
print_status "Setting up build artifacts directory..."
ARTIFACTS_DIR="/var/lib/jenkins/artifacts"
sudo mkdir -p $ARTIFACTS_DIR
sudo chown jenkins:jenkins $ARTIFACTS_DIR

# Create Jenkins configuration file
print_status "Creating Jenkins configuration..."
sudo tee /var/lib/jenkins/jenkins-config.xml > /dev/null <<EOF
<?xml version='1.1' encoding='UTF-8'?>
<project>
  <actions/>
  <description>TruckDoc Mobile Build Configuration</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <hudson.plugins.disk__usage.DiskUsageProperty/>
    <jenkins.model.BuildDiscarderProperty>
      <strategy class="hudson.tasks.LogRotator">
        <daysToKeep>30</daysToKeep>
        <numToKeep>50</numToKeep>
        <artifactDaysToKeep>-1</artifactDaysToKeep>
        <artifactNumToKeep>-1</artifactNumToKeep>
      </strategy>
    </jenkins.model.BuildDiscarderProperty>
  </properties>
  <scm class="hudson.plugins.git.GitSCM" plugin="git@4.11.0">
    <configVersion>2</configVersion>
    <userRemoteConfigs>
      <hudson.plugins.git.UserRemoteConfig>
        <url>YOUR_GIT_REPOSITORY_URL</url>
        <credentialsId>jenkins-ssh-key</credentialsId>
      </hudson.plugins.git.UserRemoteConfig>
    </userRemoteConfigs>
    <branches>
      <hudson.plugins.git.BranchSpec>
        <name>*/main</name>
      </hudson.plugins.git.BranchSpec>
      <hudson.plugins.git.BranchSpec>
        <name>*/develop</name>
      </hudson.plugins.git.BranchSpec>
    </branches>
    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
    <submoduleCfg class="empty-list"/>
    <extensions/>
  </scm>
  <canRoam>true</canRoam>
  <disabled>false</disabled>
  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
  <triggers>
    <hudson.triggers.SCMTrigger>
      <spec>H/15 * * * *</spec>
    </hudson.triggers.SCMTrigger>
    <hudson.triggers.TimerTrigger>
      <spec>0 2 * * *</spec>
    </hudson.triggers.TimerTrigger>
  </triggers>
  <concurrentBuild>false</concurrentBuild>
  <builders/>
  <publishers/>
  <buildWrappers/>
</project>
EOF

# Create build script
print_status "Creating build script..."
sudo tee /var/lib/jenkins/build-truckdoc.sh > /dev/null <<'EOF'
#!/bin/bash

# TruckDoc Mobile Build Script
# This script is executed by Jenkins to build the TruckDoc mobile apps

set -e

echo "🚀 Starting TruckDoc Mobile build..."

# Set environment variables
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export GRADLE_HOME=/opt/gradle/latest
export ANDROID_HOME=/opt/android-sdk
export PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$PATH

# Build information
BUILD_NUMBER=${BUILD_NUMBER:-"manual"}
BUILD_URL=${BUILD_URL:-"local"}
GIT_COMMIT=$(git rev-parse HEAD 2>/dev/null || echo "unknown")
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")

echo "Build Number: $BUILD_NUMBER"
echo "Build URL: $BUILD_URL"
echo "Git Commit: $GIT_COMMIT"
echo "Git Branch: $GIT_BRANCH"

# Clean previous builds
echo "Cleaning previous builds..."
./gradlew clean

# Build main app
echo "Building main TruckDoc app..."
./gradlew :app:assembleRelease --stacktrace

# Build updater app
echo "Building app updater..."
./gradlew :app-updater:assembleRelease --stacktrace

# Generate build metadata
echo "Generating build metadata..."
cat > build-metadata.json <<EOF
{
  "buildNumber": "$BUILD_NUMBER",
  "buildUrl": "$BUILD_URL",
  "buildTimestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "gitCommit": "$GIT_COMMIT",
  "gitBranch": "$GIT_BRANCH",
  "mainAppVersion": "$(aapt dump badging truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk | grep versionName | cut -d"'" -f2)",
  "mainAppVersionCode": "$(aapt dump badging truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk | grep versionCode | cut -d"'" -f2)",
  "updaterVersion": "$(aapt dump badging app-updater/build/outputs/apk/release/app-updater-release.apk | grep versionName | cut -d"'" -f2)",
  "updaterVersionCode": "$(aapt dump badging app-updater/build/outputs/apk/release/app-updater-release.apk | grep versionCode | cut -d"'" -f2)"
}
EOF

echo "✅ Build completed successfully!"
echo "📱 APKs generated:"
echo "  - Main App: truckdoc-client-m2/application/build/outputs/apk/release/application-release.apk"
echo "  - Updater: app-updater/build/outputs/apk/release/app-updater-release.apk"
echo "📄 Metadata: build-metadata.json"
EOF

sudo chmod +x /var/lib/jenkins/build-truckdoc.sh
sudo chown jenkins:jenkins /var/lib/jenkins/build-truckdoc.sh

# Create health check script
print_status "Creating health check script..."
sudo tee /var/lib/jenkins/health-check.sh > /dev/null <<'EOF'
#!/bin/bash

# Health check script for Jenkins environment

echo "🔍 Checking Jenkins environment..."

# Check Java
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java: $java_version"
else
    echo "❌ Java: Not found"
    exit 1
fi

# Check Gradle
if command -v gradle &> /dev/null; then
    gradle_version=$(gradle --version | grep "Gradle" | head -n 1 | awk '{print $2}')
    echo "✅ Gradle: $gradle_version"
else
    echo "❌ Gradle: Not found"
    exit 1
fi

# Check Android SDK
if [ -d "/opt/android-sdk" ]; then
    echo "✅ Android SDK: Found at /opt/android-sdk"
else
    echo "❌ Android SDK: Not found"
    exit 1
fi

# Check Android tools
if command -v aapt &> /dev/null; then
    echo "✅ Android tools: Available"
else
    echo "❌ Android tools: Not found"
    exit 1
fi

# Check Jenkins user
if id "jenkins" &> /dev/null; then
    echo "✅ Jenkins user: Exists"
else
    echo "❌ Jenkins user: Not found"
    exit 1
fi

# Check workspace permissions
if [ -w "/var/lib/jenkins/workspace" ]; then
    echo "✅ Workspace: Writable"
else
    echo "❌ Workspace: Not writable"
    exit 1
fi

echo "🎉 All health checks passed!"
EOF

sudo chmod +x /var/lib/jenkins/health-check.sh
sudo chown jenkins:jenkins /var/lib/jenkins/health-check.sh

# Set up log rotation
print_status "Setting up log rotation..."
sudo tee /etc/logrotate.d/jenkins-truckdoc > /dev/null <<EOF
/var/lib/jenkins/workspace/*/build.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 jenkins jenkins
}
EOF

# Create systemd service for Jenkins (if not using Docker)
print_status "Creating systemd service configuration..."
sudo tee /etc/systemd/system/jenkins-truckdoc.service > /dev/null <<EOF
[Unit]
Description=Jenkins TruckDoc Mobile Build Server
After=network.target

[Service]
Type=simple
User=jenkins
Group=jenkins
Environment="JAVA_HOME=$JAVA_HOME"
Environment="GRADLE_HOME=$GRADLE_HOME"
Environment="ANDROID_HOME=$ANDROID_HOME"
Environment="PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:\$PATH"
ExecStart=/usr/bin/java -jar /usr/share/jenkins/jenkins.war
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

# Final setup
print_status "Finalizing setup..."
sudo chown -R jenkins:jenkins /var/lib/jenkins

# Print summary
echo ""
print_success "🎉 Jenkins environment setup completed!"
echo ""
echo "📋 Setup Summary:"
echo "  - Java 17: $java_version"
echo "  - Gradle: $gradle_version"
echo "  - Android SDK: $ANDROID_HOME"
echo "  - Jenkins user: jenkins"
echo "  - Workspace: /var/lib/jenkins/workspace"
echo "  - Artifacts: /var/lib/jenkins/artifacts"
echo ""
echo "🔧 Next Steps:"
echo "  1. Install Jenkins server"
echo "  2. Configure Jenkins with the provided configuration"
echo "  3. Set up the pipeline job using the Jenkinsfile"
echo "  4. Configure Git repository access"
echo "  5. Run health check: sudo -u jenkins /var/lib/jenkins/health-check.sh"
echo ""
echo "📚 Documentation:"
echo "  - Jenkinsfile: jenkins/Jenkinsfile"
echo "  - Build script: /var/lib/jenkins/build-truckdoc.sh"
echo "  - Health check: /var/lib/jenkins/health-check.sh"
echo ""
print_success "Setup completed successfully!" 
 