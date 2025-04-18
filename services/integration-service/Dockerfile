FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copy maven executables and configuration
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY integration-service/pom.xml ./integration-service/
COPY common/pom.xml ./common/

# Make the maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY common/src ./common/src
COPY integration-service/src ./integration-service/src

# Build the application
RUN ./mvnw clean package -pl integration-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Add application user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built JAR from the build stage
COPY --from=build /workspace/app/integration-service/target/*.jar app.jar

# Set health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD wget -q --spider http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]