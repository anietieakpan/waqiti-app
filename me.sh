#!/bin/bash
#
# Script to create the new Waqiti project structure
# This will create the directory skeleton directly in the current directory
# It checks for existing directories and only creates missing ones
#

# Set the root directory to the current directory
ROOT_DIR="."
CREATED_COUNT=0
SKIPPED_COUNT=0

echo "Creating new project structure for Waqiti in the current directory..."
echo "Only creating directories that don't already exist..."

# Function to create directory only if it doesn't exist
create_dir() {
    if [ ! -d "$1" ]; then
        mkdir -p "$1"
        CREATED_COUNT=$((CREATED_COUNT+1))
        return 0  # Directory was created
    else
        SKIPPED_COUNT=$((SKIPPED_COUNT+1))
        return 1  # Directory already existed
    fi
}

# Function to create file only if it doesn't exist
create_file() {
    if [ ! -f "$1" ]; then
        touch "$1"
        return 0  # File was created
    else
        return 1  # File already existed
    fi
}

# Create top-level directories
for DIR in .github config infrastructure services frontend common docs scripts; do
    create_dir "$ROOT_DIR/$DIR" && echo "Created top-level directory: $DIR"
done

# Create GitHub workflows directory
create_dir "$ROOT_DIR/.github/workflows"

# Create configuration directories
for DIR in secrets vault/policies templates; do
    create_dir "$ROOT_DIR/config/$DIR" && echo "Created config directory: config/$DIR"
done

# Create infrastructure directories
INFRA_DIRS=(
    "kubernetes/base/namespaces"
    "kubernetes/base/services"
    "kubernetes/base/deployments"
    "kubernetes/base/config"
    "kubernetes/base/ingress"
    "kubernetes/base/security"
    "kubernetes/operators/p2p-wallet-operator"
    "kubernetes/operators/p2p-monitoring-operator"
    "kubernetes/monitoring/prometheus"
    "kubernetes/monitoring/grafana/dashboards"
    "kubernetes/monitoring/grafana/datasources"
    "kubernetes/monitoring/alertmanager"
    "kubernetes/scaling/hpa"
    "kubernetes/scaling/vpa"
    "kubernetes/overlays/dev"
    "kubernetes/overlays/staging"
    "kubernetes/overlays/production"
    "terraform/modules/database"
    "terraform/modules/messaging"
    "terraform/modules/cache"
    "terraform/modules/networking"
    "terraform/environments/dev"
    "terraform/environments/staging"
    "terraform/environments/production"
    "docker/base/java"
    "docker/base/node"
    "docker/base/nginx"
    "docker/compose"
    "disaster-recovery/backup"
    "disaster-recovery/restore"
    "disaster-recovery/testing"
)

for DIR in "${INFRA_DIRS[@]}"; do
    create_dir "$ROOT_DIR/infrastructure/$DIR" && echo "Created infrastructure directory: infrastructure/$DIR"
done

# Create service directories
SERVICES=(
    "api-gateway"
    "user-service"
    "wallet-service"
    "payment-service"
    "notification-service"
    "integration-service"
    "security-service"
    "analytics-service"
    "discovery-service"
    "config-service"
)

