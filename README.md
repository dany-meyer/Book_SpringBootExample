## Spring Boot Beispiel ##

Das Beispiel demonstriert ein einfaches Beispiel in den Schichten
- Controller
- Repository
- Beans

Inklusive Slice-Tests
- Controller-Schicht: BookControllerTest (mit Mock-Objekten, testet die Methoden) sowie BookControllerWebTest (via @WebMvcTest testet auch die Schnittstelle)
- Repository-Schicht: BookRepository-Schicht (mit @DataJpaTest, verwendet in-memory DB)
- Test, ob SpringBoot-Applikation korrekt startet: DemoApplicationTests (@SpringBootTest, prüft context)
- Integrationstest: BookApiIT (@SpringBootTest, prüft komplette Integration) 


Web-Site mit einfacher Integration von post und get-Requests
- abgelegt unter main/ressources/static
- Beispiel kann als Start für weitere Anwendungen gesehen werden z.B. in Vue.js

Start der Applikation aus IntelliJ
- über: Start --> DemoApplication
- Requests erreichbar: z.B. GET von http://localhost:8080/books
- Swagger-Doc
  - wird aktiviert durch Eintragen des entsprechenden Eintrags in den dependencies (siehe pom.xml)
  - erreichbar: http://localhost:8080/swagger-ui/index.html
- Test-Web-Site
  - erreichbar: http://localhost:8080/books.html
- Datenbank:
  - es ist einen h2-in-Memory DB konfiguriert (siehe main/ressouces/application.properties)
  - ist bei jedem Neustart geleert
  - web-Konsole erreichbar über: http://localhost:8080/h2-console





