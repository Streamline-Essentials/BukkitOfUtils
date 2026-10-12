@echo off
setlocal
rem Uploads the built BukkitOfUtils jar to Modrinth. All logic lives in upload-modrinth.ps1.
rem
rem Usage: upload-modrinth.bat [-Token <token>] [-Project <slug>] [-Version <version>]
rem          [-JarPath <path>] [-Changelog <path>] [-VersionType release^|beta^|alpha]
rem          [-GameVersions <v1,v2>] [-Loaders <l1,l2>] [-Featured] [-Build] [-DryRun] [-Force]
rem
rem The token defaults to the MODRINTH_TOKEN environment variable.
rem Examples:
rem   upload-modrinth.bat -DryRun
rem   upload-modrinth.bat -Build
rem   upload-modrinth.bat -VersionType beta -Force

set "SCRIPT=%~dp0upload-modrinth.ps1"
if not exist "%SCRIPT%" (
    echo upload-modrinth.ps1 not found next to this file: "%SCRIPT%"
    exit /b 1
)

where pwsh >nul 2>nul
if %ERRORLEVEL% equ 0 (
    pwsh -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
)
exit /b %ERRORLEVEL%
