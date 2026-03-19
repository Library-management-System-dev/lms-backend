#!/bin/bash

# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

# Extract connection details from DB_URL
# Format: jdbc:postgresql://host:port/database?params
HOST=$(echo $DB_URL | sed 's/jdbc:postgresql:\/\///' | cut -d':' -f1)
PORT=$(echo $DB_URL | sed 's/jdbc:postgresql:\/\///' | cut -d':' -f2 | cut -d'/' -f1)
DATABASE=$(echo $DB_URL | sed 's/jdbc:postgresql:\/\///' | cut -d'/' -f2 | cut -d'?' -f1)

echo "Fixing book table columns in PostgreSQL..."
echo "Host: $HOST"
echo "Database: $DATABASE"
echo ""

# Execute SQL using psql
PGPASSWORD=$DB_PASSWORD psql -h $HOST -p $PORT -U $DB_USERNAME -d $DATABASE << EOF
ALTER TABLE book 
  ALTER COLUMN isbn TYPE VARCHAR(20),
  ALTER COLUMN title TYPE VARCHAR(255),
  ALTER COLUMN author TYPE VARCHAR(255);

SELECT 'Column types updated successfully!' as status;
EOF

echo ""
echo "Done! Restart your application to test."
