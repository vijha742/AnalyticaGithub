# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the POM file first to leverage Docker cache
COPY pom.xml .
# Download dependencies (separate layer for better caching)
RUN mvn dependency:go-offline -B

# Copy source code and build the application

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user to run the application
RUN addgroup --system javauser && adduser --system --ingroup javauser javauser
USER javauser

# Copy the built JAR from the build stage
# Assuming the JAR name follows the standard Spring Boot naming convention
COPY --from=build /app/target/*.jar app.jar

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose the port your application runs on (typically 8080 for Spring Boot)
EXPOSE 8080

# Run the application with optimized JVM settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
