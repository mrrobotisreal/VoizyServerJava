@echo off
REM Run Voizy Server

REM Set environment variables if not already set
if "%DBU%"=="" (
    echo Setting default MySQL username (root)...
    set DBU=root
)

if "%DBP%"=="" (
    echo Setting default MySQL password (empty)...
    set DBP=
)

if "%KEYSTORE_PASSWORD%"=="" (
    echo Setting default keystore password (changeit)...
    set KEYSTORE_PASSWORD=changeit
)

REM Build the application if not already built
if not exist "build\libs\VoizyServer-1.0-SNAPSHOT.jar" (
    echo Building application...
    call gradlew.bat clean shadowJar
)

REM Run the application
echo Starting Voizy Server...
java -jar build\libs\VoizyServer-1.0-SNAPSHOT.jar
