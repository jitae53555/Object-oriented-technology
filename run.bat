@echo off
chcp 65001 > nul
echo [SYSTEM] Compiling Movie Reservation System...
"C:\Users\user\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64\bin\javac.exe" -d bin -encoding UTF-8 src/com/movie/system/model/*.java src/com/movie/system/service/*.java src/com/movie/system/main/*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)
echo [SYSTEM] Running Movie Reservation System...
echo.
"C:\Users\user\.vscode\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64\bin\java.exe" -Dfile.encoding=UTF-8 -cp bin com.movie.system.main.MovieSystemMain
pause
