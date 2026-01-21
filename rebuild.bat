@echo off
echo ============================================
echo  E-Commerce Project - Force Rebuild
echo ============================================
echo.

echo Step 1: Cleaning build directory...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Clean failed
    pause
    exit /b 1
)

echo.
echo Step 2: Refreshing dependencies...
call gradlew.bat build --refresh-dependencies -x test
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo ============================================
echo  Build successful!
echo ============================================
echo.
echo Next steps:
echo 1. Start MySQL: net start MySQL80
echo 2. Start Redis: redis-server
echo 3. Run: gradlew.bat bootRun
echo.

pause

