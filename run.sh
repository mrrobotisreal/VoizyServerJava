#!/bin/bash

# Run Voizy Server

# Set environment variables if not already set
export USE_HTTPS=false
export SERVER_PORT=8282

if [ -z "$DBU" ]; then
    echo "Setting default MySQL username (root)..."
    export DBU=root
fi

if [ -z "$DBP" ]; then
    echo "Setting default MySQL password (empty)..."
    export DBP=""
fi

if [ -z "$KEYSTORE_PASSWORD" ]; then
    echo "Setting default keystore password (changeit)..."
    export KEYSTORE_PASSWORD=changeit
fi

# Build the application if not already built
if [ ! -f "build/libs/VoizyServer-1.0-SNAPSHOT.jar" ]; then
    echo "Building application..."
    ./gradlew clean shadowJar
fi

# Run the application
echo "Starting Voizy Server..."
java -jar build/libs/VoizyServer-1.0-SNAPSHOT.jar
