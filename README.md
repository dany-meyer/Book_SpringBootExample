# Spring Boot Beispiel #

## Structure ##
Das Beispiel demonstriert ein einfaches Beispiel in den Schichten
- Controller
- Repository
- Beans

## Slice-Tests ## 
- Controller-Schicht: BookControllerTest (mit Mock-Objekten, testet die Methoden) sowie BookControllerWebTest (via @WebMvcTest testet auch die Schnittstelle)
- Repository-Schicht: BookRepository-Schicht (mit @DataJpaTest, verwendet in-memory DB)
- Test, ob SpringBoot-Applikation korrekt startet: DemoApplicationTests (@SpringBootTest, prüft context)
- Integrationstest: BookApiIT (@SpringBootTest, prüft komplette Integration) 

## Start der Applikation aus IntelliJ
- über: Start --> DemoApplication
- Requests erreichbar: z.B. GET von http://localhost:8080/books

## Static WebSite with post/get-Examples ##
Web-Site mit einfacher Integration von post und get-Requests
- abgelegt unter main/ressources/static
- Beispiel kann als Start für weitere Anwendungen gesehen werden z.B. in Vue.js
- Test-Web-Site erreichbar: http://localhost:8080/books.html (wenn Spring-Boot-App läuft)

## Swagger-Doc ##
  - wird aktiviert durch Eintragen des entsprechenden Eintrags in den dependencies (siehe pom.xml)
  - erreichbar: http://localhost:8080/swagger-ui/index.html

## in-memory Datenbank ## 
  - es ist einen h2-in-Memory DB konfiguriert (siehe main/ressouces/application.properties)
  - ist bei jedem Neustart geleert
  - web-Konsole erreichbar über: http://localhost:8080/h2-console

## Deployment via docker und render
### Voraussetzungen 
- docker installieren z.B. docker Desktop via http://docker.com
- test in Konsole (Terminal) mit: docker --version
- Dockerfile erzeugen:
  - enthält Anweisungen zum Erstellen des Docker-Image
  - liegt auf oberster Ebene des Projektes, auf gleicher Ebene wie pom.xml
- Anmelden auf Render: http://render.com

### Schritt 1
- ein jar der Applikation erzeugen: in (IntelliJ-)Terminal eingeben:  ./mvnw clean package -DskipTests
- danach Docker starten (das ist i.d.R. eine extra Application); zum Test, ob Docker läuft: docker info

### Schritt 2: Docker-Image erzeugen
- Docker-Image wird entsprechend des Dockerfile erzeugt: docker build --platform linux/amd64 -t my-spring-app:latest .
- Das gebaute image kann jetzt testweise lokal ausgeführt werden, später soll es auf ein cloud-repository geladen werden
  - prüfe, ob das Image vorhanden ist: docker images
  
### Schritt 2.1: Lokaler Test - Docker-Container starten und testen
- container (aus dem Image wird ein lauffähiger Prozess erzeugt) starten: docker run -p 8080:8080 my-spring-app:latest
  - -p 8080:8080 → leitet den Port 8080 im Container auf deinen Mac-Port 8080 weiter
  - my-spring-app:latest → das Image
- jetzt kann ich meinen Service auf meinem Rechner über Port 8080 erreichen: z.B. http://localhost:8080/swagger-ui/index.html
- prüfen, ob ob der Container läuft: docker ps --> liefert auch Container-Id
- stoppen des Containers: docker stop <container-id>
- löschen des Containers: docker rm <container-id>

### Schritt 3: Image auf GitHub Container Registry hochladen ###
- auf gitHub Container Registry (ghcr) registieren: echo "<GITHUB_TOKEN>" | docker login ghcr.io -u deinuser --password-stdin
- das Image für GHCR taggen:
  - GHCR verwendet dieses URL-Schema: ghcr.io/<USERNAME>/<IMAGENAME>:<TAG>
  - in unserem Beispiel: docker tag my-spring-app:latest ghcr.io/<USERNAME>/my-spring-app:latest
- Image auf GHCR hochladen:
  - docker push ghcr.io/deinuser/myapp:latest
  - in unserem Beispiel: docker push ghcr.io/deinuser/my-spring-app:latest
- in GitHub prüfen:
  - Gehe zu: GitHub → Dein Profil → Packages
  - dort sollten wir unser image sehen
  - ggf. über Settings 'public' machen
  
### Schritt 4: in Render verwenden ###
- In Render → New Web Service → Deploy from existing image: ghcr.io/deinuser/my-spring-app:latest
- in der Render Konsole ist nun beobachtbar, dass der Docker-Container gestartet wird
- nach dem Starten der Spring-Boot-Application ist der Service erreichbar z.B. https://my-spring-app-latest-bvnk.onrender.com/swagger-ui/index.html
- beachten Sie, dass in der Free-Version der Service bei Innaktivität > 60s herunterfährt und danach beim ersten Zugriff eine längere Startup-Zeit benötigt


