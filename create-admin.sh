#!/bin/bash

# Create admin user manually
echo "Creating admin user..."

RESPONSE=$(curl -s -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "Admin@123",
    "fullName": "Admin User",
    "phone": "0000000000"
  }')

echo "$RESPONSE" | jq .

# Note: This creates a regular user. You'll need to manually update the role in database to ROLE_ADMIN
echo ""
echo "Note: User created with ROLE_USER. To make admin, update database:"
echo "UPDATE \"user\" SET role = 'ROLE_ADMIN' WHERE email = 'admin@example.com';"
