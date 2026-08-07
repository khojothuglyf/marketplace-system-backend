# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl && \
    addgroup -S appuser && adduser -S -G appuser appuser && \
    mkdir -p /app/logs && chown -R appuser:appuser /app/logs
USER appuser
WORKDIR /app
COPY --from=build /workspace/target/marketplace-system.jar /app/marketplace-system.jar

ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    LOG_PATH=/app/logs

EXPOSE 8080

# Graceful shutdown on SIGTERM (docker stop), container-friendly heap sizing.
# Render injects the dynamic port via the PORT env var; bind to it and fall
# back to SERVER_PORT (8080) for local runs and docker-compose.
ENTRYPOINT ["/bin/sh", "-c", "exec java -XX:MaxRAMPercentage=75 -jar /app/marketplace-system.jar --server.port=${PORT:-${SERVER_PORT:-8080}}"]
