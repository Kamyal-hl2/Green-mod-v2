@echo off
setlocal

:: Set the default Steam path
set "STEAM_PATH=%ProgramFiles(x86)%\Steam"
set "GARRYSMOD_PATH=%STEAM_PATH%\steamapps\common\GarrysMod\garrysmod"
set "GARRYSMODSE_PATH=%STEAM_PATH%\steamapps\common\GarrysMod\sourceengine"
set "GARRYSMODP_PATH=%STEAM_PATH%\steamapps\common\GarrysMod\platform"

:: Set mod directory to gmodsdk_fix folder within the script directory
set "MOD_PATH=%~dp0gmodsdk_fix"

:: Create the target folder if it doesn't exist
if not exist "%MOD_PATH%" (
    mkdir "%MOD_PATH%"
)

echo.
echo Searching for .vpk files in: %GARRYSMOD_PATH%
echo Copying to: %MOD_PATH%
echo.

if not exist "%GARRYSMOD_PATH%" (
    echo [ERROR] Could not find Garry's Mod folder.
    echo Make sure Garry's Mod is installed and Steam is in the default location.
    pause
    exit /b 1
)

:: Copy all .vpk files from GMod to the gmodsdk_fix folder
xcopy "%GARRYSMOD_PATH%\*.vpk" "%MOD_PATH%" /Y /I
xcopy "%GARRYSMODSE_PATH%\*.vpk" "%MOD_PATH%" /Y /I
xcopy "%GARRYSMODP_PATH%\*.vpk" "%MOD_PATH%" /Y /I

echo.
echo Done! All .vpk files copied to gmodsdk_fix.
pause
endlocal
