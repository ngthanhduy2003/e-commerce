ôi# Environment Setup Guide

## Java Setup

### Check Java Installation
```powershell
java -version
```

If not installed or wrong version (need Java 24+):

### Download Java 24
1. Go to https://jdk.java.net/24/
2. Download Windows x64 ZIP
3. Extract to `C:\Program Files\Java\jdk-24`

### Set JAVA_HOME (Windows)
```powershell
# Option 1: PowerShell (Current Session)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Option 2: System Environment Variables (Permanent)
# 1. Press Win + X, select "System"
# 2. Click "Advanced system settings"
# 3. Click "Environment Variables"
# 4. Under "System variables", click "New"
#    - Variable name: JAVA_HOME
#    - Variable value: C:\Program Files\Java\jdk-24
# 5. Edit "Path" variable, add: %JAVA_HOME%\bin
# 6. Click OK, restart terminal
```

### Verify
```powershell
java -version
# Should show: java version "24"...

echo $env:JAVA_HOME
# Should show: C:\Program Files\Java\jdk-24
```

## MySQL Setup

### Download & Install
1. Download MySQL 8+ from https://dev.mysql.com/downloads/installer/
2. Run installer, choose "Custom" or "Developer Default"
3. Set root password (remember it!)

### Verify
```powershell
mysql --version
# Should show: mysql  Ver 8.x.x

# Test connection
mysql -u root -p
# Enter password
mysql> SHOW DATABASES;
mysql> exit
```

### Create Database
```sql
CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## Redis Setup

### Option 1: Windows Native
Download from: https://github.com/tporadowski/redis/releases
1. Download Redis-x64-5.x.zip
2. Extract to `C:\Redis`
3. Run `redis-server.exe`

### Option 2: Docker (Recommended)
```powershell
# Install Docker Desktop first
docker --version

# Run Redis
docker run -d --name redis -p 6379:6379 redis:latest

# Check status
docker ps

# Stop
docker stop redis

# Start again
docker start redis
```

### Verify
```powershell
# If native install
cd C:\Redis
.\redis-cli.exe ping
# Should return: PONG

# If Docker
docker exec -it redis redis-cli ping
# Should return: PONG
```

## Gradle Setup

Gradle Wrapper is included in project, but if needed:

### Download Gradle
https://gradle.org/releases/
- Download latest version
- Extract to `C:\Gradle`

### Set GRADLE_HOME (Optional)
```powershell
$env:GRADLE_HOME = "C:\Gradle\gradle-8.x"
$env:PATH = "$env:GRADLE_HOME\bin;$env:PATH"
```

### Verify
```powershell
.\gradlew.bat --version
# Should show Gradle version
```

## IDE Setup (IntelliJ IDEA)

### Import Project
1. Open IntelliJ IDEA
2. File → Open → Select `e-commerce` folder
3. IntelliJ will auto-detect Gradle project
4. Wait for dependency download

### Configure JDK
1. File → Project Structure
2. Project → SDK → Add JDK
3. Select `C:\Program Files\Java\jdk-24`
4. Apply

### Enable Lombok
1. File → Settings → Plugins
2. Search "Lombok"
3. Install plugin
4. Restart IDE

### Run Application
1. Open `EcommerceApplication.java`
2. Right-click → Run 'EcommerceApplication'
3. Or use Gradle: `.\gradlew.bat bootRun`

## Postman Setup

### Install Postman
Download from: https://www.postman.com/downloads/

### Import Collection
1. Open Postman
2. File → Import
3. Select `postman-collection.json`
4. Collection "E-commerce API" will appear

### Set Variables
Collection variables are auto-filled during requests.
Manual setup:
- baseUrl: http://localhost:8080
- cartToken: (filled after creating cart)
- orderId: (filled after checkout)
- trackingToken: (filled after checkout)

## Troubleshooting

### "JAVA_HOME is not set"
```powershell
# Quick fix (temporary)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify
echo $env:JAVA_HOME
java -version
```

### "Cannot connect to MySQL"
```powershell
# Check MySQL service is running
# Windows Services → MySQL → Start

# Or restart MySQL
net stop MySQL80
net start MySQL80
```

### "Cannot connect to Redis"
```powershell
# If native install
cd C:\Redis
.\redis-server.exe

# If Docker
docker start redis
docker ps  # Check it's running
```

### "Port 8080 already in use"
```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID with actual PID)
taskkill /F /PID <PID>

# Or change port in application.yaml
```

### "Lombok not working"
1. Ensure Lombok plugin installed in IntelliJ
2. Settings → Build, Execution, Deployment → Compiler → Annotation Processors
3. Check "Enable annotation processing"

### "Gradle build fails"
```powershell
# Clean and rebuild
.\gradlew.bat clean build --refresh-dependencies

# If permission error on Windows
.\gradlew.bat --stop
.\gradlew.bat clean build
```

## Quick Verification Script

Save as `verify-env.ps1`:
```powershell
Write-Host "=== Environment Verification ===" -ForegroundColor Cyan

# Java
Write-Host "`nJava:" -ForegroundColor Yellow
java -version 2>&1
Write-Host "JAVA_HOME: $env:JAVA_HOME"

# MySQL
Write-Host "`nMySQL:" -ForegroundColor Yellow
mysql --version 2>&1

# Redis
Write-Host "`nRedis:" -ForegroundColor Yellow
if (Get-Command redis-cli -ErrorAction SilentlyContinue) {
    redis-cli ping
} else {
    Write-Host "Redis CLI not found in PATH"
    Write-Host "Checking Docker..."
    docker exec redis redis-cli ping 2>&1
}

# Gradle
Write-Host "`nGradle:" -ForegroundColor Yellow
.\gradlew.bat --version 2>&1 | Select-Object -First 3

Write-Host "`n=== Verification Complete ===" -ForegroundColor Cyan
```

Run:
```powershell
.\verify-env.ps1
```

## Next Steps

After environment is ready:
1. ✅ Java 24 installed and JAVA_HOME set
2. ✅ MySQL running and database created
3. ✅ Redis running
4. ✅ Project opens in IDE
5. ⏭️ Follow `QUICKSTART.md` to run application

---

**Need help?** Check logs or create an issue.

