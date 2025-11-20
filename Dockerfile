# --------- Build-Stage ---------
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

# Maven Wrapper + Konfiguration kopieren
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Dependencies vorab laden
RUN ./mvnw dependency:go-offline

# Source-Code kopieren und bauen
COPY src src
RUN ./mvnw package -DskipTests

# --------- Runtime-Stage ---------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Jar aus der Build-Stage kopieren
COPY --from=build /workspace/target/*.jar app.jar

# Doku-Port für Render – üblicherweise 10000
EXPOSE 10000
#EXPOSE 8080

# Wichtig:
# - Render setzt $PORT (z.B. 10000)
# - Wir reichen diesen Wert an Spring Boot weiter (--server.port=...)
# - Fallback: 10000, falls PORT nicht gesetzt ist (z.B. beim lokalen Test)
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT:-10000}"]
#ENTRYPOINT ["java","-jar","/app/app.jar"]

