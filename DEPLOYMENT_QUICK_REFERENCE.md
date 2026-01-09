# Deployment Quick Reference

## 🚀 Quick Start Commands

### Jenkins Setup (Linux)
```bash
# Install Jenkins
sudo apt update && sudo apt install jenkins
sudo systemctl start jenkins && sudo systemctl enable jenkins

# Get initial password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# Install required plugins
# - Android Lint Plugin
# - Gradle Plugin
# - Git Plugin
# - Pipeline Plugin
# - SSH Agent Plugin
```

### Jenkins Setup (Windows)
```powershell
# Install Java
winget install EclipseAdoptium.Temurin.17.JDK

# Install Jenkins
choco install jenkins
# or download from https://www.jenkins.io/download/lts/windows/

# Start Jenkins
Start-Service jenkins
# or
java -jar jenkins.war --httpPort=8080

# Get initial password
Get-Content "$env:JENKINS_HOME\secrets\initialAdminPassword"

# Install required tools
winget install Git.Git
winget install Google.AndroidStudio
winget install Gradle.Gradle
```

### Nginx Setup
```bash
# Install nginx
sudo apt install nginx

# Create APK directory
sudo mkdir -p /var/www/apks
sudo chown -R jenkins:jenkins /var/www/apks

# Enable site
sudo ln -s /etc/nginx/sites-available/truckdoc-apks /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl restart nginx
```

## 📋 Essential Configurations

### Jenkins Credentials
- **SSH Key**: `nginx-ssh-key` for nginx server access
- **Git Credentials**: For repository access
- **Email Credentials**: For notifications

### Environment Variables
```bash
ANDROID_HOME=/opt/android-sdk
GRADLE_HOME=/opt/gradle
BUILD_VARIANT=defaultClientRelease
NGINX_SERVER=your-nginx-server.com
NGINX_USER=jenkins
NGINX_PATH=/var/www/apks
```

### Environment Variables (Windows)
```powershell
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$env:GRADLE_HOME = "C:\Gradle\gradle-8.0"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:JENKINS_HOME = "C:\Jenkins\jenkins_home"
$env:BUILD_VARIANT = "defaultClientRelease"
$env:LOCAL_APK_PATH = "C:\Jenkins\apks"
```

### Build Commands (Linux)
```bash
# Manual build trigger
./gradlew clean assembleDefaultClientRelease

# Run tests
./gradlew testDefaultClientDebugUnitTest
./gradlew connectedDefaultClientDebugAndroidTest

# Generate signed APK
jarsigner -keystore keystore.jks -storepass truckdoc123 application-release.apk truckdoc
zipalign -v 4 application-release.apk application-release-aligned.apk
```

### Build Commands (Windows)
```powershell
# Manual build trigger
.\gradlew.bat clean assembleDefaultClientRelease

# Run tests
.\gradlew.bat testDefaultClientDebugUnitTest
.\gradlew.bat connectedDefaultClientDebugAndroidTest

# Generate signed APK
jarsigner -keystore keystore.jks -storepass truckdoc123 application-release.apk truckdoc
& "$env:ANDROID_HOME\build-tools\33.0.0\zipalign.exe" -v 4 application-release.apk application-release-aligned.apk
```

## 🔧 API Endpoints

### Update Check Endpoints
```
GET https://your-nginx-server.com/api/updates/latest
GET https://your-nginx-server.com/api/updates/notification
GET https://your-nginx-server.com/api/updates/version
```

### APK Download
```
GET https://your-nginx-server.com/apks/truckdoc-mobile-1.0.3.apk
```

## 📁 File Structure

```
/var/www/apks/
├── truckdoc-mobile-1.0.3.apk          # Latest APK
├── build-info-1.0.3.json              # Build information
├── truckdoc-mobile-1.0.3.sha256       # Checksum
├── latest-build.json                  # Latest build info
├── update-notification.json           # Update notification
└── latest-version.txt                 # Latest version
```

## 🔍 Monitoring Commands

