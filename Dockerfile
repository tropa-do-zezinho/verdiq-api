# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src src
RUN ./mvnw -q -B -DskipTests package \
 && cp target/na-epoca-api-*.jar /app/app.jar

FROM eclipse-temurin:25-jre-noble AS runtime
WORKDIR /app

RUN groupadd -r verdiq && useradd -r -g verdiq verdiq
USER verdiq

COPY --from=build /app/app.jar /app/app.jar

# Render injeta PORT (default 10000); bind fica em server.address=0.0.0.0
ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
