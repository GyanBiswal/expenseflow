# Stage 1 — Build
# Use a full JDK image to compile and package the app
FROM eclipse-temurin:21-jdk-alpine AS build

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and pom.xml first
# Docker caches layers — copying pom.xml separately means dependencies
# are only re-downloaded when pom.xml changes, not on every code change
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build the jar
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2 — Run
# Use a smaller JRE-only image for the final container
# This keeps the image size small — no compiler needed at runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]