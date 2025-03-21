@echo off
REM Run Voizy Server locally with HTTP

REM Set environment variables for local development
set USE_HTTPS=false
set SERVER_PORT=8282

REM Set database credentials
if "%DBU%"=="" (
	echo Setting default MySQL username to root...
	set DBU=root
)

if "%DBP%"=="" (
	echo Setting default MySQL password...
	set DBP=
)

REM Build the application if not already built
if not exist "build\libs\VoizyServer-1.0-SNAPSHOT-all.jar" (
	echo Building application...
	call gradlew.bat clean shadowJar
)

REM Run the application
echo Starting Voizy Server in local development mode (HTTP)...
echo Server will be available at http://localhost:8282
java -jar build\libs\VoizyServer-1.0-SNAPSHOT-all.jar
