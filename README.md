# P2P Finance Application: Comprehensive Technical Report

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Project Overview](#2-project-overview)
   1. [Vision and Objectives](#21-vision-and-objectives)
   2. [Scope and Capabilities](#22-scope-and-capabilities)
   3. [Target Users](#23-target-users)
3. [System Architecture](#3-system-architecture)
   1. [Architectural Overview](#31-architectural-overview)
   2. [Architecture Diagram](#32-architecture-diagram)
   3. [Component Relationships](#33-component-relationships)
   4. [Technology Stack](#34-technology-stack)
   5. [Deployment Architecture](#35-deployment-architecture)
4. [Microservices Breakdown](#4-microservices-breakdown)
   1. [API Gateway](#41-api-gateway)
   2. [User Service](#42-user-service)
   3. [Wallet Service](#43-wallet-service)
   4. [Payment Service](#44-payment-service)
   5. [Notification Service](#45-notification-service)
   6. [Integration Service](#46-integration-service)
   7. [Supporting Services](#47-supporting-services)
5. [Domain Model](#5-domain-model)
   1. [User Domain](#51-user-domain)
   2. [Wallet Domain](#52-wallet-domain)
   3. [Payment Domain](#53-payment-domain)
   4. [Notification Domain](#54-notification-domain)
   5. [Cross-Domain Relationships](#55-cross-domain-relationships)
6. [Database Schema](#6-database-schema)
   1. [User Service Schema](#61-user-service-schema)
   2. [Wallet Service Schema](#62-wallet-service-schema)
   3. [Payment Service Schema](#63-payment-service-schema)
   4. [Notification Service Schema](#64-notification-service-schema)
   5. [Schema Relationships](#65-schema-relationships)
7. [API Endpoints](#7-api-endpoints)
   1. [User Service APIs](#71-user-service-apis)
   2. [Wallet Service APIs](#72-wallet-service-apis)
   3. [Payment Service APIs](#73-payment-service-apis)
   4. [Notification Service APIs](#74-notification-service-apis)
   5. [API Versioning Strategy](#75-api-versioning-strategy)
8. [External System Integration](#8-external-system-integration)
   1. [Fineract Integration](#81-fineract-integration)
   2. [Cyclos Integration](#82-cyclos-integration)
   3. [Integration Architecture](#83-integration-architecture)
   4. [Resilience Patterns](#84-resilience-patterns)
9. [Security Implementation](#9-security-implementation)
   1. [Authentication Mechanism](#91-authentication-mechanism)
   2. [Authorization Controls](#92-authorization-controls)
   3. [Data Protection](#93-data-protection)
   4. [Security Compliance](#94-security-compliance)
10. [Event-Driven Architecture](#10-event-driven-architecture)
   1. [Event Types](#101-event-types)
   2. [Kafka Implementation](#102-kafka-implementation)
   3. [Event Processing Flow](#103-event-processing-flow)
11. [Implementation Status](#11-implementation-status)
   1. [Completed Components](#111-completed-components)
   2. [In-Progress Components](#112-in-progress-components)
   3. [Pending Components](#113-pending-components)
12. [Development Workflow](#12-development-workflow)
   1. [Code Organization](#121-code-organization)
   2. [Building and Deployment](#122-building-and-deployment)
   3. [CI/CD Pipeline](#123-cicd-pipeline)
13. [Testing Strategy](#13-testing-strategy)
   1. [Testing Types](#131-testing-types)
   2. [Test Implementation](#132-test-implementation)
   3. [Notification Service Testing](#133-notification-service-testing)
   4. [Test Coverage Targets](#134-test-coverage-targets)
14. [Monitoring and Observability](#14-monitoring-and-observability)
   1. [Logging Strategy](#141-logging-strategy)
   2. [Metrics Collection](#142-metrics-collection)
   3. [Health Checks](#143-health-checks)
   4. [Alerting System](#144-alerting-system)
15. [Performance Considerations](#15-performance-considerations)
   1. [Caching Strategy](#151-caching-strategy)
   2. [Database Optimization](#152-database-optimization)
   3. [Scalability Approach](#153-scalability-approach)
16. [Development Roadmap](#16-development-roadmap)
   1. [Phase 1: Foundation (Completed)](#161-phase-1-foundation-completed)
   2. [Phase 2: Core Features (Substantial Progress)](#162-phase-2-core-features-substantial-progress)
   3. [Phase 3: Advanced Features (Planned)](#163-phase-3-advanced-features-planned)
   4. [Phase 4: Scale & Security (Planned)](#164-phase-4-scale--security-planned)
17. [Implementation Timeline](#17-implementation-timeline)
   1. [Current Progress](#171-current-progress)
   2. [Next Milestones](#172-next-milestones)
   3. [MVP Release Plan](#173-mvp-release-plan)
18. [Future Enhancements](#18-future-enhancements)
   1. [Technical Enhancements](#181-technical-enhancements)
   2. [Feature Enhancements](#182-feature-enhancements)
   3. [Integration Enhancements](#183-integration-enhancements)
19. [Setup & Installation Guide](#19-setup--installation-guide)
   1. [Prerequisites](#191-prerequisites)
   2. [Development Setup](#192-development-setup)
   3. [Configuration](#193-configuration)
20. [Conclusion and Recommendations](#20-conclusion-and-recommendations)
   1. [Current Status Summary](#201-current-status-summary)
   2. [Critical Path Items](#202-critical-path-items)
   3. [Strategic Recommendations](#203-strategic-recommendations)

---

## 1. Executive Summary

The P2P Finance Application is a comprehensive peer-to-peer payment platform designed to provide functionality similar to popular services like Venmo or Cash App. Built using a modern microservices architecture with Spring Boot and integrated with established financial platforms (Fineract and Cyclos), the application enables users to send/receive money, manage digital wallets, request payments, and split bills among friends.

The project follows a phased implementation approach, with core services for user management, wallet operations, and payment processing already completed. The application adheres to modern software engineering principles, including domain-driven design, event-driven architecture, comprehensive security measures, and resilient integration patterns.

This report provides a comprehensive overview of the application's architecture, components, development status, and roadmap for future enhancements.

---

## 2. Project Overview

### 2.1 Vision and Objectives

The P2P Finance Application aims to create a secure, scalable, and user-friendly platform for peer-to-peer financial transactions. The primary objectives include:

- Providing seamless money transfers between users
- Enabling comprehensive wallet management
- Supporting various payment scenarios (direct, requested, scheduled, split)
- Ensuring robust security and regulatory compliance
- Delivering real-time notifications across multiple channels
- Integrating with established financial systems for backend operations

### 2.2 Scope and Capabilities

The application encompasses the following core capabilities:

- **User Management**: Registration, authentication, profiles, and KYC verification
- **Wallet Management**: Create wallets, check balances, track transactions
- **Payment Processing**: Direct transfers, payment requests, scheduled payments, split payments
- **Notifications**: In-app, email, SMS, and push notifications
- **External Integration**: Connection with financial systems like Fineract and Cyclos

Additional capabilities planned or in development include:

- Advanced security features with fraud detection
- Social features for contact management
- Transaction history and analytics
- Multiple currency support
- Mobile and web interfaces

### 2.3 Target Users

The platform is designed to serve:

- **Individual Users**: For personal money transfers and financial management
- **Small Groups**: For splitting expenses and group payments
- **Small Businesses**: For accepting payments and managing transactions
- **Developers**: Through a robust API for integration with other applications

---

## 3. System Architecture

### 3.1 Architectural Overview

The P2P Finance application uses a microservices architecture, where functionality is divided into discrete, loosely-coupled services that communicate via well-defined APIs. This approach allows for:

- Independent scaling of services based on demand
- Technology diversity and flexibility
- Resilience through isolation of failures
- Parallel development by different teams
- Continuous deployment with minimal risk

Each service is responsible for a specific business domain and maintains its own data store, following the database-per-service pattern. Services communicate synchronously via REST APIs for immediate operations and asynchronously via Kafka for event-driven processes.

### 3.2 Architecture Diagram

The high-level architecture of the P2P Finance application is illustrated below:

```
[Mobile/Web Clients]
           ↓
   [API Gateway/Load Balancer]
           ↓
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
│  │User Service│  │Wallet Service│  │Payment Service│  │Notification│
│  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  │  Service  │
│        │              │              │        └─────┬─────┘
│        └──────────────┴──────────────┴──────────────┘     │
│                            ↓                              │
│  ┌─────────────────────────────────────────────────────┐  │
│  │              Integration Service                    │  │
│  │  ┌─────────────────────┐  ┌────────────────────┐   │  │
│  │  │    Fineract Client  │  │   Cyclos Client    │   │  │
│  │  └─────────────────────┘  └────────────────────┘   │  │
│  └─────────────────────────────────────────────────────┘  │
│                            ↑                              │
│  ┌─────────────────────┐   │   ┌────────────────────┐    │
│  │  Common Module      │───┘   │                    │    │
│  └─────────────────────┘       └────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
           ↓                           ↓
┌─────────────────────┐  ┌─────────────────────────┐
│Supporting Services  │  │      Data Stores        │
│- Discovery Service  │  │- PostgreSQL (per service)│
│- Config Service     │  │- Redis Cache            │
│- Monitoring         │  │- Kafka                  │
└─────────────────────┘  └─────────────────────────┘
           ↓                           ↓
┌─────────────────────┐  ┌─────────────────────────┐
│External Systems     │  │                          │
│- Fineract           │  │                          │
│- Cyclos             │  │                          │
└─────────────────────┘  └─────────────────────────┘
```

### 3.3 Component Relationships

The core services interact in the following ways:

- **API Gateway**: Routes client requests to appropriate microservices
- **User Service**: Manages user identity, authentication, and profiles
- **Wallet Service**: Handles financial accounts and transactions
- **Payment Service**: Orchestrates payment workflows between users
- **Notification Service**: Sends alerts across multiple channels
- **Integration Service**: Connects with external financial systems
- **Common Module**: Provides shared utilities, event infrastructure, and exceptions

These services communicate through:
- Synchronous REST API calls for immediate operations
- Asynchronous event messaging via Kafka for eventual consistency
- Shared configuration via Config Service
- Service discovery through Discovery Service

### 3.4 Technology Stack

The application is built using the following technologies:

**Core Framework**:
- Java 17
- Spring Boot 3.2.0
- Spring Cloud (for microservices capabilities)

**Database**:
- PostgreSQL (primary data store)
- Flyway (database migrations)
- Redis (caching and distributed data)

**Messaging**:
- Apache Kafka (event streaming)

**Security**:
- Spring Security
- JWT (JSON Web Tokens)
- OAuth2 (for authorization)

**Integration**:
- Spring WebClient (reactive HTTP client)
- Spring Cloud OpenFeign (declarative REST client)
- Resilience4j (circuit breaking, retry)
- Fineract and Cyclos clients

**Documentation**:
- OpenAPI/Swagger (via SpringDoc)

**Monitoring**:
- Spring Boot Actuator
- Prometheus
- Grafana
- ELK Stack (Elasticsearch, Logstash, Kibana)

**Testing**:
- JUnit Jupiter
- Mockito
- TestContainers
- WireMock
- REST Assured

**DevOps**:
- Docker
- Kubernetes
- GitHub Actions (CI/CD)
- Istio (Service Mesh)

**Utilities**:
- Lombok
- MapStruct

### 3.5 Deployment Architecture

The application is containerized and designed for deployment on Kubernetes:

- Each microservice is packaged as a Docker container
- Local development uses Docker Compose
- Production deployment uses Kubernetes
- Service mesh with Istio for advanced networking capabilities
- Horizontal scaling for high-demand services
- Load balancing across service instances
- Configuration through Kubernetes ConfigMaps and Secrets

---

## 4. Microservices Breakdown

### 4.1 API Gateway

The API Gateway serves as the single entry point for all client requests, routing them to appropriate microservices.

**Key Components**:
- **Route Configuration**: Defines path-based routing rules
- **Authentication Filter**: Validates JWT tokens
- **Rate Limiting**: Prevents API abuse
- **Request/Response Transformation**: Handles data formatting
- **Circuit Breaking**: Prevents cascading failures
- **Logging and Tracing**: Records request details with correlation IDs
- **Global Error Handling**: Standardizes error responses

**Implementation Files**:
- `ApiGatewayApplication.java`: Main application class
- `RouteConfig.java`: Defines routing rules
- `SecurityConfig.java`: JWT security configuration
- `RateLimiterConfig.java`: Rate limiting settings
- `AuthenticationFilter.java`: JWT validation logic
- `TracingFilter.java`: Request tracing implementation
- `GlobalErrorHandler.java`: Centralized error handling

### 4.2 User Service

The User Service manages user accounts, authentication, and profiles.

**Key Responsibilities**:
- User registration and account management
- Authentication and token generation
- Authentication with JWT-based token handling
- Profile information management with extensive data fields
- Password reset and recovery workflows
- Password management with secure hashing and reset capabilities
- email and phone verificagion workflows
- KYC (Know Your Customer) verification
- Role-based authorization
- OAuth2 integration for third-party authentication
- KYC (Kow Your Customer) status tracking
- Integration with external financial systems

**Domain Objects**:
- `User.java`: Core user entity. Rich entity with comprehensive validation and lifecycle management
- `UserProfile.java`: Extended user information. Detailed user profile information with preferences
- `VerificationToken.java`: Email/phone verification. Multi-purpose token system for verification workflows
- `UserStatus.java`: Account lifecycle states (PENDING, ACTIVE, SUSPENDED, CLOSED)
- `Role.java`: Authorization roles
- `KycStatus.java`: KYC verification states (NOT_STARTED, IN_PROGRESS, PENDING_REVIEW, APPROVED, REJECTED)
- `VerificationType.java`: Token purposes (EMAIL, PHONE, KYC_BASIC, KYC_FULL, PASSWORD_RESET)

**Implementation Files**:

-- `UserRepository.java`: Data access for user entities
UserServiceApplication.java: Main application class with Spring Boot features
UserController.java: REST endpoints for user operations
AuthController.java: Authentication endpoints for login/logout
OAuth2Controller.java: OAuth2 callback handling
AdminController.java: Administrative endpoints
UserService.java: Core business logic for user management
AuthService.java: Authentication and token handling
OAuth2Service.java: External authentication providers integration
JwtTokenProvider.java: JWT generation and validation
GlobalExceptionHandler.java: Centralized exception handling
and more ..


**Service Layer**
- UserService: business logic for user operations
- AuthService: Authentication and JWT generation
- OAuth2Service

**Security Implementation**
- JwtAuthenticationFilter
- JwtTokenProvider: JWT creation and validation
- SecurityConfig: Security configuration

**API Layer**
- AuthController: authentication APIs
- UserContoller: user management APIs
- AdminController: Admin APIs

**API Endpoints**
The User Service provides the following APIs:

Authentication APIs:

POST /api/v1/auth/login - Authenticate user
POST /api/v1/auth/refresh - Refresh access token
POST /api/v1/auth/logout - Logout user


User Management APIs:

POST /api/v1/users/register - Register new user
GET /api/v1/users/verify/{token} - Verify user account
GET /api/v1/users/me - Get current user profile
PUT /api/v1/users/profile - Update user profile
POST /api/v1/users/password/change - Change password
POST /api/v1/users/password/reset/request - Request password reset
POST /api/v1/users/password/reset - Reset password


Admin APIs:

GET /api/v1/admin/users - Get all users (admin only)





**Integration Layer**
The User Service integrates with external systems:
@FeignClient(name = "integration-service", url = "${integration-service.url}")
public interface IntegrationServiceClient {
@PostMapping("/api/v1/users/create")
CreateUserResponse createUser(@RequestBody CreateUserRequest request);

    @PostMapping("/api/v1/users/update")
    UpdateUserResponse updateUser(@RequestBody UpdateUserRequest request);
    
    @PostMapping("/api/v1/users/status")
    UpdateUserStatusResponse updateUserStatus(@RequestBody UpdateUserStatusRequest request);
}

And employs resilience patterns:
@CircuitBreaker(name = "integrationService", fallbackMethod = "registerUserFallback")
@Retry(name = "integrationService")
public UserResponse registerUser(UserRegistrationRequest request) {
// Implementation
}

private UserResponse registerUserFallback(UserRegistrationRequest request, Throwable t) {
log.warn("Fallback for registerUser executed due to: {}", t.getMessage());
throw new RuntimeException("Unable to register user at this time. Please try again later.");
}


### 4.3 Wallet Service

The Wallet Service manages digital wallets, balances, and financial transactions, with robust transaction handling and external system integration. The Wallet service implements a comprehensive digital wallet system

**Key Responsibilities**:
- Digital Wallet creation and management
- Balance tracking and updates
- Transaction processing (deposits, withdrawals, transfers)
- Transaction history
- Currency conversion
- Integration with external financial systems
- Wallet status management (active, frozen, closed)
- Digital wallet creation and management
- Transaction processing (deposits, withdrawals, transfers)
- Balance tracking and updates
- Currency conversion
- Integration with external financial systems
- Transaction logging and auditing
- Event publishing for cross-service communication

**Domain Objects**:
- `Wallet.java`: Core entity for managing digital wallets with rich domain logic. Digital wallet entity
- `Transaction.java`: Entity representing financial movements with transaction lifecycle management. Financial transaction record
- `Money.java`: Value object for currency amounts
- `WalletStatus.java`: Status lifecycle (ACTIVE, FROZEN, CLOSED). Enumeration of wallet states
- `TransactionType.java`: Types of financial transactions (DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, REFUND, FEE)
- `TransactionStatus.java`: Status lifecycle for transactions (PENDING, IN_PROGRESS, COMPLETED, FAILED)

- Enumerated Types:
   - WalletStatus
   - TransactionType
   - TransactionStatus

  **Implementation Files**:

WalletServiceApplication.java: Main application class with Spring Boot features
WalletController.java: REST endpoints for wallet operations
CurrencyController.java: REST endpoints for currency conversion
WalletService.java: Core business logic with transaction handling
IntegrationService.java: Interface for integration with external systems
FeignIntegrationService.java: Implementation using Feign client
CurrencyConversionService.java: Currency exchange capabilities
TransactionLogger.java: Audit logging for transactions
GlobalExceptionHandler.java: Centralized exception handling
and more...

**Service Layer**
- WalletService (complex business operations)
- FeignIntegrationService (external system interaction)
- TransactionLogger (event publishing)

**Repository Layer**
- WalletRepository: (specialized queries)
- TransactionRepository (advanced queries)

**API Layer**
- WalletController (RESTful API endpoints)
- CurrencyController (Currency operations)


**Domain Events**:
- `TransactionCompletedEvent`
- `WalletCreatedEvent`


Technical Features:
Transactional boundaries with isolation levels
Optimistic and pessimistic locking for concurrency control
Circuit breaker patterns for resilient external system integration
Event-driven communication via Kafka
Caching for exchange rates
Rich domain model with encapsulated business rules
Comprehensive error handling
External integration with resilience patterns

### 4.4 Payment Service

The Payment Service handles various payment operations between users.

**Key Responsibilities**:
- Direct transfers between users
- Payment request creation and management
- Scheduled payment setup and execution
- Split payment organization and tracking
- Payment status management
- Integration with Wallet Service for transfers

**Domain Objects**:
- `PaymentRequest.java`: Payment request entity
- `ScheduledPayment.java`: Recurring payment configuration
- `SplitPayment.java`: Group payment entity
- `PaymentStatus.java`: Enumeration of payment states

**Implementation Files**:
- `PaymentServiceApplication.java`: Main application class
- `PaymentRequestController.java`: REST API for payment requests
- `ScheduledPaymentController.java`: Scheduled payment endpoints
- `SplitPaymentController.java`: Split payment endpoints
- `PaymentRequestService.java`: Business logic for payment requests
- `ScheduledPaymentService.java`: Recurring payment logic
- `SplitPaymentService.java`: Split payment orchestration
- `PaymentRepository.java`: Data access for payment entities

### 4.5 Notification Service

The Notification Service sends notifications to users across multiple channels.

**Key Responsibilities**:
- Notification generation and delivery
- Multi-channel support (in-app, email, SMS, push)
- User notification preferences
- Template-based message creation
- Notification status tracking
- Notification history management
- Event-based notification triggers
- Process domain events from other services and generate appropriate notifications
- Manage notification templates with variable substitution
- Respect user notification preferences (channels, categories, quiet hours)
- Deliver notifications through multiple channels (in-app, email, SMS, push)
- Track notification delivery status and support retries
- Provide APIs for notification history and preference management
- Support administrative functions for template management
  Processing domain events from other services via Kafka
  Generating notifications based on templates
  Delivering notifications through multiple channels (in-app, email, SMS, push)
  Managing user notification preferences
  Supporting quiet hours and category-based filtering
  Maintaining notification history and status tracking

**Domain Objects**:
- `Notification.java`: Notification entity with status tracking
- `NotificationPreferences.java`: User preferences for notification
- `NotificationTemplate.java`: Message templates for different channels
- `DeliveryStatus.java`: Notification delivery status
- `NotificationType.java`: Types of notifications (APP, EMAIL, SMS, PUSH)
- `NotificationCategory.java`: Categories for notification grouping


**Implementation Files**:
- `NotificationServiceApplication.java`: Main application class with multiple Spring Boot features enabled
- `NotificationController.java`: REST API for notifications and notification management
- `NotificationPreferencesController.java`: PAPI for managing notification preferences. reference management
- `NotificationTemplateController.java`: Admin API for template management. Template management
- `NotificationService.java`: Core business logic for notification processing
- `NotificationSenderService.java`: Channel-specific delivery logic
- `NotificationEventListener.java`: Kafka event consumption with specialized handlers
- `TemplateRenderer.java`: Dynamic template rendering using Thymeleaf
- `FirebaseConfig.java`: Push notification integration and configuration

**Technical implementation**
The Notification Service is built using:

Spring Boot 3.x with Java 17
Spring Data JPA for database operations
Spring Kafka for event processing
Spring Mail for email delivery
Thymeleaf for template rendering
Firebase Cloud Messaging for push notifications
Redis for caching
Flyway for database migrations
Resilience4j for circuit breaking and retry mechanisms


**Core Components**

Controllers:

NotificationController - Manages notification operations
NotificationPreferencesController - Handles user preferences
NotificationTemplateController - Provides template management


Domain Model:

Notification - Core entity representing a notification
NotificationPreferences - User preferences for notifications
NotificationTemplate - Templates for generating notifications
Supporting enums: NotificationType, NotificationCategory, DeliveryStatus


Service Layer:

NotificationService - Core business logic for notifications
NotificationPreferencesService - Manages user preferences
NotificationTemplateService - Handles template operations
NotificationSenderService - Delivers notifications via different channels
TemplateRenderer - Renders templates with variable substitution


Event Processing:

The service implements comprehensive event listeners for various domain events:

User events (registration, verification)
Wallet events (transactions)
Payment Request events (created, approved, rejected)
Scheduled Payment events (created, executed, failed)
Split Payment events (created, participant actions)
Security events (login, password changes, suspicious activity)
NotificationEventListener - Consumes domain events from Kafka
Event handlers for user, wallet, payment, and security events


Configuration:

FirebaseConfig - Configures Firebase for push notifications
KafkaConsumerConfig - Sets up Kafka for event consumption

Delivery Mechanisms:
The service supports multiple notification channels:

In-app notifications (stored in database)
Email notifications (via JavaMailSender)
SMS notifications (implementation placeholder)
Push notifications (via Firebase Cloud Messaging)


### 4.6 Integration Service

The Integration Service connects the P2P Finance application with external financial systems, providing a robust bridge to Fineract and Cyclos.

**Key Responsibilities**:
- Integration with Fineract for core banking
- Integration with Cyclos for payment processing
- Client creation and account management
- Transaction processing through external systems
- Resilient communication with circuit breaking and retry mechanisms
- Error handling and fallback strategies
- Client creation and management in external systems
- Account creation and management

**Domain Objects**:
DTOs for Fineract API integration (ClientRequest, SavingsAccountRequest, etc.)
DTOs for Cyclos API integration (UserRegistrationRequest, AccountCreationRequest, etc.)
Request/response mapping objects for both systems


**Implementation Files**:
- `IntegrationServiceApplication.java`: Main application class
- `FineractConfig.java`: Fineract API configuration
- `CyclosConfig.java`: Cyclos API configuration
- `FeignClientConfig.java`: Feign client configuration with resilience
- `FineractIntegrationService.java`: Fineract API integration
- `CyclosIntegrationService.java`: Cyclos API integration
- `FineractApiClient.java`: Fineract API client
- `CyclosApiClient.java`: Cyclos API client
- `CyclosAuthService.java`: Authentication for Cyclos API
- various DTOs for request/response handling

**Technical Features**:
- Circuit breaker patterns with Resilience4j
- Retry mechanisms for transient failures
- Fallback strategies for graceful degradation
- WebClient and FeignClient for API communication
- Centralized authentication handling


The Integration Service demonstrates strong resilience patterns with circuit breakers, retries, and fallback mechanisms, ensuring robust communication with external systems

### 4.7 Supporting Services

Additional services that support the core application functionality:

**Discovery Service**:
- Service registry using Netflix Eureka
  @SpringBootApplication
  @EnableEurekaServer
  public class DiscoveryServiceApplication {
  // Main application
  }

- Dynamic service registration and discovery
- Load balancing support
- Configuration
  eureka:
  instance:
  hostname: localhost
  client:
  registerWithEureka: false
  fetchRegistry: false


**Config Service**:
- Centralized configuration management
- Environment-specific configurations
- Runtime configuration updates
- Git-based configuration storage


**Monitoring Services**:
- Prometheus for metrics collection
- Grafana for visualization
- ELK stack for centralized logging
- Health check endpoints and aggregation


**Common Module**:
The Common Module provides shared functionality, utilities, and abstractions used across all services in the application.

**Key Responsibilities**
Central exception handling with standardized error responses
Domain event infrastructure for asynchronous communication
Common configuration for security, API documentation, and tracing
Audit infrastructure for entity tracking
Utility classes for frequently needed operations
Base abstractions for domain events and messaging

**Key Components**
- Audit Framework: Automatic entity auditing with user tracking
- Event Infrastructure: Base definitions and publishing for domain events
- Exception Framework: Standardized exception hierarchy and handling
- OpenAPI Configuration: Common documentation setup for all services
- Security Abstractions: OAuth2 configuration and security utilities
- Utility Classes: Pagination, DTO mapping, string utilities
- Shared domain events

**implementation files**
- AuditConfig.java, Auditable.java, SpringSecurityAuditorAware.java: JPA auditing setup
- EventPublisher.java, DomainEvent.java, AbstractDomainEvent.java: Event infrastructure
- BusinessException.java and various specialized exceptions: Exception hierarchy
- OpenApiConfig.java: API documentation standardization
- DtoUtils.java, PageResponse.java, StringUtils.java: Common utilities
- GlobalExceptionHandler.java: Common exception handling


---

## 5. Domain Model

### 5.1 User Domain

The User Domain encompasses user accounts, authentication, and profiles.

**Key Entities**:

**User**:
- Primary entity for authentication and identity
- Contains credentials and basic information
- Properties: id, username, email, passwordHash, status, roles, createdAt, updatedAt, passwordHash, phoneNumber, externalId, kycStatus
- Behaviors:
   - Account lifecycle management (Activation/deactivation, Suspension, closure)
   - KYC status tracking and updates
   - Password updates and management
   - Role management,
   - Input validation (username, email, phone number)


**UserProfile**:
- Extended user information
- Properties: userId, firstName, lastName, dateOfBirth, address fields phoneNumber, profilePictureURL, preferedCurrency, preferences
- Behaviors:
   - Profile update management (name, address, and date of birth)
   - profile picture management
   - Preference handling (language, currency)

**VerificationToken**:
- Flexible token system for multiple verification purposes
- Used for email/phone verification and password reset
- Properties: id, userId, token, type, purpose, expiryDate, used, createdAt
- Behaviors:
   - Token validation,
   - Expiration checking and management
   - one-time use tracking
   - validation logic

**Role**:
- User authorization roles
- Properties: id, name, description

**Enumerations**
UserStatus:

PENDING - User has registered but not yet activated
ACTIVE - User is active and can perform operations
SUSPENDED - User is temporarily suspended
CLOSED - User account is permanently closed

KycStatus:

NOT_STARTED - KYC verification not yet started
IN_PROGRESS - KYC verification in progress
PENDING_REVIEW - KYC verification pending manual review
APPROVED - KYC verification approved
REJECTED - KYC verification rejected

VerificationType:

EMAIL - Email verification
PHONE - Phone verification
KYC_BASIC - Basic KYC verification
KYC_FULL - Full KYC verification
PASSWORD_RESET - Password reset verification



**Domain Rules**:
- Users must have unique username and email
- Passwords must be securely hashed
- Verification tokens expire after a specified time
- Users can have multiple roles
- KYC verification workflow with defined states
- Phone numbers must follow international format
- Usernames must follow specific format rules (letters, numbers, periods, underscores, hyphens)
- Email addresses must be valid and unique
- Phone numbers must follow international format
- Passwords are stored using BCrypt hashing
- Verification tokens have configurable expiration periods
- Users must verify their email before becoming active
- Account states follow a specific workflow (PENDING → ACTIVE → SUSPENDED/CLOSED)
  Phone numbers must follow international format (+XX...)
  Tokens expire after a configurable time
  Account activation requires email verification
  Password changes require current password verification
  Suspended users cannot perform operations
  Roles determine access permissions
  OAuth2 users are automatically activated
  Tokens can only be used once

### 5.2 Wallet Domain

The Wallet Domain manages digital wallets and financial transactions.

**Key Entities**:

**Wallet**:
- Digital wallet for storing money
- Properties: id, userId, externalID, walletType, accountType, balance, currency, status, createdAt, updatedAt, version (for optimistic locking)
- Behaviors: Balance management with validation, status transition (active, frozen, closed), Optimistic locking for concurrent modifications

**Transaction**:
- Record of financial movement
- Properties: id, walletID, externalID, sourceWalletId, targetWalletId, type, amount, currency, description, status, referenceId, createdAt, completedAt,updatedAt
- Behaviors: Transaction status tracking, completion and failure handling
   - Lifecycle management (creation, in-progress, completion, failure)
   - Transaction type specialization (deposit, withdrawal, transfer)
   - Audit trail with timestamps and user tracking

**Money**:
- Value object representing an amount with currency
- Properties: amount, currency
- Consistent representation across transactions and wallets

**Domain Rules**:
- Wallets must maintain non-negative balance for debits
- Validation for positive transaction amounts
- Transactions must be consistent (debits = credits)
- Currency must be valid according to ISO standards
- Wallet operations require optimistic locking for concurrent modifications
- Transactions are immutable once completed
- Transaction history must be preserved
- Wallet status transactions follow defined rules
  Wallets must maintain non-negative balance (checked before external operations)
  Active wallets can perform all operations
  Frozen wallets can receive deposits but not perform withdrawals or transfers
  Closed wallets cannot perform any operations
  Wallets must be in valid currency code format
  Amounts must be positive for all transactions
  Wallet operations use optimistic locking to prevent lost updates
  Wallets have a one-to-many relationship with transactions
  Users can have multiple wallets but only one per currency
  Closed wallets must have zero balance

### 5.3 Payment Domain

The Payment Domain handles payment operations between users.

**Key Entities**:

**PaymentRequest**:
- Request for payment between users
- Properties: id, requesterId, recipientId, amount, currency, description, status, frequency, startDate, endDate, expirationDate, createdAt, updatedAt
- Behaviors: Status transition, Expiration handling

**ScheduledPayment**:
- Configuration for recurring payments
- Properties: id, requesterId, senderId, recipientId, amount, currency, description, frequency, startDate, endDate, status, expirationDate, createdAt, updatedAt
- Behaviors: Scheduling logic, Status management

**ScheduledPaymentExecution**:
- Individual execution of a scheduled payment
- Properties: id, scheduledPaymentId, amount, status, executionDate, transactionId
- Behaviors: Execution status tracking

**SplitPayment**:
- Group payment shared among multiple users
- Properties: id, createdById, title, description, amount, totalAmount, currency, status, dueDate, createdAt, updatedAt
-

**SplitPaymentParticipant**:
- Individual participant in a split payment
- Properties: id, splitPaymentId, userId, amount, status, paidAt

**Domain Rules**:
- Payment requests expire after a specified period
- Scheduled payments follow defined frequency patterns
- Split payments divide total amount among participants
- Payment status transitions follow a defined state machine
- All payments require user authorization
- Failed payments must be recorded and reported

### 5.4 Notification Domain

The Notification Domain manages user notifications across multiple channels.

**Key Entities**:

**Notification**:
- Message sent to a user
- Properties: id, userId, title, message, type, category, referenceId, read, status, readAt, actionUrl, expiresAt, createdAt, deliveryStatus, deliveryError
- Behaviors: Marking as read, Delivery status tracking, Expiration handling
  Represents a message sent to a user
  Properties: id, userId, title, message, type, category, referenceId, read, actionUrl, deliveryStatus, createdAt, expiresAt, readAt
  Behaviors: markAsRead(), updateDeliveryStatus(), isExpired()

**NotificationPreferences**:
- User preferences for notifications
- Properties: id, userId, appNotificationsEnabled, emailNotificationsEnabled, smsNotificationsEnabled, pushNotificationsEnabled, categoryPreferences, emailEnabled, pushEnabled, smsEnabled, quietHoursStart, quietHoursEnd, categories, email, phoneNumber, deviceToken
- Behaviours:
> Channel enablement/disablement
> Quiet hours management
> Notification filtering
User preferences for notifications
Properties: userId, appNotificationsEnabled, emailNotificationsEnabled, smsNotificationsEnabled, pushNotificationsEnabled, categoryPreferences, quietHoursStart, quietHoursEnd, email, phoneNumber, deviceToken
Behaviors: shouldSendNotification(), isQuietHours(), setCategoryPreference()


**NotificationTemplate**:
- Template for generating notifications
- Properties: id, code, name, titleTemplate, messageTemplate, emailSubjectTemplate, emailBodyTemplate, actionUrlTemplate, title, message, emailTemplate, smsTemplate, pushTemplate, category, enabled
- Behaviours
> Template management
> Channel-specific content
Template for generating notifications
Properties: id, code, name, category, titleTemplate, messageTemplate, emailSubjectTemplate, emailBodyTemplate, smsTemplate, actionUrlTemplate, enabled
Behaviors: updateContent(), setEnabled()

**Domain Rules**:
- Notifications respect user preferences and quiet hours
- Templates support variable substitution through Thymeleaf
- Notifications can expire
- Delivery status is tracked for each notification
- Unread notifications are highlighted for users
- Categories determine notification grouping and preferences
- Templates can be enabled/disabled
  Users can control notification preferences by category and channel
  Notifications respect user-defined quiet hours
  Templates support variable substitution for personalization
  Notifications can expire after a specific time
  Failed notifications are automatically retried
  In-app notifications are always delivered (stored in database)
  Email, SMS, and push notifications require valid contact information

**Enumerations**
NotificationType:
- APP - In-app notification
- EMAIL - Email notification
- SMS - SMS notification
- PUSH - Push notification

NotificationCategory:
- ACCOUNT - Account-related notifications
- TRANSACTION - Transaction-related notifications
- PAYMENT_REQUEST - Payment request notifications
- SCHEDULED_PAYMENT - Scheduled payment notifications
- SPLIT_PAYMENT - Split payment notifications
- SECURITY - Security-related notifications

DeliveryStatus:
- PENDING - Notification is waiting to be delivered
- SENT - Notification has been sent
- DELIVERED - Notification has been delivered
- FAILED - Notification delivery failed
- EXPIRED - Notification has expired



### 5.5 Common Domain
The Common Module defines several cross-cutting domain concepts:
Event Model:

DomainEvent: Interface defining the contract for all domain events
AbstractDomainEvent: Base implementation with common properties
Custom event publishing with Kafka integration

Audit Model:

Auditable: Base class for entities requiring audit trails
Created/updated timestamps
Created by/updated by tracking
Version tracking for optimistic locking

Exception Hierarchy:

BusinessException: Base business exception
ResourceNotFoundException: For missing resources
DuplicateResourceException: For unique constraint violations
InsufficientFundsException: For financial operations without sufficient funds
InvalidResourceStateException: For invalid state transitions
TransactionFailedException: For failed financial transactions


### 5.6 Integration Domainain
The Integration Service acts as a bridge between the P2P Finance domain and external financial system domains:
Fineract Domain:

Client management (creating users in the banking system)
Savings account management
Transaction processing (deposits, withdrawals, transfers)

Cyclos Domain:

User management (creating users in the payment system)
Account management
Payment processing

Cross-Domain Mapping:

P2P Finance User -> Fineract Client and Cyclos User
P2P Finance Wallet -> Fineract Savings Account and Cyclos Account
P2P Finance Transaction -> Fineract Transaction and Cyclos Payment


## 5.5 Cross-Domain Relationships
The domains interact through the following relationships:

- **User-Wallet Relationship**: Each user can have multiple wallets in different currencies
- **Wallet-Transaction Relationship**: Wallets contain transaction history
- **User-Payment Relationship**: Users initiate and receive payments
- **Payment-Wallet Relationship**: Payments affect wallet balances
- **User-Notification Relationship**: Users receive notifications
- **Payment-Notification Relationship**: Payments trigger notifications
- **Wallet-Notification Relationship**: Wallet activities trigger notifications
  Wallet-Notification Relationship: Wallet activities trigger notifications
  User-Integration Relationship: Users have accounts in external systems
  Wallet-Integration Relationship: Wallets are linked to external accounts

These relationships are maintained through:
- Foreign key references in the database
- Event-based communication for cross-domain operations
- Service-to-service API calls for direct operations
- Domain events for asynchronous updates



---

## 6. Database Schema

### 6.1 User Service Schema

The User Service maintains the following database tables, as evidenced by the FLyway migration scripts:

**users**:
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    external_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    kyc_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
```


**user_profiles**:
```sql
CREATE TABLE user_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id),
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    date_of_birth DATE,
    address_line1 VARCHAR(100),
    address_line2 VARCHAR(100),
    city VARCHAR(50),
    state VARCHAR(50),
    postal_code VARCHAR(20),
    country VARCHAR(50),
    profile_picture_url VARCHAR(255),
    preferred_language VARCHAR(10) NOT NULL,
    preferred_currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
```

**user_roles**:
```sql
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);
```

**verification_tokens**:
```sql
CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

**Indexes**:
```sql
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone_number ON users(phone_number);
CREATE INDEX idx_users_external_id ON users(external_id);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_kyc_status ON users(kyc_status);

CREATE INDEX idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX idx_verification_tokens_expiry_date ON verification_tokens(expiry_date);
```


### 6.2 Wallet Service Schema

The Wallet Service maintains the following database tables as evidenced by the Flyway migration scripts:

**wallets**:
```sql
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    external_id VARCHAR(100) NOT NULL,
    wallet_type VARCHAR(50) NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    balance DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(user_id, currency)
);
```

**transactions**:
```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    external_id VARCHAR(100),
    source_wallet_id UUID,
    target_wallet_id UUID,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    reference_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
```


**Indexes**:
```sql
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_external_id ON wallets(external_id);
CREATE INDEX idx_wallets_status ON wallets(status);

CREATE INDEX idx_transactions_external_id ON transactions(external_id);
CREATE INDEX idx_transactions_source_wallet_id ON transactions(source_wallet_id);
CREATE INDEX idx_transactions_target_wallet_id ON transactions(target_wallet_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
```


### 6.3 Payment Service Schema

The Payment Service maintains the following database tables:

**payment_requests**:
```sql
CREATE TABLE payment_requests (
    id UUID PRIMARY KEY,
    requester_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    expiration_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**scheduled_payments**:
```sql
CREATE TABLE scheduled_payments (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description TEXT,
    frequency VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**scheduled_payment_executions**:
```sql
CREATE TABLE scheduled_payment_executions (
    id UUID PRIMARY KEY,
    scheduled_payment_id UUID REFERENCES scheduled_payments(id),
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    execution_date TIMESTAMP NOT NULL,
    transaction_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**split_payments**:
```sql
CREATE TABLE split_payments (
    id UUID PRIMARY KEY,
    created_by_id UUID NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    total_amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

**split_payment_participants**:
```sql
CREATE TABLE split_payment_participants (
    id UUID PRIMARY KEY,
    split_payment_id UUID REFERENCES split_payments(id),
    user_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```


**Indexes**:
```sql
CREATE INDEX idx_payment_requests_requester_id ON payment_requests(requester_id);
CREATE INDEX idx_payment_requests_recipient_id ON payment_requests(recipient_id);
CREATE INDEX idx_payment_requests_status ON payment_requests(status);
CREATE INDEX idx_scheduled_payments_sender_id ON scheduled_payments(sender_id);
CREATE INDEX idx_scheduled_payments_status ON scheduled_payments(status);
CREATE INDEX idx_split_payments_created_by_id ON split_payments(created_by_id);
```


### 6.4 Notification Service Schema

The Notification Service maintains the following database tables as evidenced by the Flyway migration scripts:

**notifications**:
```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(100) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(100),
    reference_id VARCHAR(100),
    read BOOLEAN NOT NULL DEFAULT FALSE,
    action_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    read_at TIMESTAMP,
    delivery_status VARCHAR(20) NOT NULL,
    delivery_error VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100)
);
```

**notification_preferences**:
```sql
CREATE TABLE notification_preferences (
    user_id UUID PRIMARY KEY,
    app_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    push_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    quiet_hours_start INTEGER,
    quiet_hours_end INTEGER,
    email VARCHAR(255),
    phone_number VARCHAR(20),
    device_token VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
```

**notification_templates**:
```sql
CREATE TABLE notification_templates (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    title_template VARCHAR(200) NOT NULL,
    message_template VARCHAR(2000) NOT NULL,
    email_subject_template VARCHAR(2000),
    email_body_template VARCHAR(5000),
    sms_template VARCHAR(200),
    action_url_template VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
```

**category_preferences**:
```sql
CREATE TABLE category_preferences (
    user_id UUID NOT NULL REFERENCES notification_preferences(user_id),
    category VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (user_id, category)
);
```

**Indexes**:
```sql
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_read ON notifications(read);
CREATE INDEX idx_notifications_delivery_status ON notifications(delivery_status);
CREATE INDEX idx_notifications_category ON notifications(category);
CREATE INDEX idx_notifications_reference_id ON notifications(reference_id);
CREATE INDEX idx_notification_templates_code ON notification_templates(code);
CREATE INDEX idx_notification_templates_category ON notification_templates(category);
```

### 6.5 Schema Relationships

The database schemas are related through the following foreign key relationships:

- `user_profiles.user_id` → `users.id`
- `user_roles.user_id` → `users.id`
- `verification_tokens.user_id` → `users.id`
- `transactions.source_wallet_id` → `wallets.id`
- `transactions.target_wallet_id` → `wallets.id`
- `scheduled_payment_executions.scheduled_payment_id` → `scheduled_payments.id`
- `split_payment_participants.split_payment_id` → `split_payments.id`
- `category_preferences.user_id` → `notification_preferences.user_id`
- `transactions.wallet_id` → `wallets.id`


Cross-service relationships are maintained through:
- Consistent use of UUID identifiers across services
- Event-based synchronization for cross-service data integrity
- Service-to-service API calls for data retrieval
- Domain events for eventual consistency

---

## 7. API Endpoints

### 7.1 User Service APIs

**Registration and Authentication:**

- `POST /api/v1/users/register` - Register new user
   - Request: `{ "username": string, "email": string, "password": string, "phoneNumber": string }`
   - Response: `{ "id": uuid, "username": string, "email": string, "status": string, "profile": object }`


- `POST /api/v1/auth/login` - Authenticate user
   - Request: `{ "usernameOrEmail": string, "password": string }`
   - Response: `{ "accessToken": string, "refreshToken": string, "tokenType": string, "expiresIn": number, "user": object }`

- `POST /api/v1/auth/refresh` - Refresh access token
   - Request: `{ "refreshToken": string }`
   - Response: `{ "accessToken": string, "refreshToken": string, "tokenType": string, "expiresIn": number, "user": object }`


- `POST /api/v1/auth/logout` - Logout user
   - Request: Header `Authorization: Bearer {token}`
   - Response: 204 No Content


**User Management:**


- `GET /api/v1/users/{userId}` - Get user profile by ID
   - Response: `{ "id": uuid, "username": string, "email": string, "profile": object }`


GET /api/v1/users/verify/{token} - Verify user account

Response: "Account verified successfully" or "Verification failed"


GET /api/v1/users/me - Get current user profile

Response: { "id": uuid, "username": string, "email": string, "profile": object }


PUT /api/v1/users/profile - Update user profile

Request: { "firstName": string, "lastName": string, ... }
Response: { "id": uuid, "username": string, "email": string, "profile": object }


POST /api/v1/users/password/change - Change password

Request: { "currentPassword": string, "newPassword": string }
Response: "Password changed successfully" or "Password change failed"


POST /api/v1/users/password/reset/request - Request password reset

Request: { "email": string }
Response: "Password reset instructions sent to your email"


POST /api/v1/users/password/reset - Reset password

Request: { "token": string, "newPassword": string }
Response: "Password reset successfully" or "Password reset failed"



**Password Management**:

- `POST /api/v1/users/password/change` - Change password
   - Request: `{ "currentPassword": string, "newPassword": string }`
   - Response: `"Password changed successfully"` or error message

- POST /api/v1/users/password/reset/request - Request password reset
   - Request: `{ "email": string }`
   - Response: `"Password reset instructions sent to your email"` or error message


- `POST /api/v1/users/password/rese` - Reset password with token
   - Request: `{ "token": string, "newPassword": string }`
   - Response: `"Password reset successfully"` or error message



**OAuth2 Integration**:

- `GET /api/v1/oauth2/callback` - OAuth2 callback endpoint
   - Request: `code` and `state` parameters
   - Response: `{ "accessToken": string, "refreshToken": string, "tokenType": string, "expiresIn": number, "user": object }`


**Admin Operations**:
- `GET /api/v1/admin/users` - Get all users (admin only)
   - Response: `[{ "id": uuid, "username": string, "email": string, ... }, ...]`





### 7.2 Wallet Service APIs

**Wallet Management:**

POST /api/v1/wallets - Create a wallet

Request: { "userId": uuid, "walletType": string, "accountType": string, "currency": string }
Response: { "id": uuid, "userId": uuid, "balance": number, "currency": string, "status": string, ... }


GET /api/v1/wallets/{walletId} - Get wallet details

Response: { "id": uuid, "userId": uuid, "balance": number, "currency": string, "status": string, ... }


GET /api/v1/wallets/user/{userId} - List user's wallets

Response: [{ "id": uuid, "userId": uuid, "balance": number, "currency": string, "status": string, ... }, ...]


**Transaction Operations:**

POST /api/v1/wallets/transfer - Transfer between wallets

Request: { "sourceWalletId": uuid, "targetWalletId": uuid, "amount": number, "description": string }
Response: { "id": uuid, "sourceWalletId": uuid, "targetWalletId": uuid, "amount": number, "status": string, ... }


POST /api/v1/wallets/deposit - Deposit to wallet

Request: { "walletId": uuid, "amount": number, "description": string }
Response: { "id": uuid, "targetWalletId": uuid, "amount": number, "status": string, ... }


POST /api/v1/wallets/withdraw - Withdraw from wallet

Request: { "walletId": uuid, "amount": number, "description": string }
Response: { "id": uuid, "sourceWalletId": uuid, "amount": number, "status": string, ... }


**Transaction History**:
GET /api/v1/wallets/{walletId}/transactions - Get wallet transactions

Query Parameters: page, size
Response: { "content": [{ "id": uuid, "amount": number, "type": string, ... }, ...], "totalElements": number, "totalPages": number, ... }


GET /api/v1/wallets/transactions/user/{userId} - Get user transactions

Query Parameters: page, size
Response: { "content": [{ "id": uuid, "amount": number, "type": string, ... }, ...], "totalElements": number, "totalPages": number, ... }


**Currency Operations**:

POST /api/v1/currency/convert - Convert currency

Request: { "amount": number, "sourceCurrency": string, "targetCurrency": string }
Response: { "sourceAmount": number, "sourceCurrency": string, "targetAmount": number, "targetCurrency": string, "exchangeRate": number, "timestamp": string }


GET /api/v1/currency/rate - Get exchange rate

Query Parameters: sourceCurrency, targetCurrency
Response: Exchange rate as decimal


### 7.3 Payment Service APIs

**Payment Requests:**

- `POST /api/v1/payments/request` - Create payment request
   - Request: `{ "requesterId": uuid, "recipientId": uuid, "amount": number, "currency": string, "description": string, "expirationDate": string }`
   - Response: `{ "id": uuid, "status": string, "message": string }`

- `GET /api/v1/payments/request/{id}` - Get payment request
   - Response: `{ "id": uuid, "requesterId": uuid, "recipientId": uuid, "amount": number, "currency": string, "status": string, ... }`

- `PUT /api/v1/payments/request/{id}/approve` - Approve payment
   - Response: `{ "id": uuid, "status": string, "transactionId": uuid, "message": string }`

- `PUT /api/v1/payments/request/{id}/cancel` - Cancel payment
   - Response: `{ "id": uuid, "status": string, "message": string }`

**Scheduled Payments:**

- `POST /api/v1/payments/scheduled` - Create scheduled payment
   - Request: `{ "senderId": uuid, "recipientId": uuid, "amount": number, "currency": string, "frequency": string, "startDate": string, "endDate": string, "description": string }`
   - Response: `{ "id": uuid, "status": string, "message": string }`

- `PUT /api/v1/payments/scheduled/{id}/pause` - Pause scheduled payment
   - Response: `{ "id": uuid, "status": string, "message": string }`

- `PUT /api/v1/payments/scheduled/{id}/resume` - Resume scheduled payment
   - Response: `{ "id": uuid, "status": string, "message": string }`

**Split Payments:**

- `POST /api/v1/payments/split` - Create split payment
   - Request: `{ "createdById": uuid, "title": string, "description": string, "totalAmount": number, "currency": string, "participants": [{ "userId": uuid, "amount": number }, ...], "dueDate": string }`
   - Response: `{ "id": uuid, "status": string, "message": string }`

- `POST /api/v1/payments/split/{id}/pay` - Pay split share
   - Request: `{ "participantId": uuid, "walletId": uuid }`
   - Response: `{ "id": uuid, "status": string, "transactionId": uuid, "message": string }`

### 7.4 Notification Service APIs

**Notification Management:**

POST /api/v1/notifications/send - Send notification

Request: { "userId": uuid, "templateCode": string, "parameters": object, "types": [string], "referenceId": string, "actionUrl": string, "expiresAt": string }
Response: [{ "id": uuid, "userId": uuid, "title": string, "message": string, ... }, ...]


GET /api/v1/notifications - List notifications

Query Parameters: page, size
Response: { "notifications": [{ "id": uuid, "title": string, "message": string, ... }, ...], "unreadCount": number, "totalPages": number, ... }


GET /api/v1/notifications/unread - List unread notifications

Query Parameters: page, size
Response: { "notifications": [{ "id": uuid, "title": string, "message": string, ... }, ...], "unreadCount": number, "totalPages": number, ... }


GET /api/v1/notifications/{id} - Get notification

Response: { "id": uuid, "title": string, "message": string, ... }


POST /api/v1/notifications/{id}/read - Mark notification as read

Response: { "id": uuid, "title": string, "message": string, "read": true, ... }


POST /api/v1/notifications/read-all - Mark all notifications as read

Response: 204 No Content

**Preference Management:**

GET /api/v1/notifications/preferences - Get notification preferences

Response: { "userId": uuid, "appNotificationsEnabled": boolean, "emailNotificationsEnabled": boolean, "smsNotificationsEnabled": boolean, "pushNotificationsEnabled": boolean, "categoryPreferences": object, ... }


PUT /api/v1/notifications/preferences - Update preferences

Request: { "appNotificationsEnabled": boolean, "emailNotificationsEnabled": boolean, "smsNotificationsEnabled": boolean, "pushNotificationsEnabled": boolean, "categoryPreferences": object, ... }
Response: { "userId": uuid, "appNotificationsEnabled": boolean, "emailNotificationsEnabled": boolean, ... }


POST /api/v1/notifications/device-token - Update device token

Request: { "deviceToken": string }
Response: 204 No Content


**Template Management:**

POST /api/v1/notifications/templates - Create template

Request: { "code": string, "name": string, "category": string, "titleTemplate": string, "messageTemplate": string, ... }
Response: { "id": uuid, "code": string, "name": string, ... }


PUT /api/v1/notifications/templates/{id} - Update template

Request: { "code": string, "name": string, "category": string, "titleTemplate": string, "messageTemplate": string, ... }
Response: { "id": uuid, "code": string, "name": string, ... }


GET /api/v1/notifications/templates/{id} - Get template

Response: { "id": uuid, "code": string, "name": string, ... }


GET /api/v1/notifications/templates - List all templates

Response: [{ "id": uuid, "code": string, "name": string, ... }, ...]


GET /api/v1/notifications/templates/category/{category} - List templates by category

Response: [{ "id": uuid, "code": string, "name": string, ... }, ...]

**Security Requirements**
All endpoints are secured with appropriate authorization:

Public endpoints: None (all require authentication)
User endpoints: Require authentication (isAuthenticated())
Admin endpoints: Require ADMIN role (hasRole('ADMIN'))




### 7.5 API Versioning Strategy

The API versioning strategy uses URL path versioning (`/api/v1/...`), which provides the following benefits:

- **Clarity**: Explicit versioning in the URL
- **Client Compatibility**: Ensures backward compatibility
- **Independent Evolution**: Allows services to evolve independently
- **Documentation**: Clear distinction between different API versions

Future API changes will adhere to these principles:
- Breaking changes require a new API version
- Additions to existing APIs don't require version changes
- Deprecation notices will be provided before removing old versions
- API versions have defined sunset schedules

The API design shows consistent patterns across services:

- Resource-based routing
- HTTP method semantics (GET, POST, PUT, DELETE)
- Query parameters for filtering and pagination
- Standardized response formats
- Error handling with descriptive messages

---

## 8. External System Integration

### 8.1 Fineract Integration

Apache Fineract is integrated as the core banking platform for financial services.

**Integration Components:**

- **Client Configuration**:
  ```java

@Configuration
@ConfigurationProperties(prefix = "fineract")
@Data
@Slf4j
public class FineractConfig {
private String baseUrl;
private String username;
private String password;
private String tenantId;
private int connectionTimeout = 60000;
private int readTimeout = 60000;

    @Bean
    public ApiClient fineractApiClient() {
        log.info("Initializing Fineract API client with base URL: {}", baseUrl);

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        apiClient.setConnectTimeout(connectionTimeout);
        apiClient.setReadTimeout(readTimeout);

        // Set up authentication
        HttpBasicAuth basicAuth = (HttpBasicAuth) apiClient.getAuthentication("basicAuth");
        basicAuth.setUsername(username);
        basicAuth.setPassword(password);

        // Set tenant ID
        ApiKeyAuth tenantAuth = (ApiKeyAuth) apiClient.getAuthentication("tenantid");
        tenantAuth.setApiKey(tenantId);

        return apiClient;
    }
}
  ```
  **Core Integration Services**:
  - Client Management: Creating and managing clients in Fineract
  - Account Operations: Creating and managing savings accounts
  - Transaction Processing: Deposit, withdrawal, and transfer operations
  - Balance Inquiries: Retrieving account balances and statements

  FineractIntegrationService: Main service for Fineract operations
FineractApiClient: Feign client for Fineract API

Functionality:

Client Management: Creating and managing clients in Fineract
Account Operations: Creating and managing savings accounts
Transaction Processing: Deposit, withdrawal, and transfer operations
Balance Inquiries: Retrieving account balances and statements


- **Resilience Patterns**:
  - Circuit breakers to handle service unavailability
  - Retry mechanisms for transient failures
  - Fallback strategies for degraded operation



### 8.2 Cyclos Integration

Cyclos is integrated as the payment platform for peer-to-peer transactions.

**Integration Components:**

- **Client Configuration**:
  ```java
@Configuration
@ConfigurationProperties(prefix = "cyclos")
@Data
@Slf4j
public class CyclosConfig {
    private String baseUrl;
    private String username;
    private String password;
    private String apiKey;
    private boolean useApiKey = false;
    private int connectionTimeout = 60000;

    @Bean
    public WebClient cyclosWebClient() {
        log.info("Initializing Cyclos WebClient with base URL: {}", baseUrl);

        WebClient.Builder builder = WebClient.builder()
            .baseUrl(baseUrl)
            .filter(logRequest())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // Add authentication
        if (useApiKey && apiKey != null && !apiKey.isEmpty()) {
            builder.defaultHeader("Api-Key", apiKey);
        } else {
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        }

        return builder.build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}
  ```

- **Core Integration Services**:
   - User Management: Creating and managing user accounts in Cyclos
   - Payment Processing: Handling payment operations
   - Account Management: Creating and managing payment accounts
   - Transaction History: Retrieving transaction records
   - CyclosIntegrationService: Main service for Cyclos operations
   - CyclosApiClient: Feign client for Cyclos API
   - CyclosAuthService: Authentication service for Cyclos

- **Resilience Patterns**:
   - WebClient with timeout configurations
   - Circuit breakers with Resilience4j
   - Exponential backoff retry strategies

  Circuit Breaker Pattern
  @CircuitBreaker(name = "fineractApi", fallbackMethod = "createClientFallback")
  @Retry(name = "fineractApi")
  public PostClientsResponse createClient(String firstName, String lastName, String externalId) {
  log.info("Creating client in Fineract: {} {}", firstName, lastName);

  ClientApi clientApi = new ClientApi(fineractApiClient);

  PostClientsRequest request = new PostClientsRequest()
  .firstName(firstName)
  .lastName(lastName)
  .externalId(externalId)
  .active(true)
  .locale("en")
  .dateFormat("dd MMMM yyyy")
  .activationDate(LocalDate.now().format(DATE_FORMAT));

  try {
  return clientApi.create11(request);
  } catch (Exception e) {
  log.error("Error creating client in Fineract", e);
  throw new RuntimeException("Failed to create client in Fineract", e);
  }
  }

private PostClientsResponse createClientFallback(String firstName, String lastName, String externalId, Throwable t) {
log.warn("Fallback for createClient executed due to: {}", t.getMessage());
// Return a dummy response
PostClientsResponse fallbackResponse = new PostClientsResponse();
fallbackResponse.setClientId(0L);
fallbackResponse.setResourceId(0L);
return fallbackResponse;
}


Retry Pattern:
@Bean
public RetryRegistry retryRegistry() {
RetryConfig config = RetryConfig.custom()
.maxAttempts(3)
.waitDuration(Duration.ofMillis(1000))
.retryExceptions(IOException.class, TimeoutException.class)
.ignoreExceptions(IllegalArgumentException.class)
.build();

    return RetryRegistry.of(Map.of("default", config));
}






- **Functionality**:
  User Management: Creating and managing user accounts in Cyclos
  Payment Processing: Handling payment operations
  Account Management: Creating and managing payment accounts
  Transaction History: Retrieving transaction records

### 8.3 Integration Architecture

The integration architecture follows a dedicated service pattern:

```
P2P Finance App Services
┌─────────────────────────────────────────────────────┐
│                                                     │
│  ┌──────────────┐   ┌─────────────┐   ┌───────────┐ │
│  │ User Service │   │Wallet Service│   │Other Svcs.│ │
│  └──────┬───────┘   └──────┬──────┘   └─────┬─────┘ │
│         │                  │                 │       │
└─────────┼──────────────────┼─────────────────┼───────┘
          │                  │                 │
          ▼                  ▼                 ▼
┌───────────────────────────────────────────────────────┐
│                Integration Service                     │
│ ┌─────────────────────────┐  ┌────────────────────────┐
│ │  Fineract Integration   │  │   Cyclos Integration   │
│ │  ┌──────────────────┐   │  │  ┌──────────────────┐  │
│ │  │ Circuit Breakers │   │  │  │ Circuit Breakers │  │
│ │  └──────────────────┘   │  │  └──────────────────┘  │
│ │  ┌──────────────────┐   │  │  ┌──────────────────┐  │
│ │  │  Retry Policies  │   │  │  │  Retry Policies  │  │
│ │  └──────────────────┘   │  │  └──────────────────┘  │
│ │  ┌──────────────────┐   │  │  ┌──────────────────┐  │
│ │  │ ApiClient/Feign  │   │  │  │    WebClient     │  │
│ │  └──────────────────┘   │  │  └──────────────────┘  │
│ └─────────────────────────┘  └────────────────────────┘
└───────────────────────────────────────────────────────┘
          │                                 │
          ▼                                 ▼
┌───────────────────────┐      ┌───────────────────────────┐
│   Fineract Platform   │      │      Cyclos Platform      │
│ (Core Banking Engine) │      │  (Payment System Engine)  │
└───────────────────────┘      └───────────────────────────┘

```

This architecture provides:
- Clear separation of concerns
- Isolation of integration complexity
- Consistent resilience patterns
- Unified approach to external systems


The Wallet Service implements a robust integration with external financial systems:
- Integration Service Interface:
  public interface IntegrationService {
  String createWallet(UUID userId, String walletType, String accountType, String currency);
  BigDecimal getWalletBalance(Wallet wallet);
  String transferBetweenWallets(Wallet sourceWallet, Wallet targetWallet, BigDecimal amount);
  String depositToWallet(Wallet wallet, BigDecimal amount);
  String withdrawFromWallet(Wallet wallet, BigDecimal amount);
  }

-Feign Client implementation:
@FeignClient(name = "integration-service", url = "${integration-service.url}")
public interface IntegrationServiceClient {
@PostMapping("/api/v1/wallets/create")
CreateWalletResponse createWallet(@RequestBody CreateWalletRequest request);

    @PostMapping("/api/v1/wallets/balance")
    GetBalanceResponse getBalance(@RequestBody GetBalanceRequest request);
    
    @PostMapping("/api/v1/wallets/transfer")
    TransferResponse transfer(@RequestBody TransferRequest request);
    
    @PostMapping("/api/v1/wallets/deposit")
    DepositResponse deposit(@RequestBody DepositRequest request);
    
    @PostMapping("/api/v1/wallets/withdraw")
    WithdrawalResponse withdraw(@RequestBody WithdrawalRequest request);
}



### 8.4 Resilience Patterns

The integration uses the following resilience patterns:

**Circuit Breaker Pattern**:
```java
@CircuitBreaker(name = "fineractApi", fallbackMethod = "createClientFallback")
@Retry(name = "fineractApi")
public PostClientsResponse createClient(String firstName, String lastName, String externalId) {
    // Implementation...
}

private PostClientsResponse createClientFallback(String firstName, String lastName, String externalId, Throwable t) {
    log.warn("Fallback for createClient executed due to: {}", t.getMessage());
    // Fallback implementation...
}
```

**Retry Pattern**:
```java
@Bean
public RetryRegistry retryRegistry() {
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.ofMillis(1000))
        .retryExceptions(IOException.class, TimeoutException.class)
        .ignoreExceptions(IllegalArgumentException.class)
        .build();
    
    return RetryRegistry.of(Map.of("default", config));
}
```

**Timeout Pattern**:
```java
@Bean
public WebClient webClient() {
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofMillis(5000));
    
    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
}


Circuit breaker configuration:

resilience4j:
  circuitbreaker:
    instances:
      fineractApi:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
      cyclosApi:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
```

These patterns ensure that the application:
- Gracefully handles external system failures
- Prevents cascading failures across services
- Recovers automatically from transient issues
- Maintains responsiveness under adverse conditions

---
Wallet service implements comprehensive resilience patterns:
@CircuitBreaker(name = "integrationService", fallbackMethod = "createWalletFallback")
@Retry(name = "integrationService")
public String createWallet(UUID userId, String walletType, String accountType, String currency) {
// Implementation...
}

private String createWalletFallback(UUID userId, String walletType, String accountType,
String currency, Throwable t) {
log.warn("Fallback for createWallet executed due to: {}", t.getMessage());
throw new TransactionFailedException("External service unavailable. Please try again later.");
}
resilience4j:
circuitbreaker:
instances:
integrationService:
registerHealthIndicator: true
slidingWindowSize: 10
minimumNumberOfCalls: 5
permittedNumberOfCallsInHalfOpenState: 3
automaticTransitionFromOpenToHalfOpenEnabled: true
waitDurationInOpenState: 5s
failureRateThreshold: 50
retry:
instances:
integrationService:
maxAttempts: 3
waitDuration: 1s
enableExponentialBackoff: true
exponentialBackoffMultiplier: 2


## 9. Security Implementation

### 9.1 Authentication Mechanism

The application uses JWT (JSON Web Token) for authentication:

**JWT Token Generation**:
```java
public String generateToken(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
    
    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("username", user.getUsername())
        .claim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
        .setIssuedAt(new Date())
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
}
```

**JWT Token Validation**:
```java
public boolean validateToken(String token) {
    try {
        Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
        return true;
    } catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
        log.error("Token validation error: {}", ex.getMessage());
        return false;
    }
}
```

**Authentication Flow**:
1. User submits credentials to `/api/v1/auth/login`
2. User Service verifies credentials and generates JWT tokens
3. Client stores tokens and includes them in Authorization header
4. API Gateway validates tokens for all secured requests
5. Refresh tokens allow obtaining new access tokens without re-authentication

### 9.2 Authorization Controls

The application implements role-based access control (RBAC):

**Security Configuration**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/v1/auth/**", "/api/v1/users/register").permitAll()
                .antMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
```

**Method-Level Security**:
```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        // Implementation...
    }
}
```

**Authorization Roles**:
- `USER`: Standard user with basic access
- `ADMIN`: Administrative access to management functions
- `SUPPORT`: Customer support access

### 9.3 Data Protection

The application implements multiple layers of data protection:

**Password Hashing**:
```java
@Service
public class UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(UserRegistrationRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Additional setup...
        return userRepository.save(user);
    }
}
```

**Sensitive Data Handling**:
- Personally Identifiable Information (PII) is stored encrypted
- Credit card information is never stored directly
- Financial data is protected with field-level encryption
- Logging excludes sensitive information

**Data Masking**:
```java
@JsonComponent
public class SensitiveDataMasker {
    
    @JsonComponent
    public static class SensitiveDataSerializer extends JsonSerializer<String> {
        
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            
            // Mask the middle digits, keeping first 4 and last 4
            if (value.length() > 8) {
                String masked = value.substring(0, 4) + "****" + value.substring(value.length() - 4);
                gen.writeString(masked);
            } else {
                gen.writeString("****");
            }
        }
    }
}
```

### 9.4 Security Compliance

The application implements security measures to comply with industry standards:

**Financial Data Security**:
- PCI DSS compliance for payment data handling
- Financial transaction logging for audit purposes
- Dual control for critical operations

**User Data Protection**:
- GDPR compliance for European users
- Data minimization principles
- Right to access and deletion

**Security Headers**:
```java
@Component
public class SecurityHeadersFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Content-Security-Policy", "default-src 'self'");
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        chain.doFilter(request, response);
    }
}
```

**API Security**:
- Rate limiting to prevent abuse
- Input validation to prevent injection attacks
- HTTPS for all communications

---

## 10. Event-Driven Architecture

### 10.1 Event Types

The application uses domain events for asynchronous communication:

**User Events**:
- `UserRegisteredEvent`: User registration completed
- `UserVerifiedEvent`: User email/phone verified
- `UserStatusChangedEvent`: User status updated

**Wallet Events**:
- `WalletCreatedEvent`: New wallet created
- `WalletBalanceChangedEvent`: Wallet balance updated (implicit in transaction events)
- `TransactionCompletedEvent`: Transaction processed
- `WalletTransactionEvent` - Wallet transaction (deposit, withdrawal, transfer)
- `WalletStatusChangedEvent` - Wallet status changed (implicit in transaction logs)
  Transaction Events:
- `TransactionCompletedEvent`: Transaction processed successfully

**Payment Events**:
- `PaymentRequestCreatedEvent`: Payment request created
- `PaymentRequestApprovedEvent`: Payment request approved
- `PaymentRequestCanceledEvent`: Payment request canceled
- `ScheduledPaymentCreatedEvent`: Scheduled payment created
- `SplitPaymentCreatedEvent`: Split payment created
- `PaymentRequestEvent` - Payment request lifecycle events
- `ScheduledPaymentEvent` - Scheduled payment lifecycle events
- `SplitPaymentEvent` - Split payment lifecycle events

**Notification Events**:
- `NotificationCreatedEvent`: Notification created
- `NotificationDeliveredEvent`: Notification delivered
- `NotificationReadEvent`: Notification marked as read

**Security Events**:
- `SecurityEvent` - Security-related events (login, password change, etc.)



**Event Structure**:
```java
public abstract class AbstractDomainEvent implements DomainEvent {
    private UUID eventId;
    private Instant timestamp;
    private String eventType;
    private String source;
    private Map<String, Object> metadata;
    
    // Getters, setters, and constructors...
}

public class UserRegisteredEvent extends AbstractDomainEvent {
    private UUID userId;
    private String username;
    private String email;
    
    // Getters, setters, and constructors...
}
```

### 10.2 Kafka Implementation

Kafka is used for event streaming between services:

**Kafka Configuration**:
```java
@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public ConsumerFactory<String, DomainEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "p2p-finance-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
```

**Event Publishing**:
```java
@Service
public class EventPublisher {
    
    @Autowired
    private KafkaTemplate<String, DomainEvent> kafkaTemplate;
    
    public void publish(DomainEvent event, String topic) {
        kafkaTemplate.send(topic, event.getEventId().toString(), event);
        log.info("Published event {} to topic {}", event.getEventId(), topic);
    }
}
```

**Event Consumption**:
```java
@Service
public class NotificationEventListener {
    
    @Autowired
    private NotificationService notificationService;
    
    @KafkaListener(topics = "payment-events", groupId = "notification-service")
    public void handlePaymentEvent(PaymentRequestApprovedEvent event) {
        log.info("Received payment event: {}", event.getEventId());
        notificationService.sendPaymentNotification(event.getPaymentId(), event.getUserId());
    }
    
    @KafkaListener(topics = "wallet-events", groupId = "notification-service")
    public void handleWalletEvent(TransactionCompletedEvent event) {
        log.info("Received wallet event: {}", event.getEventId());
        notificationService.sendTransactionNotification(event.getTransactionId(), event.getUserId());
    }
}
```

- The Notification Service implements Kafka consumers for these events:

@KafkaListener(topics = "user-events", groupId = "notification-service")
public void consumeUserEvents(String message) {
try {
NotificationEvent baseEvent = objectMapper.readValue(message, NotificationEvent.class);

        switch (baseEvent.getEventType()) {
            case "USER_REGISTERED" -> handleUserRegisteredEvent(message);
            case "USER_VERIFIED" -> handleUserVerifiedEvent(message);
            default -> log.warn("Unknown user event type: {}", baseEvent.getEventType());
        }
    } catch (Exception e) {
        log.error("Error processing user event", e);
    }
}
Similar listeners exist for wallet events, payment events, and security events.

### 10.3 Event Processing Flow

Events flow through the system following these patterns:

**Event Production**:
1. Service completes a business operation
2. Service creates a domain event
3. Event is published to Kafka topic
4. Service continues processing (fire-and-forget)

**Event Consumption**:
1. Listener service subscribes to Kafka topic
2. Event is received and deserialized
3. Business logic processes the event
4. Success/failure is recorded (not returned to producer)

**Example Flow: User Registration to Notification**:
1. User Service registers a new user
2. User Service publishes `UserRegisteredEvent` to Kafka
3. Notification Service consumes the event
4. Notification Service sends welcome notification
5. Integration Service consumes the event
6. Integration Service creates accounts in external systems

This event-driven approach provides:
- Loose coupling between services
- Asynchronous processing for better scalability
- Resilience through message persistence
- Eventual consistency across the system

For the Notification Service, the event processing flow follows this pattern:
- Event Reception: The service receives an event via Kafka
- Event Classification: The event type is determined
- Event Handling: A specialized handler processes the event
- Template Selection: An appropriate notification template is selected
- Parameter Extraction: Parameters are extracted from the event
- Notification Creation: A notification is created using the template and parameters
- Channel Selection: Delivery channels are selected based on user preferences
- Notification Delivery: The notification is delivered through selected channels
- Status Tracking: Delivery status is tracked and updated

This process is asynchronous and resilient, with error handling and retry capabilities.

---

## 11. Implementation Status

### 11.1 Completed Components

The following components have been fully implemented with code evidence present in the repository:

**Payment Service**: ✅ Complete
- **Domain Model**: Fully implemented with rich entity classes (`PaymentRequest.java`, `ScheduledPayment.java`, `SplitPayment.java`)
- **API Controllers**: Complete controllers with proper request/response handling (`PaymentRequestController.java`, `ScheduledPaymentController.java`, `SplitPaymentController.java`)
- **Business Logic**: Comprehensive service implementations (`PaymentRequestService.java`, `ScheduledPaymentService.java`, `SplitPaymentService.java`)
- **Data Access Layer**: Specialized repository interfaces with custom queries (`PaymentRequestRepository.java`, `ScheduledPaymentRepository.java`, `SplitPaymentRepository.java`)
- **Client Integration**: Service clients for user and wallet services (`UserServiceClient.java`, `WalletServiceClient.java`)
- **Event Publishing**: Kafka event publishing for notifications (`KafkaEventPublisher.java`)
- **Database Migrations**: Flyway migration scripts for schema creation (`V1__create_payment_tables.sql`)
- **Error Handling**: Global exception handling with specific exception types (`GlobalExceptionHandler.java`)
- **External Dependencies**: Properly defined in `pom.xml` with version management

**User Service**:
✅ Rich domain model with validation
✅ JWT-based authentication and authorization
✅ User registration and verification
✅ Profile management
✅ Password management (change, reset)
✅ OAuth2 integration
✅ Integration with external systems
✅ Security features with role-based access control
✅ Error handling and validation
🟨 Advanced KYC verification (partial implementation)
🟨 Multi-factor authentication (basic structure present)


**Wallet Service**: ✅ Complete
- Wallet creation and management
- Transaction processing with optimistic locking
- Balance tracking with currency support
- Transfer operations between wallets
- Transaction history and reporting
- Integration with external financial systems
  ✅ Domain model with rich business logic
  ✅ Comprehensive transaction management
  ✅ External system integration
  ✅ Event publishing
  ✅ API endpoints
  ✅ Repository layer with optimized queries
  ✅ Validation and error handling
  ✅ Transaction isolation and locking strategies
  🟨 Currency conversion service (basic implementation)
  🟨 Advanced analytics features (partial implementation)

**Common Module**: ✅ Complete
- Cross-cutting concerns (auditing, exception handling)
- Shared domain events
- Common utilities
- Base configuration templates

**Discovery Service**: ✅ Complete
- Service discovery configuration
  ✅ Config Server with Git backend
  ✅ Service discovery with Eureka
  ✅ Security configuration
  ✅ Health checks and monitoring


**Config Service**: ✅ Complete
- Centralized configuration management
  Centralized Configuration: Git-backed config server:
  @SpringBootApplication
  @EnableConfigServer
  @EnableEurekaClient
  public class ConfigServiceApplication {
  // Main application
  }


- Environment-specific configuration profiles
  spring:
  cloud:
  config:
  server:
  git:
  uri: ${CONFIG_GIT_URI:https://github.com/yourusername/p2p-finance-config}
  default-label: ${CONFIG_GIT_BRANCH:main}

**Notificatin Service**
- Domain Model: ✅ Fully implemented with rich entity classes and business logic
- API Controllers: ✅ Complete controllers for notifications, preferences, and templates
- Business Logic: ✅ Comprehensive service implementations
- Data Access Layer: ✅ Repository interfaces with optimized queries
- Event Processing: ✅ Kafka event listeners for all major domain events
- Template System: ✅ Complete template management and rendering
- Multi-Channel Delivery: ✅ In-app, email, and push notifications implemented
- User Preferences: ✅ Complete preference management system
- Database Migrations: ✅ Flyway migration scripts for schema creation



### 11.2 In-Progress Components

These components have partial implementation with some features pending:

**API Gateway**: 🟨 Partially Complete
- ✅ Route configuration for all microservices
- ✅ JWT-based authentication and authorization
- ✅ Rate limiting with Redis
- ✅ Circuit breaking with fallback handling
- ✅ Request/response logging
- ✅ Distributed tracing support
- ✅ Comprehensive error handling
- ✅ CORS configuration
- ✅ Security headers implementation
- 🟨 Advanced metrics collection (partially implemented)
- 🟨 Custom response transformation (basic implementation)

**Notification Service**: 🟨 Partially Complete
- ✅ Domain model (`Notification.java`, `NotificationPreferences.java`, `NotificationTemplate.java`)
- ✅ Repository interfaces (`NotificationRepository.java`, `NotificationPreferencesRepository.java`)
- ✅ API endpoints (`NotificationController.java`, `NotificationPreferencesController.java`)
- ✅ Database schema (`V1__create_notification_tables.sql`)
- 🟨 Service implementations (core functionality in `NotificationService.java`)
- 🟨 Firebase integration (configuration in `FirebaseConfig.java`)
- SMS Delivery: 🟨 Framework in place, external service integration pending
- Advanced Analytics: 🟨 Basic tracking implemented, detailed reporting pending
- Internationalization: 🟨 Basic structure in place, full support pending
- Performance Optimizations: 🟨 Basic caching implemented, advanced optimizations pending

**Integration Service**: 🟨 Partially Complete
- ✅ Base configuration for external systems (`FineractConfig.java`, `CyclosConfig.java`)
- ✅ API client interfaces (`FineractApiClient.java`, `CyclosApiClient.java`)
- ✅ DTO models for data exchange
- 🟨 Service implementations (partial implementation for core operations)
- 🟨 Error handling and resilience (basic circuit breaking)
- ❌ Comprehensive monitoring
- ❌ Complete feature coverage for all external system capabilities
- ❌ Advanced reconciliation processes

### 11.3 Pending Components

These components are planned but have minimal or no implementation yet:

**Mobile/Web UI**: ❌ Not Started
- ❌ User interface design
- ❌ Registration and login screens
- ❌ Wallet management interface
- ❌ Payment operations
- ❌ Notification handling

**Admin Dashboard**: ❌ Not Started
- ❌ User management console
- ❌ System monitoring dashboards
- ❌ Transaction oversight and management
- ❌ Configuration management
- ❌ Reporting and analytics

**Advanced Security Features**: ❌ Limited Implementation
- 🟨 Basic security framework implemented
- ❌ Two-factor authentication
- ❌ Advanced fraud detection
- ❌ Behavioral analysis
- ❌ Enhanced compliance monitoring

**Enhanced Analytics**: ❌ Not Started
- ❌ Business intelligence dashboards
- ❌ User behavior analysis
- ❌ Transaction pattern recognition
- ❌ Regulatory reporting
- ❌ Predictive analytics

**Production Deployment Infrastructure**: 🟨 Partially Implemented
- ✅ Docker containerization (Dockerfiles for services)
- ✅ Basic Kubernetes configuration (namespace, configmap)
- 🟨 CI/CD pipeline (limited implementation)
- ❌ Advanced Kubernetes operators
- ❌ Auto-scaling configuration
- ❌ Production monitoring and alerting
- ❌ Disaster recovery procedures

**Notification Service**
- ❌ Bulk notification capabilities
- ❌ Notification batching and rate limiting
- ❌ Advanced internationalization support
- ❌ Comprehensive notification analytics
- ❌

**Concepts from Briar P2P messagging app**


### 11.4 Feature Implementation Analysis

Based on the payment-service implementation code review:

**Completed Features**:
- **Payment Requests**: Full CRUD operations, approval/rejection workflows, expiration handling
- **Scheduled Payments**: Creation, pausing, resuming, cancellation, and automatic execution
- **Split Payments**: Group expense management with participant tracking, payment collection
- **Event Publishing**: Asynchronous notifications via Kafka for all payment state changes
- **Service Integration**: Communication with User and Wallet services
- **Security**: JWT authentication, role-based authorization for endpoints
- **Error Handling**: Specialized exceptions and global error handling
- **Metrics Collection**: Basic metrics for payment operations

**Partially Implemented**:
- **Resilience Patterns**: Circuit breakers defined but not fully configured for all scenarios
- **Cache Management**: Basic caching structure but not comprehensive
- **Advanced Metrics**: Limited custom business metrics
- **Rate Limiting**: Basic structure but not fully implemented

**Missing or Needs Enhancement**:
- **Comprehensive Testing**: Limited test coverage, especially for integration scenarios
- **Documentation**: Minimal API documentation
- **Advanced Analytics**: No implementation for tracking payment patterns
- **Internationalization**: No support for multiple languages or locales
- **Enhanced Security**: No fraud detection or transaction monitoring
- **Performance Optimization**: Limited performance tuning for high-volume scenarios

**Enhanced Security Features**: ❌ Not Started
- ❌ Advanced fraud detection
- ❌ Transaction monitoring
- ❌ Risk assessment
- ❌ Compliance reporting

---

## 12. Development Workflow

### 12.1 Code Organization

The project follows a structured code organization pattern:

**Multi-Module Structure**:
```
p2p-finance-app/
├── api-gateway/
├── config-service/
├── discovery-service/
├── user-service/
├── wallet-service/
├── payment-service/
├── notification-service/
├── integration-service/
└── common/
```

**Service Module Structure**:
```
service-module/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/p2pfinance/service/
│   │   │       ├── api/            # REST controllers
│   │   │       ├── config/         # Service configuration
│   │   │       ├── domain/         # Domain models
│   │   │       ├── dto/            # Data transfer objects
│   │   │       ├── repository/     # Data access layer
│   │   │       ├── service/        # Business logic
│   │   │       ├── client/         # Service clients
│   │   │       ├── exception/      # Exception handling
│   │   │       ├── event/          # Domain events
│   │   │       └── security/       # Security configuration
│   │   └── resources/
│   │       ├── application.yml     # Application configuration
│   │       └── db/migration/       # Flyway migrations
│   └── test/
│       └── java/                   # Test classes
└── pom.xml                         # Maven configuration
```

**Common Module Structure**:
```
common/
├── src/main/java/com/p2pfinance/common/
│   ├── dto/              # Shared DTOs
│   ├── event/            # Domain event definitions
│   ├── exception/        # Common exceptions
│   ├── util/             # Utility classes
│   └── validation/       # Validation helpers
└── pom.xml
```

This organization provides:
- Clear separation of concerns
- Consistent structure across services
- Modular development and testing
- Reuse of common components

### 12.2 Building and Deployment

The application uses Maven for building and Docker/Kubernetes for deployment:

**Maven Build**:
```xml

```

### 12.3 CI/CD Pipeline

The application uses GitHub Actions for continuous integration and deployment:

**CI/CD Workflow**:
```yaml
name: P2P Finance CI/CD

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      run: mvn -B clean package
    
    - name: Run tests
      run: mvn -B test
    
    - name: SonarQube analysis
      run: mvn sonar:sonar -Dsonar.projectKey=p2p-finance -Dsonar.host.url=${{ secrets.SONAR_URL }} -Dsonar.login=${{ secrets.SONAR_TOKEN }}
      
    - name: Build and push Docker images
      if: github.event_name != 'pull_request'
      run: |
        docker login -u ${{ secrets.DOCKER_USERNAME }} -p ${{ secrets.DOCKER_PASSWORD }}
        docker-compose build
        docker-compose push
    
    - name: Deploy to Kubernetes
      if: github.ref == 'refs/heads/main'
      run: |
        echo "${{ secrets.KUBE_CONFIG }}" > kubeconfig
        export KUBECONFIG=./kubeconfig
        kubectl apply -f kubernetes/
```

This workflow provides:
- Automated building and testing
- Code quality analysis with SonarQube
- Docker image creation and publishing
- Kubernetes deployment for production

---

## 13. Testing Strategy

### 13.1 Testing Types

The application implements a comprehensive testing approach:

**Unit Tests**:
- Test individual components in isolation
- Mock dependencies using Mockito
- Focus on business logic and edge cases
- Target service and repository classes

**Integration Tests**:
- Test interactions between components
- Validate database operations
- Test Kafka event production and consumption
- Use TestContainers for real database testing

**End-to-End Tests**:
- Test complete user flows across services
- Validate API contracts
- Ensure feature completeness
- Use REST Assured for API testing

**Performance Tests**:
- Load testing with JMeter
- Scalability validation
- Response time measurements
- Throughput analysis

### 13.2 Test Implementation

Examples of test implementations for different layers:

**Unit Test Example**:
```java
@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
    
    @Mock
    private WalletRepository walletRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @InjectMocks
    private WalletService walletService;
    
    @Test
    void transferBetweenWallets_SufficientBalance_Success() {
        // Arrange
        UUID sourceWalletId = UUID.randomUUID();
        UUID destWalletId = UUID.randomUUID();
        Wallet sourceWallet = new Wallet();
        sourceWallet.setId(sourceWalletId);
        sourceWallet.setBalance(new BigDecimal("100.00"));
        sourceWallet.setCurrency("USD");
        
        Wallet destWallet = new Wallet();
        destWallet.setId(destWalletId);
        destWallet.setBalance(new BigDecimal("50.00"));
        destWallet.setCurrency("USD");
        
        when(walletRepository.findById(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findById(destWalletId)).thenReturn(Optional.of(destWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        
        // Act
        TransferResult result = walletService.transferBetweenWallets(
            sourceWalletId, destWalletId, new BigDecimal("25.00"), "Test transfer");
        
        // Assert
        assertEquals(TransferStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("75.00"), sourceWallet.getBalance());
        assertEquals(new BigDecimal("75.00"), destWallet.getBalance());
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}
```

**Integration Test Example**:
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class WalletRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Test
    void findByUserIdAndCurrency_ReturnsCorrectWallet() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setCurrency("USD");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(wallet);
        
        // Act
        Optional<Wallet> result = walletRepository.findByUserIdAndCurrency(userId, "USD");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertEquals("USD", result.get().getCurrency());
    }
    
    @Test
    void saveTransaction_CreatesTransactionWithCorrectProperties() {
        // Arrange
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setUserId(UUID.randomUUID());
        wallet.setCurrency("USD");
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setStatus(WalletStatus.ACTIVE);
        walletRepository.save(wallet);
        
        Transaction transaction = new Transaction();
        transaction.setWalletId(walletId);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setCurrency("USD");
        transaction.setDescription("Test deposit");
        transaction.setStatus(TransactionStatus.COMPLETED);
        
        // Act
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Assert
        assertNotNull(savedTransaction.getId());
        assertEquals(walletId, savedTransaction.getWalletId());
        assertEquals(TransactionType.DEPOSIT, savedTransaction.getType());
        assertEquals(new BigDecimal("50.00"), savedTransaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
    }
}
```

**API Test Example**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WalletControllerTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private WalletRepository walletRepository;
    
    @BeforeEach
    void setup() {
        walletRepository.deleteAll();
    }
    
    @Test
    void createWallet_ValidRequest_ReturnsCreatedWallet() {
        // Arrange
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(UUID.randomUUID());
        request.setCurrency("USD");
        
        // Act
        ResponseEntity<WalletDto> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/v1/wallets",
            request,
            WalletDto.class
        );
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(request.getUserId(), response.getBody().getUserId());
        assertEquals(request.getCurrency(), response.getBody().getCurrency());
        assertEquals(BigDecimal.ZERO, response.getBody().getBalance());
        assertEquals(WalletStatus.ACTIVE.name(), response.getBody().getStatus());
    }
}
```

### 13.3 Notification Service Testing

The Notification Service requires specialized testing:

**Domain Model Tests**:
- Test notification creation and validation
- Verify notification preference rules
- Validate template rendering
- Test notification status transitions

**Service Layer Tests**:
- Test notification sending logic
- Verify channel selection based on preferences
- Test quiet hours functionality
- Validate template-based notification generation

**Event Handling Tests**:
- Verify correct handling of domain events
- Test event deserialization
- Validate notification generation from events
- Test error handling for malformed events

**Integration Tests**:
- Test database operations
- Verify Kafka event consumption
- Test external service integration (email, SMS, Firebase)
- Validate end-to-end notification flows

**Test Scenarios**:
- Notification creation with different templates
- Delivery to multiple channels
- Respecting user preferences and quiet hours
- Handling delivery failures
- Template rendering with different data models

### 13.4 Test Coverage Targets

The project aims for the following test coverage targets:

**Coverage Targets**:
- Unit Test Coverage: 80%+ for service classes
- Integration Test Coverage: All critical integration points
- E2E Coverage: Key user flows across services

The Notification Service aims for:

85%+ unit test coverage for service layer
90%+ test coverage for domain model
Key integration points tested with end-to-end tests
All event types covered by integration tests

**Test Execution**:
```bash
# Run all tests
mvn clean test

# Run specific test class
mvn test -Dtest=WalletServiceTest

# Run tests with specific pattern
mvn test -Dtest=*IntegrationTest

# Run tests with coverage report
mvn clean test jacoco:report
```

**Test Reports**:
- JUnit XML reports for CI/CD integration
- Jacoco coverage reports
- Surefire reports for detailed test results
- SonarQube integration for quality metrics

---

## 14. Monitoring and Observability

### 14.1 Logging Strategy

The application implements a comprehensive logging strategy:

**Logging Configuration**:
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdc>true</includeMdc>
            <includeContext>false</includeContext>
            <includeTags>true</includeTags>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
    
    <logger name="com.p2pfinance" level="DEBUG" />
</configuration>
```

**Structured Logging**:
```java
@Slf4j
@Service
public class TransactionService {
    
    public TransactionResult processTransaction(Transaction transaction) {
        MDC.put("transactionId", transaction.getId().toString());
        MDC.put("walletId", transaction.getWalletId().toString());
        MDC.put("transactionType", transaction.getType().toString());
        
        log.info("Processing transaction: amount={}, currency={}", 
            transaction.getAmount(), transaction.getCurrency());
        
        try {
            // Transaction processing logic
            log.debug("Validating transaction");
            // Validation logic
            
            log.debug("Updating wallet balance");
            // Balance update logic
            
            log.info("Transaction processed successfully");
            return new TransactionResult(TransactionStatus.COMPLETED, null);
        } catch (InsufficientBalanceException e) {
            log.warn("Insufficient balance for transaction: {}", e.getMessage());
            return new TransactionResult(TransactionStatus.FAILED, e.getMessage());
        } catch (Exception e) {
            log.error("Error processing transaction", e);
            return new TransactionResult(TransactionStatus.FAILED, "Internal error");
        } finally {
            MDC.clear();
        }
    }
}
```

**Distributed Tracing**:
```java
@Component
public class TracingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        String correlationId = httpRequest.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put("correlationId", correlationId);
        
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
```

### 14.2 Metrics Collection

The application collects metrics using Spring Boot Actuator and Micrometer:

**Metrics Configuration**:
```java
@Configuration
public class MetricsConfig {
    
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
            @Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }
    
    @Bean
    @ConditionalOnClass(name = "io.micrometer.prometheus.PrometheusMeterRegistry")
    @ConditionalOnBean(PrometheusMeterRegistry.class)
    PrometheusScrapeEndpoint prometheusEndpoint(PrometheusMeterRegistry registry) {
        return new PrometheusScrapeEndpoint(registry);
    }
    
    @Bean
    @ConditionalOnBean(PrometheusScrapeEndpoint.class)
    RouterFunction<ServerResponse> prometheusScrapingRoutes(PrometheusScrapeEndpoint endpoint) {
        return route(GET("/prometheus"), req -> ok().body(endpoint.scrape()));
    }
}
```

**Custom Metrics**:
```java
@Service
public class WalletService {
    
    private final Counter transactionCounter;
    private final DistributionSummary transactionAmounts;
    private final Timer transferTimer;
    
    public WalletService(MeterRegistry registry) {
        this.transactionCounter = Counter.builder("wallet.transactions")
            .description("Number of transactions processed")
            .register(registry);
        
        this.transactionAmounts = DistributionSummary.builder("wallet.transaction.amounts")
            .description("Distribution of transaction amounts")
            .register(registry);
        
        this.transferTimer = Timer.builder("wallet.transfer.duration")
            .description("Time taken to complete transfers")
            .register(registry);
    }
    
    public TransferResult transferBetweenWallets(UUID sourceWalletId, UUID destWalletId, 
            BigDecimal amount, String description) {
        return transferTimer.record(() -> {
            // Transfer logic
            
            transactionCounter.increment();
            transactionAmounts.record(amount.doubleValue());
            
            return new TransferResult(TransferStatus.SUCCESS, null);
        });
    }
}
```

**Prometheus Integration**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.75, 0.95, 0.99
      slo:
        http.server.requests: 50ms, 100ms, 200ms
  endpoint:
    health:
      show-details: always
```

### 14.3 Health Checks

The application provides comprehensive health checks:

**Health Check Configuration**:
```java
@Component
public class DatabaseHealthIndicator extends AbstractHealthIndicator {
    
    private final DataSource dataSource;
    
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT 1")) {
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                builder.up()
                    .withDetail("database", connection.getMetaData().getDatabaseProductName())
                    .withDetail("version", connection.getMetaData().getDatabaseProductVersion());
            } else {
                builder.down()
                    .withDetail("error", "Database query returned no results");
            }
        } catch (Exception e) {
            builder.down()
                .withDetail("error", e.getMessage());
        }
    }
}
```

**Kafka Health Check**:
```java
@Component
public class KafkaHealthIndicator extends AbstractHealthIndicator {
    
    private final KafkaAdmin kafkaAdmin;
    
    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }
    
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            Map<String, TopicDescription> topics = AdminClient
                .create(kafkaAdmin.getConfigurationProperties())
                .describeTopics(Collections.singletonList("heartbeat"))
                .all()
                .get(5, TimeUnit.SECONDS);
            
            builder.up()
                .withDetail("topics", topics.size())
                .withDetail("bootstrapServers", kafkaAdmin.getConfigurationProperties()
                    .get(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG));
        } catch (Exception e) {
            builder.down()
                .withDetail("error", e.getMessage());
        }
    }
}
```

**External System Health Check**:
```java
@Component
public class FineractHealthIndicator extends AbstractHealthIndicator {
    
    private final FineractConfig fineractConfig;
    private final WebClient webClient;
    
    public FineractHealthIndicator(FineractConfig fineractConfig, WebClient.Builder webClientBuilder) {
        this.fineractConfig = fineractConfig;
        this.webClient = webClientBuilder.baseUrl(fineractConfig.getBaseUrl()).build();
    }
    
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            String response = webClient.get()
                .uri("/actuator/health")
                .header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader())
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(5));
            
            builder.up()
                .withDetail("baseUrl", fineractConfig.getBaseUrl())
                .withDetail("response", response);
        } catch (Exception e) {
            builder.down()
                .withDetail("baseUrl", fineractConfig.getBaseUrl())
                .withDetail("error", e.getMessage());
        }
    }
    
    private String getBasicAuthHeader() {
        String auth = fineractConfig.getUsername() + ":" + fineractConfig.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }
}
```

### 14.4 Alerting System

The application integrates with alerting systems:

**Prometheus Alerting Rules**:
```yaml
```

---

## 15. Performance Considerations

### 15.1 Caching Strategy

The application implements a multi-level caching strategy:

**Cache Configuration**:
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(cacheConfiguration)
            .withCacheConfiguration("users", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)))
            .withCacheConfiguration("wallets", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
            .build();
    }
}
```

**Cache Implementation**:
```java
@Service
@CacheConfig(cacheNames = "wallets")
public class WalletService {
    
    private final WalletRepository walletRepository;
    
    @Cacheable(key = "#walletId")
    public Wallet getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));
    }
    
    @Cacheable(key = "'user:' + #userId + ':currency:' + #currency")
    public Wallet getWalletByUserIdAndCurrency(UUID userId, String currency) {
        return walletRepository.findByUserIdAndCurrency(userId, currency)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Wallet not found for user: " + userId + " and currency: " + currency));
    }
    
    @CacheEvict(key = "#wallet.id")
    public Wallet updateWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }
    
    @CacheEvict(allEntries = true)
    @Scheduled(fixedRate = 3600000) // Every hour
    public void clearWalletCache() {
        // Cache cleared automatically
    }
}
```

**Cache Considerations**:
- Caching frequently accessed, rarely changing data
- Using appropriate TTL (Time-To-Live) values
- Cache invalidation on data updates
- Periodic cache refresh for time-sensitive data
- Distributed caching with Redis for scalability

### 15.2 Database Optimization

The application implements various database optimizations:

**Indexing Strategy**:
```sql
-- User Service
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_verification_tokens_token ON verification_tokens(token);

-- Wallet Service
CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_user_id_currency ON wallets(user_id, currency);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(type);

-- Payment Service
CREATE INDEX idx_payment_requests_requester_id ON payment_requests(requester_id);
CREATE INDEX idx_payment_requests_recipient_id ON payment_requests(recipient_id);
CREATE INDEX idx_payment_requests_status ON payment_requests(status);
CREATE INDEX idx_scheduled_payments_sender_id ON scheduled_payments(sender_id);
CREATE INDEX idx_scheduled_payments_status ON scheduled_payments(status);
CREATE INDEX idx_split_payments_created_by_id ON split_payments(created_by_id);

-- Notification Service
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
```

**Query Optimization**:
```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletId(@Param("walletId") UUID walletId, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByWalletIdAndDateRange(
        @Param("walletId") UUID walletId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT t.* FROM transactions t " +
           "WHERE t.wallet_id = :walletId " +
           "AND t.status = 'COMPLETED' " +
           "ORDER BY t.created_at DESC LIMIT 10", nativeQuery = true)
    List<Transaction> findRecentTransactions(@Param("walletId") UUID walletId);
    
    @Query(value = "SELECT SUM(t.amount) FROM transactions t " +
           "WHERE t.wallet_id = :walletId " +
           "AND t.type = :type " +
           "AND t.status = 'COMPLETED' " +
           "AND t.created_at BETWEEN :startDate AND :endDate", nativeQuery = true)
    BigDecimal sumAmountByTypeAndDateRange(
        @Param("walletId") UUID walletId, 
        @Param("type") String type,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
}
```

**Database Connection Pooling**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/p2pfinance
    username: postgres
    password: password
    hikari:
      connection-timeout: 20000  # 20 seconds
      minimum-idle: 10
      maximum-pool-size: 20
      idle-timeout: 300000       # 5 minutes
      max-lifetime: 1200000      # 20 minutes
```

**Optimistic Locking**:
```java
@Entity
@Table(name = "wallets")
public class Wallet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    private UUID userId;
    private BigDecimal balance;
    private String currency;
    private String status;
    
    @Version
    private Integer version;
    
    // Getters, setters, etc.
}
```

**Batch Processing**:
```java
@Service
@Transactional
public class TransactionBatchService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Value("${batch.size:100}")
    private int batchSize;
    
    public void processTransactionBatch(List<Transaction> transactions) {
        for (int i = 0; i < transactions.size(); i++) {
            entityManager.persist(transactions.get(i));
            
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}
```

### 15.3 Scalability Approach

The application implements a scalable architecture:

**Horizontal Scaling**:
- Stateless services for easy replication
- Load balancing across service instances
- Session data stored in Redis for shared state
- Database connection pooling for efficient resource usage

**Asynchronous Processing**:
```java
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailSender emailSender;
    
    @Async
    public CompletableFuture<Void> sendNotificationAsync(Notification notification) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (notification.getType() == NotificationType.EMAIL) {
                    emailSender.sendEmail(notification.getUserId(), notification.getTitle(), notification.getMessage());
                }
                notification.setStatus(NotificationStatus.DELIVERED);
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setFailureReason(e.getMessage());
            }
            
            notificationRepository.save(notification);
        });
    }
}
```

**Data Partitioning**:
```java
@Configuration
public class PartitioningConfig {
    
    @Bean
    public PartitionKeyResolver transactionPartitionKeyResolver() {
        return new TransactionPartitionKeyResolver();
    }
    
    static class TransactionPartitionKeyResolver implements PartitionKeyResolver {
        @Override
        public String resolvePartitionKey(Object entity) {
            if (entity instanceof Transaction) {
                Transaction transaction = (Transaction) entity;
                
                // Using last 2 digits of year and month as partition key
                LocalDateTime dateTime = transaction.getCreatedAt();
                return String.format("%02d%02d", dateTime.getYear() % 100, dateTime.getMonthValue());
            }
            
            return null;
        }
    }
}
```

**Rate Limiting**:
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    @Autowired
    private RedisTemplate<String, Long> redisTemplate;
    
    @Value("${rate.limit.requests:100}")
    private int maxRequests;
    
    @Value("${rate.limit.duration:60}")
    private int durationSeconds;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        String clientId = getClientIdentifier(request);
        String key = "rate-limit:" + clientId;
        
        Long currentCount = redisTemplate.opsForValue().increment(key, 1);
        if (currentCount == 1) {
            redisTemplate.expire(key, durationSeconds, TimeUnit.SECONDS);
        }
        
        if (currentCount > maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        // Use user ID from JWT token if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        
        // Fall back to IP address for unauthenticated requests
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        
        return ipAddress;
    }
}
```

**Scaling Considerations**:
- Service discovery for dynamic scaling
- Circuit breaking for resilience during scaling events
- Monitoring and auto-scaling rules
- Infrastructure as Code for consistent environments
- Containerization for runtime consistency

---

## 16. Development Roadmap

### 16.1 Phase 1: Foundation (Completed)

**Environment Setup**:
- ✅ Docker Compose configuration
- ✅ PostgreSQL database configuration
- ✅ Kafka messaging infrastructure
- ✅ Redis caching setup
- ✅ Monitoring tools (Prometheus, Grafana)

**Project Structure**:
- ✅ Multi-module Maven project
- ✅ Parent POM with dependencies
- ✅ Service module organization
- ✅ Common module for shared code

**Integration Framework**:
- ✅ Fineract client configuration
- ✅ Cyclos client configuration
- ✅ Resilience patterns (circuit breakers, retries)
- ✅ DTOs for integration communication

### 16.2 Phase 2: Core Features (Substantial Progress)

**User Management**:
- ✅ User registration and authentication
- ✅ Profile management
- ✅ JWT-based security
- ✅ Email/token verification flow
- ✅ Password management

**Wallet System**:
- ✅ Wallet creation and management
- ✅ Transaction processing
- ✅ Balance tracking
- ✅ Transfer operations
- ✅ Transaction history

**Payment Capabilities**:
- ✅ Direct transfers between wallets
- ✅ Payment request creation and management
- ✅ Scheduled/recurring payments
- ✅ Split payment capabilities

**External Integrations**:
- ✅ Core integration with Fineract
- ✅ Core integration with Cyclos
- 🟨 Bank account connectivity (In progress)
- 🟨 Card processing (In progress)

**API Gateway**:
- 🟨 Routing configuration (In progress)
- 🟨 Authentication filter (In progress)
- 🟨 Rate limiting (In progress)
- ❌ Circuit breaking (Pending)

**Notification System**:
- 🟨 Event-driven architecture (In progress)
- 🟨 Multi-channel delivery (In progress)
- 🟨 User preferences (In progress)
- ❌ Template-based messages (Pending)

### 16.3 Phase 3: Advanced Features (Planned)

**Enhanced Security**:
- ❌ Two-factor authentication
- ❌ Advanced fraud detection
- ❌ Transaction monitoring
- ❌ Risk assessment

**Social Features**:
- ❌ Contact management
- ❌ Activity feed
- ❌ Transaction sharing
- ❌ Enhanced split expenses

**Mobile/Web UI**:
- ❌ User interface design
- ❌ Registration and login screens
- ❌ Wallet management interface
- ❌ Payment operations

**Business Intelligence**:
- ❌ Transaction reporting
- ❌ User analytics
- ❌ Financial dashboards
- ❌ Business insights

### 16.4 Phase 4: Scale & Security (Planned)

**Performance Optimization**:
- ❌ Caching enhancements
- ❌ Database query optimization
- ❌ Asynchronous processing improvements
- ❌ Load testing and bottleneck resolution

**Advanced Security**:
- ❌ Security audit and penetration testing
- ❌ Enhanced encryption
- ❌ Compliance certifications
- ❌ Advanced threat detection

**Scalability**:
- ❌ Database sharding
- ❌ Read replicas
- ❌ Horizontal scaling
- ❌ Auto-scaling configuration

**Disaster Recovery**:
- ❌ Backup and restore procedures
- ❌ High availability configuration
- ❌ Geographic redundancy
- ❌ Recovery testing

---

## 17. Implementation Timeline

### 17.1 Current Progress

The project has made significant progress on core functionality:

**Completed Milestones**:
- ✅ Phase 1: Foundation (100% complete)
- ✅ User Service (100% complete)
- ✅ Wallet Service (100% complete)
- ✅ Payment Service (100% complete)
- 🟨 Integration Service (75% complete)
- 🟨 API Gateway (50% complete)
- 🟨 Notification Service (40% complete)

**Current Development Focus**:
- API Gateway implementation
- Notification Service development
- External system integration enhancements
- Testing framework expansion

### 17.2 Next Milestones

The project will focus on completing these milestones in the coming weeks:

**Short-term Goals** (Next 2 Weeks):
- Complete API Gateway implementation
- Finish core Notification Service functionality
- Enhance integration test coverage
- Implement additional security features

**Medium-term Goals** (Next 4-6 Weeks):
- Develop basic web interface
- Enhance notification templates
- Complete external system integrations
- Set up comprehensive monitoring

**Long-term Goals** (Next 2-3 Months):
- Implement mobile application
- Add advanced security features
- Enhance business intelligence capabilities
- Prepare for production deployment

### 17.3 MVP Release Plan

The plan for Minimum Viable Product (MVP) release is as follows:

**MVP Core Features**:
- User registration and authentication
- Basic wallet management
- Simple payment operations
- Essential notifications
- Minimal web interface

**MVP Timeline**:
- API Gateway & Notifications: 2 weeks
- Basic Web Interface: 3 weeks
- Integration Testing & Deployment: 2 weeks
- MVP Release: 7 weeks from now

**Post-MVP Priorities**:
- Enhanced user experience
- Mobile application development
- Advanced payment features
- Social capabilities
- Business intelligence

---

## 18. Future Enhancements

### 18.1 Technical Enhancements

**Infrastructure**:
- Kubernetes service mesh with Istio
- Advanced auto-scaling rules
- Multi-region deployment
- Edge caching for APIs

**Observability**:
- Distributed tracing with OpenTelemetry
- Custom alerting dashboards
- Real-time system visualization
- Advanced log analysis

**Performance**:
- Real-time data processing with Kafka Streams
- Advanced caching strategies
- Read replicas for database scaling
- Query optimization and tuning

**Security**:
- Enhanced encryption for data at rest
- Advanced threat detection
- Security logging and auditing
- Compliance automation

### 18.2 Feature Enhancements

**User Experience**:
- Customizable user interface
- Enhanced notification preferences
- Personalized insights
- User behavior analytics

**Payment Capabilities**:
- International payments
- Multi-currency support
- Advanced recurring payment options
- Payment templates and favorites

**Financial Features**:
- Spending categorization
- Budget tracking
- Financial goals
- Merchant recognition

**Social Features**:
- Enhanced contact management
- Social payment requests
- Group expenses
- Activity sharing

### 18.3 Integration Enhancements

**Fineract Integration**:
- Additional financial products
- Advanced reporting
- Loan management
- Interest-bearing accounts

**Cyclos Integration**:
- Marketplace functionality
- Advanced payment workflows
- Business accounts
- Marketplace integration

**Third-party Integrations**:
- Card processing services
- Identity verification providers
- Credit scoring agencies
- Regulatory reporting systems

---

## 19. Setup & Installation Guide

### 19.1 Prerequisites

To set up the P2P Finance application, you need:

**Development Tools**:
- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- Git

**Infrastructure**:
- PostgreSQL 13+
- Redis 6+
- Kafka 2.8+
- Zookeeper

**External Services** (Optional for full functionality):
- Fineract instance
- Cyclos instance
- SMTP server for email notifications
- Firebase account for push notifications

### 19.2 Development Setup

**Clone the Repository**:
```bash
git clone https://github.com/yourusername/p2p-finance-app.git
cd p2p-finance-app
```

**Build the Project**:
```bash
./mvnw clean install -DskipTests
```

**Start Infrastructure with Docker Compose**:
```bash
docker-compose -f docker-compose-infra.yml up -d
```

**Run the Services**:

Option 1: Individual services for development:
```bash
cd user-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Option 2: All services with Docker Compose:
```bash
docker-compose up -d
```

**Access the Services**:
- API Gateway: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Eureka Dashboard: http://localhost:8761
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

### 19.3 Configuration

**Environment-Specific Configuration**:
Each service uses Spring profiles for environment-specific configuration:
- `dev`: Development environment (default)
- `test`: Testing environment
- `prod`: Production environment

**Configuration Files**:
- `application.yml`: Base configuration
- `application-{profile}.yml`: Profile-specific configuration

**Common Configuration Properties**:
```yaml
spring:
  application:
    name: service-name
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:p2pfinance}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: validate
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}

server:
  port: ${SERVER_PORT:8080}

eureka:
  client:
    serviceUrl:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka/}

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

**Security Configuration**:
Create a `security.properties` file in a secure location with:
```properties
jwt.secret=your-secret-key
jwt.expirationInMs=86400000
```

**External Services Configuration**:
Create a `services.properties` file with:
```properties
fineract.baseUrl=http://localhost:8443/fineract-provider/api/v1
fineract.username=mifos
fineract.password=password
fineract.tenantId=default

cyclos.baseUrl=http://localhost:8888/api
cyclos.username=admin
cyclos.password=password
```

---

## 20. Code Analysis and Recommendations

### 20.1 Current Status Summary

Based on comprehensive code review, the P2P Finance application has made significant progress:

**Achievements**:
- Complete microservices architecture with domain-driven design principles
- Rich domain models with encapsulated business logic and proper validation
- Comprehensive payment processing system with multiple payment types
- Robust security implementation with JWT authentication and proper authorization checks
- Event-driven architecture for asynchronous operations via Kafka
- Integration framework for external financial systems (Fineract, Cyclos)
- Proper error handling with specialized exceptions and global handlers
- Database schema management through Flyway migrations
- Resilience patterns with circuit breakers and retry mechanisms

**Remaining Work**:
- Completing the API Gateway implementation with comprehensive routing
- Finalizing the Notification Service for multi-channel delivery
- Developing user interfaces (web and mobile)
- Enhancing monitoring and observability infrastructure
- Implementing advanced security features for fraud detection
- Increasing test coverage especially for integration scenarios

The application has a solid foundation with core services (Payment, User, Wallet) well-implemented, positioning it well for MVP release after addressing the critical items.

### 20.2 Critical Path Items

To reach the MVP milestone, these critical path items should be prioritized:

**API Gateway Completion**:
- Finalize routing configuration for all service endpoints
- Complete authentication filter implementation
- Implement comprehensive rate limiting
- Enhance circuit breaking for downstream service failures
- Add request/response transformation for standardized formats

**Notification Service Development**:
- Complete event listeners for all domain events
- Implement delivery mechanisms for all channels
- Develop rendering engine for notification templates
- Implement user preference management system
- Add retry mechanisms for failed notifications

**Basic Web Interface**:
- Develop registration and login screens
- Implement wallet management interface
- Create payment operation workflows (request, schedule, split)
- Build notification center and preference screens
- Develop transaction history visualization

**Integration Testing Suite**:
- Develop end-to-end test scenarios for critical flows
- Implement integration tests for service-to-service communication
- Create performance tests for high-volume scenarios
- Build automated test suite for continuous verification

### 20.3 Refactoring Recommendations

Based on detailed code analysis, the following refactoring recommendations will improve maintainability, extensibility, and scalability:

**1. Domain Model Refinement**:
- Implement the Aggregate pattern more strictly to ensure transactional boundaries
- Extract common behavior into base domain classes for consistency
- Use more value objects for conceptual integrity (e.g., Money, Address, PhoneNumber)
- Make domain objects immutable where appropriate to prevent unintended state changes
- Add invariant validation methods to enforce business rules consistently

**2. Service Layer Improvements**:
- Reduce service class sizes through further decomposition (SRP principle)
- Extract utility methods to separate helper classes to improve readability
- Implement command/query separation (CQRS) for complex operations
- Use method references and functional interfaces for more declarative code
- Normalize error handling approaches across services for consistency

**3. API Design Enhancements**:
- Implement consistent pagination patterns across all collection endpoints
- Add hypermedia links (HATEOAS) for better API discoverability
- Standardize error response formats across all services
- Implement versioning headers in addition to path versioning
- Create comprehensive API documentation with usage examples

**4. Database Optimization**:
- Review and optimize indexes for frequently used queries
- Implement database partitioning strategy for historical data
- Use materialized views for frequently accessed, computation-heavy data
- Implement read replicas for scaling query operations
- Add query hints for complex operations

**5. Code Organization**:
- Establish consistent package structure across all services
- Group related functionality in modules with clear boundaries
- Extract common patterns into reusable components
- Improve naming conventions for better code readability
- Standardize code formatting and style across services

**6. Testing Improvements**:
- Increase unit test coverage, especially for domain logic
- Add more integration tests for service-to-service interactions
- Implement contract testing for service boundaries
- Add performance tests for critical operations
- Create comprehensive end-to-end test suites

**7. Security Enhancements**:
- Implement comprehensive input validation at API boundaries
- Add request throttling to prevent abuse
- Implement IP-based rate limiting
- Add advanced JWT validation with JWK rotation
- Implement audit logging for all security-sensitive operations

**8. Specific Service Recommendations**:

**For Payment Service**:
- Extract payment processing logic from `PaymentRequestService` to dedicated processor classes
- Implement specialized services for different payment types (one-time, recurring, split)
- Standardize transaction handling for consistent locking strategies
- Improve event publication with transactional outbox pattern
- Enhance metrics collection for performance monitoring

**For User Service**:
- Improve KYC verification workflow with multi-step validation
- Enhance OAuth2 integration with more providers
- Streamline token management for better security
- Add progressive profiling capabilities
- Implement user preferences management

**For Wallet Service**:
- Enhance concurrency handling for high-volume transactions
- Implement multiple wallet types with different characteristics
- Add support for more currencies and exchange operations
- Implement balance holds for pending transactions
- Add transaction categorization capabilities
  Implement entity-attribute-value pattern for storing custom wallet attributes
  Extract transaction execution logic into specialized service classes
  Add a distributed lock service for handling concurrent transactions across instances
  Implement a more sophisticated reconciliation process with external systems
  Extract transaction validation rules into a dedicated validator component
  Consider implementing CQRS pattern to separate read and write operations
  Enhance audit logging with user context information
  Add more domain events for granular status changes
  Implement idempotency for transaction processing
  Add more extensive metrics collection for transaction performance

**For API Gateway**:
- Implement response caching for frequently accessed data
- Add request validation before routing
- Enhance metrics collection for API usage analysis
- Implement circuit breaking with fallback responses
- Add request correlation for distributed tracing

**For Notification Service**:
- Implement priority-based notification delivery
- Add notification batching for efficiency
- Enhance delivery status tracking
- Implement notification preferences inheritance
- Add support for rich content in notifications

### 20.4 Architecture Extension Recommendations

For improved architecture that supports future growth:

**1. Event Sourcing Implementation**:
- Consider event sourcing for critical domains (transactions, payments)
- Implement event store for complete audit history
- Use CQRS pattern for separate read and write models
- Support event replay for system recovery
- Enable point-in-time reconstruction of state

**2. Scalability Enhancements**:
- Implement sharding strategy for high-volume data
- Add read replicas for query-heavy services
- Implement distributed caching with Redis
- Configure automatic scaling policies
- Optimize resource utilization

**3. Advanced Integration Patterns**:
- Implement the Saga pattern for distributed transactions
- Use the Outbox pattern for reliable event publishing
- Implement API composition for aggregated data
- Add circuit breakers with fallback responses
- Implement back-pressure handling for service protection

**4. DevOps Improvements**:
- Enhance CI/CD pipeline with extensive testing stages
- Implement infrastructure as code for all environments
- Add blue/green deployment capabilities
- Implement canary releases for new features
- Add comprehensive monitoring and alerting

**5. Feature Extensibility**:
- Implement feature toggles for controlled rollout
- Add plugin architecture for modular extensions
- Implement webhook system for external integrations
- Create developer API for third-party extensions
- Support configurable business rules

**For improving the Wallet Service architecture:**
1. Multi-Currency Support:

Enhance currency conversion service with historical rates
Implement exchange rate caching with time-based invalidation
Add support for cryptocurrency wallets
Implement multi-currency transactions with auto-conversion

2. Advanced Financial Operations:

Implement scheduled transfers and recurring payments
Add transaction bundling for better performance
Implement transaction rollback capabilities
Add two-phase commit for critical transactions
Support for payment holds and pre-authorization

3. Advanced Wallet Features:

Implement wallet sub-accounts for different purposes
Add virtual card integration for wallet spending
Implement spending limits and controls
Add support for joint accounts with multiple owners
Implement loyalty points and rewards system

4. Enhanced Security Features:

Add transaction verification for high-value transfers
Implement velocity checking for fraud detection
Add multi-signature approvals for enterprise wallets
Implement geographic restrictions for transactions
Add spending pattern analysis for anomaly detection

5. Resilience Enhancements:

Implement transaction outbox pattern for guaranteed processing
Add circuit breaking at more granular levels
Enhance fallback strategies with partial functionality
Implement read replicas for high-volume query operations
Add distributed caching for wallet balances

### 20.5 Strategic Technical Recommendations

For long-term technical excellence:

**1. API-First Development**:
- Document APIs before implementation using OpenAPI
- Generate client libraries from API specifications
- Implement contract testing to verify API compliance
- Provide comprehensive API documentation
- Maintain backward compatibility through versioning

**2. Comprehensive Test Automation**:
- Achieve high test coverage across all layers
- Implement automated performance testing
- Add security testing to CI/CD pipeline
- Use property-based testing for edge cases
- Implement regression test suites

**3. Security Excellence**:
- Conduct regular security reviews and penetration testing
- Implement fraud detection and prevention systems
- Use real-time transaction monitoring
- Implement advanced authentication methods
- Maintain regulatory compliance controls

**4. Performance Optimization**:
- Establish performance baselines for all services
- Implement continuous performance testing
- Optimize database access patterns
- Enhance caching strategies
- Use profiling to identify bottlenecks

**5. Technical Debt Management**:
- Schedule regular refactoring sprints
- Maintain up-to-date dependencies
- Document technical decisions and trade-offs
- Track and prioritize technical debt
- Implement code quality gates

Waqiti shows great promise with its solid foundation. By implementing these recommendations systematically, the project can achieve high levels of maintainability, extensibility, and scalability while delivering an excellent user experience.wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_