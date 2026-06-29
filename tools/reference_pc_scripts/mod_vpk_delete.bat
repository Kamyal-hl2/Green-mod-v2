@echo off
setlocal

:: Path to the gmodsdk_fix folder inside the script directory
set "MOD_PATH=%~dp0gmodsdk_fix"

echo.
echo Looking for specific Garry's Mod .vpk files to remove from:
echo %MOD_PATH%
echo.

:: Remove specific GMod-related .vpk files from the gmodsdk_fix directory
del /Q "%MOD_PATH%\hl2*.vpk" 2>nul
del /Q "%MOD_PATH%\fallback*.vpk" 2>nul
del /Q "%MOD_PATH%\garrysmod*.vpk" 2>nul
del /Q "%MOD_PATH%\platform*.vpk" 2>nul
del /Q "%MOD_PATH%\content*.vpk" 2>nul

echo.
echo All hl2*, fallback*, garrysmod*, platform*, and content* .vpk files removed from gmodsdk_fix.
echo Your mod is now clean for public sharing.
pause
endlocal
