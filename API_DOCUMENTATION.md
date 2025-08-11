# Library Management RESTful Web Service

## Overview
This is a complete RESTful web service for a simple online library application built with Kotlin and Ktor framework. The service manages three main resources: Books, Categories, and Customers.

## Features Implemented

### 1. Customer Management (CRUD Operations)
- **POST /customers** - Register a new customer (public)
- **GET /customers** - Get all customers (authenticated)
- **GET /customers/{id}** - Get customer by ID (authenticated)
- **PUT /customers/{id}** - Update customer (authenticated)
- **DELETE /customers/{id}** - Delete customer (authenticated)

### 2. Authentication & Authorization
- **POST /login** - Login endpoint that returns JWT token
- **Email validation** - Validates email address format using regex
- **Password hashing** - Simple password hashing for security
- **JWT token authentication** - Secures protected endpoints

### 3. Category Management (CRUD Operations)
- **GET /categories** - Get all categories (public)
- **GET /categories/{id}** - Get category by ID (public)
- **POST /categories** - Create category (authenticated)
- **PUT /categories/{id}** - Update category (authenticated)
- **DELETE /categories/{id}** - Delete category (authenticated, with constraint checking)

### 4. Book Management (CRUD Operations)
- **GET /books** - Get all books (public)
- **GET /books/{id}** - Get book by ID (public)
- **POST /books** - Create book (authenticated)
- **PUT /books/{id}** - Update book (authenticated)
- **DELETE /books/{id}** - Delete book (authenticated)

### 5. Data Models

#### Customer
```kotlin
- id: Int (auto-generated)
- name: String
- email: String (unique, validated)
- password: String (hashed)
```

#### Category
```kotlin
- id: Int (auto-generated)
- name: String
- description: String
- bookCount: Int (calculated)
```

#### Book
```kotlin
- id: Int (auto-generated)
- title: String
- author: String
- publisher: String
- publishingYear: Int
- categoryId: Int (foreign key)
- categoryName: String (joined)
```

### 6. Security Features
- JWT token-based authentication
- Email format validation using regex pattern
- Password hashing (simplified for demo)
- Protected endpoints require valid JWT token
- Anonymous access allowed for read operations on books and categories

### 7. Database
- H2 in-memory database for development/testing
- PostgreSQL support available (configurable)
- Proper foreign key constraints
- Automatic table creation

### 8. Error Handling
- Comprehensive error responses with proper HTTP status codes
- Custom exception types for different error scenarios
- Validation error handling
- Conflict detection (e.g., duplicate emails, deleting categories with books)

### 9. Testing
- Comprehensive test suite covering all endpoints
- Integration tests using Ktor test framework
- Authentication flow testing
- Validation testing
- Error scenario testing

## API Endpoints Summary

### Public Endpoints
- `GET /` - API status
- `POST /customers` - Customer registration
- `POST /login` - User authentication
- `GET /categories` - List all categories
- `GET /categories/{id}` - Get specific category
- `GET /books` - List all books
- `GET /books/{id}` - Get specific book

### Protected Endpoints (Require Authentication)
- `GET /customers` - List all customers
- `GET /customers/{id}` - Get specific customer
- `PUT /customers/{id}` - Update customer
- `DELETE /customers/{id}` - Delete customer
- `POST /categories` - Create category
- `PUT /categories/{id}` - Update category
- `DELETE /categories/{id}` - Delete category
- `POST /books` - Create book
- `PUT /books/{id}` - Update book
- `DELETE /books/{id}` - Delete book

## Technology Stack
- **Framework**: Ktor (Kotlin web framework)
- **Serialization**: Kotlinx Serialization
- **Authentication**: JWT (JSON Web Tokens)
- **Database**: H2 (in-memory) / PostgreSQL
- **Build Tool**: Gradle with Kotlin DSL
- **Testing**: Ktor Test Framework with JUnit

## Usage Example

1. **Register a customer**:
```json
POST /customers
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "password123"
}
```

2. **Login to get token**:
```json
POST /login
{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

3. **Create a category** (with Authorization header):
```json
POST /categories
Authorization: Bearer <jwt_token>
{
  "name": "Science Fiction",
  "description": "Books about futuristic themes"
}
```

4. **Create a book** (with Authorization header):
```json
POST /books
Authorization: Bearer <jwt_token>
{
  "title": "The Hitchhiker's Guide to the Galaxy",
  "author": "Douglas Adams",
  "publisher": "Pan Books",
  "publishingYear": 1979,
  "categoryId": 1
}
```

## Running the Application

1. **Build and run**:
```bash
./gradlew run
```

2. **Run tests**:
```bash
./gradlew test
```

3. **API Documentation**:
- Swagger UI available at: `http://localhost:8080/openapi`

The application runs on `http://localhost:8080` and is ready to handle requests according to the RESTful API specification.
