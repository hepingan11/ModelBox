@echo off
echo 正在清理Cursor缓存...

:: 关闭所有正在运行的Cursor进程
taskkill /f /im "Cursor.exe" /t 2>nul

:: 清理缓存目录
set CURSOR_CACHE=%LOCALAPPDATA%\Cursor\Cache
set CURSOR_CODE_CACHE=%LOCALAPPDATA%\Cursor\Code Cache
set CURSOR_GPUCACHE=%LOCALAPPDATA%\Cursor\GPUCache
set CURSOR_STORAGE=%LOCALAPPDATA%\Cursor\Storage
set CURSOR_CURSOR_STORE=%APPDATA%\cursor-assistant-electron\cursor_store

echo 清理 %CURSOR_CACHE%
if exist "%CURSOR_CACHE%" rmdir /s /q "%CURSOR_CACHE%"

echo 清理 %CURSOR_CODE_CACHE%
if exist "%CURSOR_CODE_CACHE%" rmdir /s /q "%CURSOR_CODE_CACHE%"

echo 清理 %CURSOR_GPUCACHE%
if exist "%CURSOR_GPUCACHE%" rmdir /s /q "%CURSOR_GPUCACHE%"

echo 清理 %CURSOR_STORAGE%
if exist "%CURSOR_STORAGE%" rmdir /s /q "%CURSOR_STORAGE%"

echo 清理 %CURSOR_CURSOR_STORE%
if exist "%CURSOR_CURSOR_STORE%" rmdir /s /q "%CURSOR_CURSOR_STORE%"

:: 清理可能存在的其他缓存文件
echo 清理其他缓存文件...
del /f /s /q "%LOCALAPPDATA%\Cursor\blob_storage\*.*" 2>nul
del /f /s /q "%LOCALAPPDATA%\Cursor\databases\*.*" 2>nul
del /f /s /q "%LOCALAPPDATA%\Cursor\IndexedDB\*.*" 2>nul
del /f /s /q "%LOCALAPPDATA%\Cursor\Local Storage\*.*" 2>nul
del /f /s /q "%LOCALAPPDATA%\Cursor\Session Storage\*.*" 2>nul

echo.
echo Cursor缓存清理完成!
echo 请重新启动Cursor以获得最佳性能。
echo.
pause
