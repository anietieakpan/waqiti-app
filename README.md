# P2P Finance Application

A modern peer-to-peer payment and e-wallet application built using microservices architecture. Think of it as your own version of CashApp or Venmo.

## 🚀 Features

- **User Management**: Registration, authentication, profiles, and KYC verification
- **Wallet Management**: Create wallets, check balances, and track transactions
- **Payment Processing**: Direct transfers, payment requests, scheduled payments, and split payments
- **Notifications**: In-app, email, SMS, and push notifications
- **Integration**: Connect with external payment systems like Fineract and Cyclos

## 🏗️ Architecture

The application is built using a microservices architecture with the following components:

- **API Gateway**: Entry point for all client requests
- **User Service**: Manages user accounts and profiles
- **Wallet Service**: Handles wallet operations and balances
- **Payment Service**: Processes various types of payments
- **Notification Service**: Sends notifications across multiple channels
- **Integration Service**: Connects with external payment systems

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot, Spring Cloud
- **Database**: PostgreSQL
- **Cache**: Redis
- **Messaging**: Kafka
- **Security**: JWT, Spring Security
- **Documentation**: OpenAPI/Swagger
- **Containerization**: Docker, Kubernetes
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus, Grafana
- **External Systems**: Apache Fineract, Cyclos

## 🔨 Setup & Installation

### Prerequisites

- Java 17 or higher
- Maven
- Docker and Docker Compose
- Kubernetes (for production deployment)

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/p2p-finance-app.git
   cd p2p-finance-app
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Start the application using Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. Access the services:
    - API Gateway: http://localhost:8080
    - Swagger UI: http://localhost:8080/swagger-ui.html
    - Grafana: http://localhost:3000
    - Prometheus: http://localhost:9090

### Configuration

Each service has its own `application.yml` file for configuration. Environment-specific configurations can be provided using Spring profiles.

## 📚 Documentation

API documentation is available through Swagger UI at http://localhost:8080/swagger-ui.html when the application is running.

## 🧪 Testing

Run the tests using Maven:

```bash
./mvnw test
```

## 🚢 Deployment

### Docker Deployment

The application can be deployed using Docker Compose:

```bash
docker-compose up -d
```

### Kubernetes Deployment

Kubernetes manifests are available in the `kubernetes` directory. Deploy using:

```bash
kubectl apply -f kubernetes/
```

## 🔄 Continuous Integration

The project uses GitHub Actions for CI/CD. The pipeline includes:

1. Building and testing the application
2. Static code analysis with SonarQube
3. Building Docker images
4. Pushing images to Docker Hub
5. Deploying to Kubernetes

## 📈 Monitoring

Prometheus and Grafana are included for monitoring the application. Dashboards are available at http://localhost:3000 when running locally.

## 🧩 External Integrations

The application integrates with:

- **Apache Fineract**: An open-source core banking system
- **Cyclos**: A payment platform for communities and complementary currency systems

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the [MIT License](LICENSE).