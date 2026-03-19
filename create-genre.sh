#!/bin/bash

BASE_URL="http://localhost:8080"

# Login as admin
echo "Logging in as admin..."
ADMIN_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "loharkrish95@gmail.com",
    "password": "@Rezero9095"
  }')

ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | jq -r '.jwt')

if [ "$ADMIN_TOKEN" == "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "Failed to login as admin"
    exit 1
fi

echo "Admin token obtained"
echo ""

# Create a genre
echo "Creating Fiction genre..."
CREATE_GENRE=$(curl -s -X POST "$BASE_URL/api/genres/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Fiction",
    "code": "FICTION",
    "description": "Fiction books"
  }')

echo "$CREATE_GENRE" | jq .
echo ""
echo "Genre created! You can now add books with genreId: 1"
