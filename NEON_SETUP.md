# Neon PostgreSQL Setup Guide

## Step 1: Create Neon Account & Database

1. Go to [https://neon.tech](https://neon.tech)
2. Sign up for a free account
3. Create a new project (e.g., "Library Management System")
4. Neon will automatically create a database named `neondb`

## Step 2: Get Connection Details

1. In your Neon dashboard, click on your project
2. Go to **Connection Details** section
3. You'll see something like:
   ```
   Host: ep-example-123456.us-east-2.aws.neon.tech
   Database: neondb
   Username: your_username
   Password: your_password
   ```

## Step 3: Update .env File

Open `.env` file in the project root and update:

```properties
DB_URL=jdbc:postgresql://YOUR_NEON_HOST/neondb?sslmode=require
DB_USERNAME=YOUR_NEON_USERNAME
DB_PASSWORD=YOUR_NEON_PASSWORD
```

**Example:**
```properties
DB_URL=jdbc:postgresql://ep-example-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
DB_USERNAME=krish_user
DB_PASSWORD=AbCdEf123456
```

**Important:** 
- Keep `?sslmode=require` at the end of the URL
- Use `neondb` as database name (Neon's default) or create a new database named `librarydb`

## Step 4: Update Gmail App Password

If you haven't already:
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Enable 2-Step Verification
3. Go to **App passwords**
4. Generate a new password for "Mail"
5. Update in `.env`:
   ```properties
   MAIL_PASSWORD=your_16_char_app_password
   ```

## Step 5: Clean Build & Start

```bash
# Kill any running server
lsof -ti:8080 | xargs kill -9

# Clean old MySQL data
mvn clean

# Build with PostgreSQL
mvn install -DskipTests

# Start server
mvn spring-boot:run
```

## Step 6: Verify Connection

Check the logs for:
```
Hibernate: create table if not exists users ...
Started LibraryManagementSystemApplication in X seconds
```

## Troubleshooting

### Error: "Connection refused"
- Check your Neon host URL is correct
- Ensure `?sslmode=require` is in the URL

### Error: "Authentication failed"
- Verify username and password from Neon dashboard
- Password is case-sensitive

### Error: "Database does not exist"
- Create database in Neon console or use default `neondb`

## Database Migration from MySQL

Since you're switching from MySQL to PostgreSQL, all tables will be recreated automatically by Hibernate with `ddl-auto=update`.

**Note:** Old MySQL data will NOT be migrated. The admin user will be auto-created on first startup:
- Email: `loharkrish95@gmail.com`
- Password: `Krish@123`

## Neon Free Tier Limits

- 0.5 GB storage
- 1 project
- Unlimited databases per project
- Auto-suspend after 5 minutes of inactivity
- Perfect for development!
