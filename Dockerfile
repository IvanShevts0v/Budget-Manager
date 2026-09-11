FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY checkstyle.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/budgetmanager-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]