### Jenkins (Linux)
```bash
# Check status
sudo systemctl status jenkins

# View logs
sudo tail -f /var/log/jenkins/jenkins.log

# Check disk usage
df -h /var/lib/jenkins
```

### Jenkins (Windows)
```powershell
# Check status
Get-Service jenkins

# View logs
Get-EventLog -LogName Application -Source Jenkins

# Check disk usage
Get-WmiObject -Class Win32_LogicalDisk | Select-Object DeviceID, Size, FreeSpace

# Check Jenkins process
Get-Process | Where-Object {$_.ProcessName -like "*java*"}
```

### Nginx
```bash
# Check status
sudo systemctl status nginx

# View logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# Test configuration
sudo nginx -t
```

### APK Files
```bash
# List APK files
ls -la /var/www/apks/*.apk

# Check file sizes
du -h /var/www/apks/*.apk

# Verify checksums
sha256sum -c /var/www/apks/*.sha256
```

## 🚨 Troubleshooting

### Build Issues
```bash
# Clean workspace
sudo rm -rf /var/lib/jenkins/workspace/truckdoc-mobile-build/

# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Check Android SDK
$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --list
```

### Network Issues
```bash
# Test connectivity
curl -I https://your-nginx-server.com/health

# Check SSL certificate
openssl s_client -connect your-nginx-server.com:443

# Test APK download
curl -I https://your-nginx-server.com/apks/truckdoc-mobile-1.0.3.apk
```

### Permission Issues
```bash
# Fix nginx permissions
sudo chown -R jenkins:jenkins /var/www/apks/
sudo chmod 755 /var/www/apks/

# Fix Jenkins permissions
sudo chown -R jenkins:jenkins /var/lib/jenkins/
```

### Windows Issues
```powershell
# Issue: Jenkins won't start
java -version

# Issue: Port 8080 already in use
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Issue: Android SDK not found
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

# Issue: Permission denied
Start-Process powershell -Verb RunAs

# Issue: Gradle not found
.\gradlew.bat --version
```

## 📧 Notifications

### Email Configuration
```bash
# Configure email in Jenkins
# Manage Jenkins > Configure System > Extended E-mail Notification
SMTP Server: smtp.gmail.com
SMTP Port: 587
Use SSL: true
```

### Webhook Configuration
```bash
# GitHub webhook URL
http://your-jenkins-server:8080/github-webhook/

# GitLab webhook URL
http://your-jenkins-server:8080/project/truckdoc-mobile-build
```

## 🔒 Security Checklist

- [ ] SSL certificate installed and valid
- [ ] Firewall rules configured
- [ ] SSH keys properly set up
- [ ] Basic authentication enabled (optional)
- [ ] Rate limiting configured
- [ ] Security headers added
- [ ] Regular backups scheduled
- [ ] Monitoring alerts configured

## 📊 Performance Metrics

### Build Metrics
- Average build time: ~10-15 minutes
- Test execution time: ~5-8 minutes
- APK file size: ~50-100MB
- Storage usage: ~1GB per 10 builds

### Network Metrics
- APK download speed: ~10MB/s
- API response time: <100ms
- Concurrent downloads: 50+
- Bandwidth usage: ~1GB/day

## 🛠️ Maintenance Tasks

### Daily
- [ ] Check build status
- [ ] Monitor disk space
- [ ] Review error logs

### Weekly
- [ ] Clean old builds
- [ ] Update dependencies
- [ ] Review security logs

### Monthly
- [ ] Update SSL certificates
- [ ] Review performance metrics
- [ ] Backup configuration
- [ ] Update system packages

## 📞 Emergency Contacts

- **Jenkins Admin**: admin@truckdoc.com
- **DevOps Team**: devops@truckdoc.com
- **System Administrator**: sysadmin@truckdoc.com

## 🔗 Useful Links

- [Full Deployment Guide](JENKINS_DEPLOYMENT_GUIDE.md)
- [Build Commands Reference](BUILD_COMMANDS.md)
- [Migration Notes](MIGRATION_NOTES.md)
- [Jenkins Documentation](https://jenkins.io/doc/)
- [Nginx Documentation](https://nginx.org/en/docs/) 