for SERVICE in "${SERVICES[@]}"; do
    if create_dir "$ROOT_DIR/services/$SERVICE"; then
        echo "Created service directory: services/$SERVICE"
    
        # Create standard Java service structure
        SERVICE_DIRS=(
            "src/main/java/com/waqiti/$SERVICE/domain/model"
            "src/main/java/com/waqiti/$SERVICE/domain/valueobject"
            "src/main/java/com/waqiti/$SERVICE/domain/aggregate"
            "src/main/java/com/waqiti/$SERVICE/domain/event"
            "src/main/java/com/waqiti/$SERVICE/domain/service"
            "src/main/java/com/waqiti/$SERVICE/application/command"
            "src/main/java/com/waqiti/$SERVICE/application/query"
            "src/main/java/com/waqiti/$SERVICE/application/service"
            "src/main/java/com/waqiti/$SERVICE/application/port/input"
            "src/main/java/com/waqiti/$SERVICE/application/port/output"
            "src/main/java/com/waqiti/$SERVICE/application/dto"
            "src/main/java/com/waqiti/$SERVICE/infrastructure/persistence/entity"
            "src/main/java/com/waqiti/$SERVICE/infrastructure/persistence/repository"
            "src/main/java/com/waqiti/$SERVICE/infrastructure/persistence/mapper"
            "src/main/java/com/waqiti/$SERVICE/infrastructure/messaging"
            "src/main/java/com/waqiti/$SERVICE/infrastructure/client"
            "src/main/java/com/waqiti/$SERVICE/infrastructure/security"
            "src/main/java/com/waqiti/$SERVICE/presentation/api"
            "src/main/java/com/waqiti/$SERVICE/presentation/dto"
            "src/main/java/com/waqiti/$SERVICE/presentation/mapper"
            "src/main/resources/db/migration"
            "src/test/java/com/waqiti/$SERVICE/unit/domain"
            "src/test/java/com/waqiti/$SERVICE/unit/service"
            "src/test/java/com/waqiti/$SERVICE/integration/repository"
            "src/test/java/com/waqiti/$SERVICE/integration/api"
            "src/test/java/com/waqiti/$SERVICE/integration/client"
            "src/test/java/com/waqiti/$SERVICE/contract"
            "src/test/java/com/waqiti/$SERVICE/performance"
            "src/test/java/com/waqiti/$SERVICE/e2e"
            "src/test/resources/contracts"
            "src/test/resources/test-data"
        )
        
        for DIR in "${SERVICE_DIRS[@]}"; do
            create_dir "$ROOT_DIR/services/$SERVICE/$DIR"
        done
        
        # Add special directories for selected services
        case $SERVICE in
            notification-service)
                SPECIAL_DIRS=(
                    "src/main/java/com/waqiti/$SERVICE/batch/config"
                    "src/main/java/com/waqiti/$SERVICE/batch/processor"
                    "src/main/java/com/waqiti/$SERVICE/batch/reader"
                    "src/main/java/com/waqiti/$SERVICE/batch/writer"
                    "src/main/java/com/waqiti/$SERVICE/analytics/service"
                    "src/main/java/com/waqiti/$SERVICE/analytics/model"
                    "src/main/java/com/waqiti/$SERVICE/analytics/repository"
                )
                ;;
            security-service)
                SPECIAL_DIRS=(
                    "src/main/java/com/waqiti/$SERVICE/mfa/api"
                    "src/main/java/com/waqiti/$SERVICE/mfa/service"
                    "src/main/java/com/waqiti/$SERVICE/mfa/provider"
                    "src/main/java/com/waqiti/$SERVICE/fraud/detection"
                    "src/main/java/com/waqiti/$SERVICE/fraud/rules"
                    "src/main/java/com/waqiti/$SERVICE/fraud/alerts"
                    "src/main/java/com/waqiti/$SERVICE/behavior/tracking"
                    "src/main/java/com/waqiti/$SERVICE/behavior/analysis"
                    "src/main/java/com/waqiti/$SERVICE/behavior/reporting"
                    "src/main/java/com/waqiti/$SERVICE/compliance/kyc"
                    "src/main/java/com/waqiti/$SERVICE/compliance/aml"
                    "src/main/java/com/waqiti/$SERVICE/compliance/reporting"
                )
                ;;
            analytics-service)
                SPECIAL_DIRS=(
                    "src/main/java/com/waqiti/$SERVICE/etl/extractor"
                    "src/main/java/com/waqiti/$SERVICE/etl/transformer"
                    "src/main/java/com/waqiti/$SERVICE/etl/loader"
                    "src/main/java/com/waqiti/$SERVICE/dashboard/model"
                    "src/main/java/com/waqiti/$SERVICE/dashboard/service"
                    "src/main/java/com/waqiti/$SERVICE/dashboard/controller"
                    "src/main/java/com/waqiti/$SERVICE/ml/model"
                    "src/main/java/com/waqiti/$SERVICE/ml/training"
                    "src/main/java/com/waqiti/$SERVICE/ml/prediction"
                    "src/main/java/com/waqiti/$SERVICE/reporting/generator"
                    "src/main/java/com/waqiti/$SERVICE/reporting/scheduler"
                    "src/main/java/com/waqiti/$SERVICE/reporting/distributor"
                )
                ;;
            *)
                SPECIAL_DIRS=()
                ;;
        esac
        
        for DIR in "${SPECIAL_DIRS[@]}"; do
            create_dir "$ROOT_DIR/services/$SERVICE/$DIR"
        done
        
        # Create basic service files
        create_file "$ROOT_DIR/services/$SERVICE/Dockerfile"
        create_file "$ROOT_DIR/services/$SERVICE/pom.xml"
    fi
