#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded from .env file"
else
    echo "❌ .env file not found!"
    exit 1
fi

# Run the Spring Boot application
echo "🚀 Starting Library Management System..."
mvn spring-boot:run
