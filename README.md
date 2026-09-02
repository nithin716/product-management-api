# Product Management API

A RESTful Product Management API built using Java and Spring Boot.

The application provides product CRUD operations, product-item retrieval, JWT-based authentication, refresh token rotation, role-based authorization, validation, pagination, centralized exception handling, Swagger/OpenAPI documentation, automated testing, and Docker support.

---

## Features

- Product CRUD operations
- Product item retrieval
- RESTful API design
- API versioning using `/api/v1`
- JWT authentication
- Refresh token rotation
- Role-based authorization
- USER and ADMIN roles
- Request validation
- Centralized exception handling
- Standardized JSON error responses
- Pagination for product listing
- Database indexing
- Swagger/OpenAPI documentation
- JUnit 5 and Mockito tests
- H2 database testing
- Docker containerization
- Docker Compose with MySQL
- CORS configuration
- Stateless Spring Security configuration

---

## Technology Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- Hibernate
- Spring Security
- JWT
- MySQL
- H2
- Maven
- JUnit 5
- Mockito
- Swagger / OpenAPI
- Docker
- Docker Compose

---

## Architecture

The application follows a layered architecture.

```text
Client / Postman / Swagger
            |
            v
     Spring Security
        JWT Filter
            |
            v
       Controller
            |
            v
         Service
            |
            v
       Repository
            |
            v
          MySQL
          
Supporting components:

DTOs
Validation
Exception Handler
JWT Authentication
Swagger/OpenAPI
JUnit + Mockito
H2
Docker
Main Layers
Controller Layer

Handles HTTP requests and responses.

Example:

/api/v1/products
/api/v1/auth/login
Service Layer

Contains application and business logic.

Repository Layer

Uses Spring Data JPA to communicate with the database.

Entity Layer

Represents database tables.

DTO Layer

Separates API request/response objects from database entities.

Security Layer

Handles JWT authentication and role-based authorization.

Database Design

The application uses MySQL.

Main tables:

users
refresh_tokens
product
item


Product

Column	      Description
id	          Primary key
product_name	Product name
created_by	Username that created the product
created_on	Creation timestamp
modified_by	Username that modified the product
modified_on	Modification timestamp



Item

Column      	Description
id			   Primary key
product_id	Foreign key to product
quantity	    Item quantity

Relationship:

Product 1 -------- * Item
Users

Stores application users and their roles.

Supported roles:

USER
ADMIN
Refresh Tokens

Refresh tokens are stored in the database and contain:

Token value
User
Expiration date
Revoked status
Authentication

The application uses JWT authentication.

Registration
POST /api/v1/auth/register

Example request:

{
  "username": "user1",
  "password": "password123"
}

Newly registered users receive the USER role by default.

Login

POST /api/v1/auth/login

Example request:

{
  "username": "user1",
  "password": "password123"
}

Successful login returns:

{
  "accessToken": "JWT_ACCESS_TOKEN",
  "refreshToken": "REFRESH_TOKEN",
  "tokenType": "Bearer"
}


Refresh Token
POST /api/v1/auth/refresh

Example request:

{
  "refreshToken": "REFRESH_TOKEN"
}

The application uses refresh token rotation.

When a refresh token is used:

The existing token is validated.
The existing refresh token is revoked.
A new access token is generated.
A new refresh token is generated.

This prevents reuse of the previous refresh token.


Authorization

Product endpoints require authentication.

USER

A USER can:

View products
Create products
Update products

A USER cannot:

Delete products
ADMIN

An ADMIN can:

View products
Create products
Update products
Delete products

Authorization is implemented using Spring Security roles.

API                 Endpoints

Authentication

Method    	Endpoint	              Authentication
POST	/api/v1/auth/register        	Public
POST	/api/v1/auth/login         	Public
POST	/api/v1/auth/refresh	       Public


Products

Method	      Endpoint				Access
GET    	/api/v1/products			USER / ADMIN
GET	      /api/v1/products/{id}	USER / ADMIN
POST		/api/v1/products			USER / ADMIN
PUT			/api/v1/products/{id}	USER / ADMIN
DELETE		/api/v1/products/{id}	ADMIN
GET			/api/v1/products/{id}/items	USER / ADMIN

Pagination

The product collection endpoint supports pagination.

Example:

GET /api/v1/products?page=0&size=10

Default values:

page = 0
size = 10

Validation

Product creation and update requests use Jakarta Bean Validation.

Example:

{
  "productName": ""
}

The API returns a standardized validation error response.

Error Handling

The application uses centralized exception handling.

Example:

{
  "timestamp": "2026-09-03T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 100",
  "path": "/api/v1/products/100"
}

Authentication and authorization errors also return JSON responses.

Example:

401 Unauthorized
403 Forbidden
404 Not Found
400 Bad Request

Database Indexing

Indexes are configured for frequently used database columns.

Product:

idx_product_created_on

Item:

idx_item_product_id

Additional indexes are provided by primary keys and unique constraints.

.........................

Swagger / OpenAPI

Swagger UI is available at:

http://localhost:8080/swagger-ui/index.html

OpenAPI specification:

http://localhost:8080/v3/api-docs

Swagger can be used to:

View API documentation
Test authentication
Test product endpoints
Test request validation
Test role-based authorization

.................
Running Locally
Prerequisites

Install:

Java 17 or later
Maven
MySQL
...............

Database

Create a MySQL database:

CREATE DATABASE product_db;

Configure the database connection in the application environment.

Example:

DB_URL=jdbc:mysql://localhost:3306/product_db
DB_USERNAME=root
DB_PASSWORD=your_password

.............
JWT Configuration

Configure:

JWT_SECRET=your-long-random-secret
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

The default access token expiration is:

15 minutes

The default refresh token expiration is:

7 days
................
Run the Application

Build the application:

mvn clean package

Run:

mvn spring-boot:run

The application starts on:

http://localhost:8080
.................
Running with Docker

Docker Compose starts:

Spring Boot Application
        +
      MySQL

Make sure Docker Desktop is running.

From the project root:

docker compose up -d

Check containers:

docker compose ps

Expected services:

product-api
product-api-mysql

View application logs:

docker compose logs -f product-api

Stop the application:

docker compose down

..........................

Docker Configuration

The application container communicates with MySQL using the Docker Compose service name:

mysql

The internal database URL is:

jdbc:mysql://mysql:3306/product_db

The application is exposed on:

http://localhost:8080

MySQL is exposed on the host for development purposes.

....................

Testing

The project uses:

JUnit 5
Mockito
Spring Boot Test
Spring Security Test
H2 in-memory database

Tests cover:

Product service
Authentication service
Product controller
Product repository
H2 database integration

Current test suite:

26 tests
26 passed
0 failed

Run tests:

mvn test
...............
Admin User for Local Testing

New users are registered with the USER role by default.

For local testing, an existing user can be promoted to ADMIN directly in the development database:

UPDATE users
SET role = 'ADMIN'
WHERE username = 'admin';

Verify:

SELECT id, username, role
FROM users;

Example:

admin  | ADMIN
admin1 | USER

After changing the role, log in again to obtain a JWT containing the updated role.

...............

Security

The application implements:

Stateless JWT authentication
BCrypt password hashing
Role-based authorization
Refresh token rotation
Revoked refresh token tracking
Request validation
Centralized error handling
CORS configuration

For production deployment:

Use HTTPS
Use a strong randomly generated JWT secret
Store secrets using environment variables or a secrets manager
Restrict CORS origins
Use secure database credentials
Do not commit .env files or secrets to Git

................
Project Structure

src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── zest/
│   │           ├── config/
│   │           ├── controller/
│   │           ├── dto/
│   │           ├── entity/
│   │           ├── exception/
│   │           ├── repository/
│   │           ├── security/
│   │           └── service/
│   │
│   └── resources/
│       └── application.properties/
│
└── test/
    ├── java/
    └── resources/
        └── application-test.properties

Dockerfile
docker-compose.yml
.dockerignore
.gitignore
pom.xml
README.md

.............
Design Decisions
Layered Architecture

A layered architecture was selected to keep responsibilities separated and make the application easier to maintain and test.

DTOs

DTOs prevent direct exposure of JPA entities through API requests and responses.

JWT

JWT provides stateless authentication suitable for REST APIs.

Refresh Token Rotation

Refresh token rotation reduces the risk of refresh-token reuse.

Role-Based Authorization

The application separates normal USER permissions from ADMIN permissions.

Database Indexes

Indexes are added to columns used frequently in relationships and queries.

H2 Testing

H2 provides an isolated in-memory database for repository testing.

Docker

Docker Compose provides a reproducible environment containing both the application and MySQL database.
..........

Future Production Improvements

Possible production improvements include:

HTTPS termination through a reverse proxy
Externalized secret management
More granular authorization policies
Database migration management using Flyway or Liquibase
Production database backups
Monitoring and logging
Rate limiting
CI/CD pipeline
..............