done

# Create frontend directories
FRONTEND_DIRS=(
    "web-app/public"
    "web-app/src/components/common"
    "web-app/src/components/auth"
    "web-app/src/components/wallet"
    "web-app/src/components/payment"
    "web-app/src/components/notification"
    "web-app/src/pages/auth"
    "web-app/src/pages/dashboard"
    "web-app/src/pages/wallet"
    "web-app/src/pages/payment"
    "web-app/src/pages/settings"
    "web-app/src/services"
    "web-app/src/store/auth"
    "web-app/src/store/wallet"
    "web-app/src/utils"
    "web-app/src/hooks"
    "web-app/src/theme"
    "mobile-app/android"
    "mobile-app/ios"
    "mobile-app/src/components"
    "mobile-app/src/screens"
    "mobile-app/src/services"
    "admin-dashboard/src/components"
    "admin-dashboard/src/pages/user-management"
    "admin-dashboard/src/pages/monitoring"
    "admin-dashboard/src/pages/transactions"
    "admin-dashboard/src/pages/configuration"
    "admin-dashboard/src/pages/analytics"
)

for DIR in "${FRONTEND_DIRS[@]}"; do
    create_dir "$ROOT_DIR/frontend/$DIR" && echo "Created frontend directory: frontend/$DIR"
done

# Create frontend application files
for APP in web-app mobile-app admin-dashboard; do
    create_file "$ROOT_DIR/frontend/$APP/.env.development"
    create_file "$ROOT_DIR/frontend/$APP/.env.production"
    create_file "$ROOT_DIR/frontend/$APP/package.json"
    create_file "$ROOT_DIR/frontend/$APP/README.md"
done

# Create common module directories
COMMON_DIRS=(
    "domain/src/main/java/com/waqiti/common/domain/event"
    "domain/src/main/java/com/waqiti/common/domain/model"
    "domain/src/main/java/com/waqiti/common/domain/valueobject"
    "security/src/main/java/com/waqiti/common/security/jwt"
    "security/src/main/java/com/waqiti/common/security/encryption"
    "security/src/main/java/com/waqiti/common/security/audit"
    "util/src/main/java/com/waqiti/common/util/validation"
    "util/src/main/java/com/waqiti/common/util/format"
    "util/src/main/java/com/waqiti/common/util/collection"
    "test/src/main/java/com/waqiti/common/test/fixture"
    "test/src/main/java/com/waqiti/common/test/assertion"
    "test/src/main/java/com/waqiti/common/test/container"
)

for DIR in "${COMMON_DIRS[@]}"; do
    create_dir "$ROOT_DIR/common/$DIR" && echo "Created common directory: common/$DIR"
done

# Create module pom files
for MODULE in domain security util test; do
    create_file "$ROOT_DIR/common/$MODULE/pom.xml"
done

# Create documentation directories
DOC_DIRS=(
    "architecture/diagrams"
    "architecture/decisions"
    "architecture/principles"
    "api/openapi"
    "api/postman"
    "development"
    "operations"
)

