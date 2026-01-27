# Miku's Money Backend

A Spring Boot REST API for a personal finance management application.

## Requirements

- Java 17 or higher
- Maven 3.6+
- PostgreSQL

## Project Structure

```
src/main/java/com/example/mikusmoneybackend/
├── account/          # Account management
├── auth/             # Authentication & authorization (JWT)
├── config/           # Security and application configuration
├── credentials/      # User credentials management
├── deposit/          # Deposit operations
├── miku/             # Miku entity management
├── savings/          # Savings management
├── transactions/     # Transaction handling
├── transfer/         # Transfer operations
└── withdraw/         # Withdrawal operations
```

## Key Features

- **JWT Authentication**: Secure token-based authentication with cookie support
- **Account Management**: Create and manage user accounts
- **Transactions**: Handle deposits, withdrawals, and transfers
- **Savings**: Track and manage savings goals
- **Security**: JWT filters, password recovery, and PIN code management

## Configuration

Configure the application in `src/main/resources/application.properties`:

- Database connection
- JWT secret and expiration
- Server port
- Other application-specific settings

## API Endpoints

The application exposes REST endpoints for:

- `/auth/*` - Authentication (login, logout, password recovery)
- `/account/*` - Account operations
- `/deposit/*` - Deposit transactions
- `/withdraw/*` - Withdrawal transactions
- `/transfer/*` - Transfer operations
- `/transactions/*` - Transaction history
- `/savings/*` - Savings management

## Development

The project uses:

- **Spring Boot** - Application framework
- **Spring Security** - Authentication and authorization
- **JWT** - Token-based security
- **JPA/Hibernate** - Database ORM
- **Maven** - Dependency management
