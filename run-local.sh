#!/bin/bash

# Run Voizy Server locally with HTTP

# Set environment variables for local development
export USE_HTTPS=false
export SERVER_PORT=8282

# Set database credentials
if [ -z "$DBU" ]; then
	echo "Setting default MySQL username to root..."
	export DBU="root"
fi

if [ -z "$DBP" ]; then
	echo "Setting default MySQL password..."
	export DBP=""
fi

# Build the application if not already built
if [ ! -f "build/libs/VoizyServer-1.0-SNAPSHOT-all.jar" ]; then
	echo "Building application..."
	./gradlew clean shadowJar
fi

# Run the application
echo "Starting Voizy Server in local development mode (HTTP)..."
echo "Server will be available at http://localhost:8282"
java -jar build/libs/VoizyServer-1.0-SNAPSHOT-all.jar
