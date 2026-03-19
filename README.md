# 📚 Library Management System

A comprehensive Library Management System built with Spring Boot, featuring book management, user subscriptions, reservations, fines, and payment integration.

## ✨ Features

- 🔐 **JWT Authentication** - Secure user authentication and authorization
- 👥 **User Management** - Registration, login, and role-based access control
- 📚 **Book Management** - Add, update, delete, and search books
- 📖 **Book Loans** - Checkout, return, and renew books
- 🔖 **Reservations** - Reserve books that are currently unavailable
- 💰 **Fine Management** - Automatic calculation and tracking of overdue fines
- 💳 **Payment Integration** - Razorpay payment gateway integration
- 📧 **Email Notifications** - Email notifications for various events
- 📊 **Subscription Plans** - Multiple subscription tiers with different benefits
- ❤️ **Wishlist** - Save favorite books for later

## 🛠️ Tech Stack

- **Backend**: Spring Boot 4.0.3
- **Database**: PostgreSQL (Neon)
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Java Version**: 21
- **Payment Gateway**: Razorpay
- **Email**: Spring Mail (Gmail SMTP)

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- PostgreSQL database (or Neon account)
- Gmail account (for email notifications)
- Razorpay account (for payment processing)

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Library-management-System-dev/lms-backend.git
cd lms-backend
```

### 2. Database Setup

Create a PostgreSQL database or use [Neon](https://neon.tech) (recommended - free tier available).

### 3. Environment Variables

Create a `.env` file in the root directory:

```properties
# Database
DB_URL=jdbc:postgresql://your-host:5432/your-database?sslmode=require
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT
JWT_SECRET_KEY=your_secure_jwt_secret_key_min_32_chars

# Email
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# Razorpay
RAZORPAY_KEY_ID=your_razorpay_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_CALLBACK_URL=http://localhost:5173

# Admin User
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=your_secure_admin_password
```

### 4. Build and Run

```bash
# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/signup` | Register new user |
| POST | `/auth/login` | User login |
| POST | `/auth/forgot-password` | Request password reset |
| POST | `/auth/reset-password` | Reset password |

### Books

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/books` | Get all books | Public |
| GET | `/api/books/{id}` | Get book by ID | Public |
| POST | `/api/books/admin` | Add new book | Admin |
| PUT | `/api/books/{id}` | Update book | Admin |
| DELETE | `/api/books/{id}` | Delete book | Admin |

### Subscriptions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/subscription-plans` | Get all plans |
| POST | `/api/subscription/subscribe` | Subscribe to plan |
| GET | `/api/subscription/user/active` | Get active subscription |

### Book Loans

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/book-loans/checkout` | Checkout book |
| POST | `/api/book-loans/checkin` | Return book |
| POST | `/api/book-loans/renew` | Renew loan |
| GET | `/api/book-loans/my` | Get my loans |

### Reservations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/reservations` | Create reservation |
| GET | `/api/reservations/my` | Get my reservations |
| DELETE | `/api/reservations/{id}` | Cancel reservation |

### Wishlist

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/wishlist/add/{bookId}` | Add to wishlist |
| GET | `/api/wishlist/my-wishlist` | Get my wishlist |
| DELETE | `/api/wishlist/remove/{bookId}` | Remove from wishlist |

### Fines

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/fines/my` | Get my fines |
| POST | `/api/fines/{id}/pay` | Pay fine |

## 🔒 Security Features

- ✅ JWT-based authentication
- ✅ Role-based access control (USER, ADMIN)
- ✅ Password encryption using BCrypt
- ✅ Environment variables for sensitive data
- ✅ CORS configuration
- ✅ SQL injection prevention via JPA

## 📁 Project Structure

```
lms-backend/
├── src/main/java/com/krish/
│   ├── configurations/      # Security, JWT, Database configs
│   ├── controller/          # REST Controllers
│   ├── domain/              # Enums and domain constants
│   ├── exception/           # Custom exceptions
│   ├── mapper/              # DTO mappers
│   ├── modal/               # Entity models
│   ├── payload/             # DTOs and request/response objects
│   ├── repository/          # JPA repositories
│   └── service/             # Business logic services
├── src/main/resources/
│   └── application.properties
├── .env.example             # Example environment variables
├── .gitignore              # Git ignore file
├── pom.xml                 # Maven dependencies
└── README.md               # This file
```

## 🧪 Testing

Run the API test suite:

```bash
bash test-apis.sh
```

## 🔧 Configuration

### Gmail App Password Setup

1. Go to Google Account > Security
2. Enable 2-Step Verification
3. Go to App passwords
4. Generate a new app password for "Mail"
5. Use this password in `MAIL_PASSWORD` environment variable

### Razorpay Setup

1. Sign up at [Razorpay](https://razorpay.com/)
2. Get your API keys from the dashboard
3. Use test keys for development
4. Use live keys for production

### JWT Secret Key

Generate a secure random key:
```bash
openssl rand -base64 32
```

## 🚀 Deployment

### Using Docker

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Environment Variables in Production

Make sure to set all environment variables in your deployment platform (Heroku, AWS, etc.)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👨‍💻 Author

**Krish Lohar**
- Email: loharkrish95@gmail.com
- GitHub: [@Library-management-System-dev](https://github.com/Library-management-System-dev)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Razorpay for payment integration
- Neon for PostgreSQL hosting
- All contributors to this project

---

**Built with ❤️ using Spring Boot**
