-- Fix PostgreSQL column types for book table
-- Run this SQL in your Neon database console

ALTER TABLE book 
  ALTER COLUMN isbn TYPE VARCHAR(20),
  ALTER COLUMN title TYPE VARCHAR(255),
  ALTER COLUMN author TYPE VARCHAR(255);

-- Verify the changes
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'book' 
  AND column_name IN ('isbn', 'title', 'author');
