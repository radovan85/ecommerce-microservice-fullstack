FROM eclipse-temurin:17-jdk-alpine

# Install curl (required for health check)
RUN apk add --no-cache curl

WORKDIR /app
COPY target/eureka-server-app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761

# Run with shorter startup timeout (helps with health checks)
ENTRYPOINT ["java", "-jar", "-Dspring.main.lazy-initialization=true", "app.jar"]