for DIR in "${DOC_DIRS[@]}"; do
    create_dir "$ROOT_DIR/docs/$DIR" && echo "Created docs directory: docs/$DIR"
done

# Create script directories
SCRIPT_DIRS=(
    "setup"
    "ci"
    "maintenance"
)

for DIR in "${SCRIPT_DIRS[@]}"; do
    create_dir "$ROOT_DIR/scripts/$DIR" && echo "Created scripts directory: scripts/$DIR"
done

# Create root project files
create_file "$ROOT_DIR/.gitignore"
create_file "$ROOT_DIR/docker-compose.yml"
create_file "$ROOT_DIR/Makefile"
create_file "$ROOT_DIR/pom.xml"

# Create example environment file if it doesn't exist
ENV_EXAMPLE="$ROOT_DIR/config/secrets/.env.example"
if [ ! -f "$ENV_EXAMPLE" ]; then
    cat > "$ENV_EXAMPLE" << EOF
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=waqiti
DB_USERNAME=admin
DB_PASSWORD=replace_with_secure_password

# JWT Security
JWT_SECRET=replace_with_secure_jwt_secret
JWT_EXPIRATION_MS=86400000

# External Service Integration
FINERACT_URL=https://fineract.example.com/api/v1
FINERACT_USERNAME=api_user
FINERACT_PASSWORD=replace_with_secure_password

CYCLOS_URL=https://cyclos.example.com/api
CYCLOS_API_KEY=replace_with_secure_api_key

# Email Configuration
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=email_user
SMTP_PASSWORD=replace_with_secure_password
EMAIL_FROM=no-reply@waqiti.com

# Push Notification
FIREBASE_CONFIG_FILE=/path/to/firebase-credentials.json
EOF
    echo "Created environment example file"
else
    echo "Environment example file already exists, skipping"
fi

# Create README if it doesn't exist
README="$ROOT_DIR/README.md"
if [ ! -f "$README" ]; then
    cat > "$README" << EOF
# Waqiti

## Project Structure

This project follows a microservices architecture with a domain-driven design approach.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Docker and Docker Compose
- Git

### Development Setup

1. Clone the repository
\`\`\`bash
git clone https://github.com/yourusername/waqiti.git
cd waqiti
\`\`\`

2. Set up environment variables
\`\`\`bash
cp config/secrets/.env.example config/secrets/.env
# Edit .env file with your configuration
\`\`\`

3. Build the project
\`\`\`bash
./mvnw clean install -DskipTests
\`\`\`

4. Start the infrastructure
\`\`\`bash
docker-compose -f infrastructure/docker/compose/development.yml up -d
\`\`\`

5. Run the services
\`\`\`bash
# Option 1: Run individual services
cd services/user-service
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Option 2: Run all services
docker-compose up -d
\`\`\`

## Project Structure Overview

- \`/.github\`: GitHub Actions workflows
- \`/config\`: Configuration management
- \`/infrastructure\`: Infrastructure as Code
- \`/services\`: Microservice implementations
- \`/frontend\`: Frontend applications
- \`/common\`: Shared libraries and utilities
- \`/docs\`: Documentation
- \`/scripts\`: Utility scripts

## Service Architecture

Each service follows the hexagonal architecture pattern:

- \`domain\`: Core domain model and business logic
- \`application\`: Application services and use cases
- \`infrastructure\`: Technical implementations and adapters
- \`presentation\`: API controllers and DTOs

## Contributing

Please refer to [docs/development/contribution.md](docs/development/contribution.md) for contribution guidelines.
EOF
    echo "Created README file"
else
    echo "README file already exists, skipping"
fi

echo "-----------------------------------------"
echo "Project structure creation summary:"
echo "Created $CREATED_COUNT new directories/files"
echo "Skipped $SKIPPED_COUNT existing directories/files"
echo "-----------------------------------------"
echo "You can now start migrating your existing code into this new structure"
