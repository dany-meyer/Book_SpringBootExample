# Spring Boot Beispiel #

## Structure ##
Das Beispiel demonstriert ein einfaches Beispiel in den Schichten
- Controller
- Repository
- Beans

## Slice-Tests ## 
- Controller-Schicht: BookControllerTest (mit Mock-Objekten, testet die Methoden) sowie BookControllerWebTest (via @WebMvcTest testet auch die Schnittstelle)
- Repository-Schicht: BookRepositoryTest (mit @DataJpaTest, verwendet in-memory DB)
- Test, ob SpringBoot-Applikation korrekt startet: DemoApplicationTests (@SpringBootTest, prüft context)
- Ganze Anwendung über HTTP: BookApiIT (@SpringBootTest mit echtem Server und Datenbank – Spring nennt das Integrationstest, nach ISTQB ein Systemtest des Backends)
- Ausführlich erklärt: Handout_SliceTests.md

## Start der Applikation aus IntelliJ
- über: Start --> DemoApplication
- Requests erreichbar: z.B. GET von http://localhost:8080/books

## Static WebSite with post/get-Examples ##
Web-Site mit einfacher Integration von post und get-Requests
- abgelegt unter src/main/resources/static
- Beispiel kann als Start für weitere Anwendungen gesehen werden z.B. in Vue.js
- Test-Web-Site erreichbar: http://localhost:8080/books.html (wenn Spring-Boot-App läuft)

## Swagger-Doc ##
- wird aktiviert durch Eintragen des entsprechenden Eintrags in den dependencies (siehe pom.xml)
- erreichbar: http://localhost:8080/swagger-ui/index.html

## in-memory Datenbank ## 
- es ist eine H2-in-Memory-DB konfiguriert (siehe src/main/resources/application.properties)
- ist bei jedem Neustart geleert
- web-Konsole erreichbar über: http://localhost:8080/h2-console

## Docker

### Voraussetzungen
- Docker installieren, z.B. Docker Desktop: https://www.docker.com
- Test im Terminal: `docker --version`
- Docker starten (Docker Desktop ist eine eigene Anwendung); prüfen, ob Docker läuft: `docker info`
- Das `Dockerfile` liegt auf oberster Ebene des Projektes, auf gleicher Ebene wie die `pom.xml`. Es enthält die Anweisungen zum Erstellen des Docker-Image.
- Das Dockerfile baut die Anwendung selbst (Multi-Stage-Build): In der ersten Stufe erzeugt Maven im Container das jar, die zweite Stufe enthält nur die Java-Laufzeit und das jar. Ein vorheriges `./mvnw package` ist deshalb nicht nötig.

### Schritt 1: Docker-Image erzeugen
- im (IntelliJ-)Terminal im Projektordner: `docker build -t my-spring-app:latest .`
  - `-t my-spring-app:latest` → gibt dem Image einen Namen und einen Tag: `my-spring-app` ist der Name, `latest` (nach dem Doppelpunkt) die Versionsbezeichnung, z.B. auch `1.0`; 
  - ohne Angabe nimmt Docker `latest`
  - ohne `-t` hat das Image nur eine ID (z.B. `3f2a9c1b7e…`), die man bei `docker run` angeben müsste
  - `.` am Ende → der aktuelle Ordner ist der Build-Kontext (hier liegen Dockerfile und pom.xml)
- der erste Build dauert einige Minuten, weil Maven alle Abhängigkeiten lädt; danach geht es schneller
- prüfen, ob das Image vorhanden ist: `docker images`
- Hinweis für Macs mit Apple-Chip (M1–M4): Das Image wird für ARM gebaut. Soll es später auf einem Linux-Server mit Intel/AMD-Prozessor laufen, mit `docker build --platform linux/amd64 -t my-spring-app:latest .` bauen.

### Schritt 2: Docker-Container starten und testen
- Container (aus dem Image wird ein lauffähiger Prozess erzeugt) starten: `docker run -p 8080:8080 my-spring-app:latest`
  - `-p 8080:8080` → leitet den Port 8080 im Container auf den Port 8080 Ihres Rechners weiter
  - `my-spring-app:latest` → das Image
- jetzt ist der Service auf Ihrem Rechner erreichbar, z.B. http://localhost:8080/books oder http://localhost:8080/swagger-ui/index.html
- Port 8080 schon belegt (z.B. weil die Anwendung noch aus IntelliJ läuft)? Dann anderen Port Ihres Rechners nehmen: `docker run -p 9090:8080 my-spring-app:latest` → http://localhost:9090/books
- prüfen, ob der Container läuft: `docker ps` → liefert auch die Container-Id
- stoppen des Containers: `docker stop <container-id>`
- löschen des Containers: `docker rm <container-id>`

## Continuous Integration
- Bei jedem Push baut GitHub Actions das Projekt, führt alle Tests aus und baut das Docker-Image (siehe `.github/workflows/main.yml`)
- Testberichte stehen danach im Actions-Lauf unter „Artifacts“
- Das automatische Hochladen des Images in die GitHub Container Registry ist im Workflow vorbereitet (auskommentiert) – mehr dazu in 07 Konfigurationsmanagement
