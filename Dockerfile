# Build stage
FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

COPY . .

RUN ./gradlew clean build

# Runtime stage
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]