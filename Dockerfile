# --------- Build-Stage: baut das jar im Container ---------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Maven Wrapper + Konfiguration kopieren
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Dependencies vorab laden (wird gecacht, solange sich die pom.xml nicht ändert)
# chmod: falls mvnw beim Auschecken (z. B. unter Windows) nicht ausführbar ist
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Source-Code kopieren und bauen
COPY src src
RUN ./mvnw -B package -DskipTests

# --------- Runtime-Stage: nur Java-Laufzeit + jar ---------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Jar aus der Build-Stage kopieren
COPY --from=build /workspace/target/*.jar app.jar

# Spring Boot lauscht standardmäßig auf Port 8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]