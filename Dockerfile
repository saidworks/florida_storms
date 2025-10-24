# Use the official Gradle image to build the application
FROM gradle:9.1.0-jdk25-alpine AS builder

# Set the working directory
WORKDIR /app

# Copy the Gradle files first (for caching dependencies)
COPY buildScript ./buildScript
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

# Build the application
RUN gradle spotlessApply --no-daemon
RUN gradle clean build --no-daemon

# Use the official OpenJDK as a base image for the final deployment
FROM openjdk:25-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built application from the builder image
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 1234

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]