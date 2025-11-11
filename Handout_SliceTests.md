# Slice-Tests in Spring Boot – Handout für das Projekt `Book_SpringBootExample`

Dieses Handout erklärt die Teststruktur und -methodik der Beispiel-App  
[`Book_SpringBootExample`](https://github.com/dany-meyer/Book_SpringBootExample).  
Es zeigt, wie **Slice-Tests** in Spring Boot genutzt werden, um gezielt einzelne Schichten (Web, JPA) zu prüfen,  
und wann ein vollständiger **Integrationstest** (`@SpringBootTest`) sinnvoll ist.

---

## 1. Ziel & Testarten

### Was ist ein Slice-Test?
Ein **Slice-Test** („Schichtentest“) startet **nur einen Teil des Spring Application Contexts**,  
z. B. den **Web-MVC-Slice** oder den **JPA-Slice**. Dadurch sind die Tests **schnell**, **fokussiert** und **isoliert**.  
Sie prüfen eine **Schicht (Layer)** realistisch, aber ohne die gesamte Anwendung zu laden.

| Testtyp | Spring-Kontext | Webserver | DB | Zweck |
|----------|----------------|:----------:|:---:|-------|
| **Unit Test** | ✗ | ✗ | ✗ | Reine Logik, keine Spring-Abhängigkeiten |
| **Slice Test** | ✔️ (Teil-Kontext) | ✗ | optional (embedded) | Schichtgetrennte Tests, schnell, realistisch |
| **Integrationstest** | ✔️ (vollständig) | optional ✔️ | ✔️ | Zusammenspiel aller Komponenten |

> **Grundprinzip:**  
> Slice-Tests prüfen gezielt **eine Schicht** mit echten Spring-Komponenten, z. B. Controller oder Repository.  
> Nur wenige `@SpringBootTest`-Fälle testen den **Durchstich** durch alle Layer.

---

## 2. Annotationen – Übersicht & Erklärung

Spring Boot stellt spezielle **Test-Annotationen** bereit, um gezielt Teilbereiche der Anwendung zu testen.

### Wichtigste Annotationen

- **`@WebMvcTest`**  
  Startet nur den **Web-MVC-Teil** der Anwendung. Lädt Controller, JSON-Konfiguration, Validierung usw.  
  Datenbank-Komponenten wie Repositories werden **nicht geladen**.  
  → Typisch für REST-API-Tests mit `MockMvc`.

- **`@DataJpaTest`**  
  Startet nur den **JPA-Kontext**: EntityManager, Repositories, Hibernate-Konfiguration.  
  Nutzt automatisch eine **embedded H2-Datenbank** und macht nach jedem Test ein **Rollback**.  
  → Ideal für Repository- und Entity-Mapping-Tests.

- **`@SpringBootTest`**  
  Startet den **vollen Spring-Kontext**.  
  Mit `webEnvironment = RANDOM_PORT` kann ein echter eingebetteter Server gestartet werden.  
  → Für vollständige Integrationstests über alle Schichten hinweg.

- **`@MockBean`**  
  Erzeugt einen **Mockito-Mock** im Spring-Kontext.  
  Wird häufig in Slice-Tests genutzt, um z. B. ein Repository zu ersetzen.

- **`@Autowired`**  
  Führt **Dependency Injection** aus – Spring sucht und injiziert automatisch eine passende Bean  
  (z. B. `MockMvc`, `BookRepository`, `TestRestTemplate`).

- **`@AutoConfigureMockMvc`**  
  Aktiviert `MockMvc` im vollständigen Kontext (`@SpringBootTest`).  
  → Wird genutzt, wenn Integrationstests ohne echten HTTP-Server laufen sollen.

- **`@Test`**  
  Markiert eine Testmethode. (JUnit 5)

---

## 3. Projektstruktur

```
src/
 ├─ main/java/com/example/demo/
 │   ├─ Book.java
 │   ├─ BookRepository.java
 │   ├─ BookController.java
 │   └─ DemoApplication.java
 └─ test/java/com/example/demo/
     ├─ BookControllerTest.java   // Web-Slice-Test (Controller)
     ├─ BookRepositoryTest.java   // JPA-Slice-Test (Repository)
     ├─ BookApiIT.java            // Voller Integrationstest
     └─ DemoApplicationTests.java // Smoke-Test
```

---

## 4. Web-Slice mit `@WebMvcTest`

**Testklasse:** `BookControllerTest.java`

- Lädt ausschließlich MVC-Komponenten (`BookController`, JSON, Validation).  
- Das `BookRepository` wird **gemockt** (`@MockBean`).  
- Prüft API-Endpunkte, HTTP-Status, JSON-Struktur und Validierung.

```java
@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired MockMvc mvc;
  @MockBean BookRepository repo;

  @Test
  void all_returnsOkAndPayload() throws Exception {
    when(repo.findAll()).thenReturn(List.of(new Book(1L, "DDD", "Evans")));

    mvc.perform(get("/books"))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$[0].title").value("DDD"));
  }

  @Test
  void create_persists_and_returns_entity() throws Exception {
    var body = "{"title":"Clean Code","author":"Martin"}";
    when(repo.save(any())).thenAnswer(inv -> {
      Book b = inv.getArgument(0);
      b.setId(5L);
      return b;
    });

    mvc.perform(post("/books")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.id").value(5))
       .andExpect(jsonPath("$.title").value("Clean Code"));
  }
}
```

**Kernpunkte:**
- Sehr schneller Test (kein Datenbankstart).  
- Gut geeignet für Validierung, Fehlerfälle und API-Verhalten.

---

## 5. JPA-Slice mit `@DataJpaTest`

**Testklasse:** `BookRepositoryTest.java`

- Startet echten JPA-Kontext mit **embedded H2-Datenbank**.  
- Alle Transaktionen werden nach jedem Test **zurückgerollt**.  
- Testet Entity-Mapping, CRUD-Operationen und Queries.

```java
@DataJpaTest
class BookRepositoryTest {

  @Autowired BookRepository repo;

  @Test
  void save_and_findById() {
    Book saved = repo.save(new Book(null, "DDD", "Evans"));
    assertThat(saved.getId()).isNotNull();

    Optional<Book> found = repo.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("DDD");
  }
}
```

**Kernpunkte:**
- Nutzt embedded DB, kein externer Server.  
- Schnell, stabil, reproduzierbar.

---

## 6. Integrationstest mit `@SpringBootTest`

**Testklasse:** `BookApiIT.java`

Startet die komplette Spring Boot-Anwendung mit eingebettetem Webserver.  
Testet den gesamten Pfad: Controller → Service/Repository → Datenbank.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiIT {

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;

  @Test
  void create_and_get_over_http() {
    var toCreate = new Book(null, "Clean Architecture", "Martin");

    ResponseEntity<Book> created =
        rest.postForEntity("http://localhost:" + port + "/books", toCreate, Book.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
    Long id = requireNonNull(created.getBody()).getId();

    Book found = rest.getForObject("http://localhost:" + port + "/books/" + id, Book.class);
    assertThat(found.getTitle()).isEqualTo("Clean Architecture");
  }
}
```

**Kernpunkte:**
- Testet echten HTTP-Pfad.  
- Verifiziert Zusammenspiel aller Komponenten.  
- Langsamer als Slice-Tests, aber unverzichtbar für End-to-End.

---

## 7. Smoke-Test mit `@SpringBootTest`

**Testklasse:** `DemoApplicationTests.java`

```java
@SpringBootTest
class DemoApplicationTests {
  @Test void contextLoads() { }
}
```

- Prüft nur, dass der **Spring Application Context** korrekt startet.  
- Wird automatisch beim Build ausgeführt.

---

## 8. Maven-Konfiguration

In `pom.xml` ist standardmäßig enthalten:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-test</artifactId>
  <scope>test</scope>
</dependency>
```

Tests ausführen:
```bash
mvn test
```

---

## 9. TL;DR

| Ebene | Annotation | Ziel |
|--------|-------------|------|
| **Controller** | `@WebMvcTest` | Schnelle API-Tests ohne DB |
| **Repository** | `@DataJpaTest` | JPA-Funktion mit embedded DB |
| **End-to-End** | `@SpringBootTest` | Kompletter Ablauf (HTTP → DB) |
| **Smoke-Test** | `contextLoads()` | Sicherstellen, dass App startet |

**Merke:**  
- **Slice-Tests** sind schnell, isoliert und decken Schichten gezielt ab.  
- **Integrationstests** sichern das Gesamtverhalten.  
- `@Autowired` injiziert automatisch Testkomponenten (`MockMvc`, `BookRepository`, `TestRestTemplate`).  
- Eine gute Teststrategie kombiniert beides: **Schnelligkeit + Abdeckung**.
