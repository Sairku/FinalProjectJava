# First stage: build
# Use a smaller JDK image
FROM openjdk:21-jdk-slim AS builder

# Set working directory
WORKDIR /app

COPY pom.xml .

# Copy source code
COPY src/ src/

# Copy Maven wrapper and config
COPY .mvn/ .mvn
COPY mvnw mvnw

# Ensure Maven wrapper is executable
RUN chmod +x mvnw

# Pre-fetch dependencies (optional, speeds up rebuilds)
RUN ./mvnw dependency:go-offline -B

# Build the Spring Boot JAR
RUN ./mvnw clean package -DskipTests

# Second stage: run
# Use a smaller JDK image
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the Spring Boot app
CMD ["java", "-jar", "app.jar", "--server.address=0.0.0.0", "--server.port=8080", "--spring.profiles.active=prod"]
