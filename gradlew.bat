@echo off
setlocal

set "APP_HOME=%~dp0"
set "WRAPPER_DIR=%APP_HOME%gradle\wrapper"
set "WRAPPER_JAR=%WRAPPER_DIR%\gradle-wrapper.jar"
set "WRAPPER_URL=https://raw.githubusercontent.com/Xevaryth/Mysticism/2d425ac722ab9e2963d53d9048b4df7792501c53/gradle/wrapper/gradle-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
    echo Downloading the Gradle wrapper bootstrap...
    if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
    if errorlevel 1 (
        echo Failed to download the Gradle wrapper from Mysticism's GitHub repository. 1>&2
        exit /b 1
    )
)

if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" -Xmx64m -Xms64m "-Dorg.gradle.appname=gradlew" -classpath "" -jar "%WRAPPER_JAR%" %*
exit /b %ERRORLEVEL%
