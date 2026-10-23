# ===== Stage 1: build =====
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first, download deps — this layer caches unless pom.xml changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source, build the fat JAR
COPY src ./src
RUN mvn -B package -DskipTests

# ===== Stage 2: runtime =====
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR
COPY --from=build /app/target/java-redis-clone-1.0.0.jar ./jredis.jar

# Expose the default Redis port (informational)
EXPOSE 6379

# The RDB file will live here; mount a volume to persist it
VOLUME ["/app"]

# Run the server. Uses injected $PORT if available (for Render/Fly.io), else 6379.
ENTRYPOINT ["sh", "-c", "java -jar jredis.jar --port=${PORT:-6379}"]