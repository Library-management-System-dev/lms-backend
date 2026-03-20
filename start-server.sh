#!/bin/bash

echo "🚀 Library Management System - PostgreSQL Neon Setup"
echo "=================================================="
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "❌ .env file not found!"
    echo "📝 Please create .env file with your Neon PostgreSQL credentials"
    echo ""
    echo "Copy .env.example to .env and fill in your details:"
    echo "  cp .env.example .env"
    echo ""
    echo "Get your Neon credentials from: https://console.neon.tech"
    exit 1
fi

# Check if DB_URL is configured
if grep -q "YOUR_NEON_HOST" .env; then
    echo "⚠️  .env file needs configuration!"
    echo "📝 Please update .env with your actual Neon PostgreSQL credentials"
    echo ""
    echo "Required fields:"
    echo "  - DB_URL"
    echo "  - DB_USERNAME"
    echo "  - DB_PASSWORD"
    echo ""
    echo "See NEON_SETUP.md for detailed instructions"
    exit 1
fi

echo "✅ .env file found"
echo ""

# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

echo "🧹 Cleaning old build..."
mvn clean -q

echo "📦 Building project with PostgreSQL..."
mvn install -DskipTests -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting server on http://localhost:8080"
    echo "📊 Database: PostgreSQL (Neon)"
    echo ""
    echo "Press Ctrl+C to stop the server"
    echo "=================================================="
    echo ""
    
    mvn spring-boot:run
else
    echo "❌ Build failed! Check the errors above."
    exit 1
fi
