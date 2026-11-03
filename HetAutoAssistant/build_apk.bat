@echo off
setlocal
echo ========================================================
echo   HET AUTO ASSISTANT - 1-CLICK APK BUILDER
echo ========================================================
echo.

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [!] Java 17 was not detected on this computer.
    echo.
    echo To build locally on Windows, install Java 17 by opening PowerShell as Administrator and running:
    echo    winget install Microsoft.OpenJDK.17
    echo.
    echo Or download Android Studio from:
    echo    https://developer.android.com/studio
    echo.
    pause
    exit /b 1
)

echo [*] Java detected. Compiling APK with Gradle...
echo.
call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================================
    echo [SUCCESS] APK generated successfully!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
    echo ========================================================
) else (
    echo.
    echo [ERROR] Build failed. Please check the logs above.
)

pause
