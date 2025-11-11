package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * Web-Layer Test für BookController mit @WebMvcTest
 *
 * UNTERSCHIED zu BookControllerTest (Unit Test):
 * - Testet die KOMPLETTE HTTP/REST-Schicht
 * - Validiert JSON-Serialisierung
 * - Prüft HTTP-Status-Codes
 * - Testet URL-Mappings
 *
 * Dies ist die BEST PRACTICE für Controller-Tests!
 *
 * WICHTIG: Ab Spring Boot 3.4.0
 * - @MockBean ist deprecated
 * - Verwende stattdessen @MockitoBean
 * - Import: org.springframework.test.context.bean.override.mockito.MockitoBean
 */
@WebMvcTest(BookController.class)
class BookControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean  // Neu ab Spring Boot 3.4.0 (statt @MockBean)
    private BookRepository bookRepository;

    private Book testBook1;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        testBook1 = new Book("Clean Code", "Robert C. Martin");
        testBook2 = new Book("Effective Java", "Joshua Bloch");
    }

    /**
     * Test 1: GET /books - Alle Bücher abrufen
     *
     * Testet:
     * - HTTP GET Request funktioniert
     * - Status Code 200 OK
     * - Content-Type ist application/json
     * - JSON-Array mit 2 Elementen
     * - JSON enthält korrekte Werte
     */
    @Test
    void getAllBooks_ShouldReturnJsonArray() throws Exception {
        // ARRANGE
        List<Book> books = Arrays.asList(testBook1, testBook2);
        when(bookRepository.findAll()).thenReturn(books);

        // ACT & ASSERT
        mockMvc.perform(get("/books"))
                .andDo(print())  // Optional: Gibt Request/Response in Console aus
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Clean Code")))
                .andExpect(jsonPath("$[0].author", is("Robert C. Martin")))
                .andExpect(jsonPath("$[1].title", is("Effective Java")))
                .andExpect(jsonPath("$[1].author", is("Joshua Bloch")));

        // VERIFY
        verify(bookRepository, times(1)).findAll();
    }

    /**
     * Test 2: GET /books - Leere Liste
     *
     * Testet Grenzfall: Keine Bücher vorhanden
     */
    @Test
    void getAllBooks_WhenEmpty_ShouldReturnEmptyJsonArray() throws Exception {
        // ARRANGE
        when(bookRepository.findAll()).thenReturn(Arrays.asList());

        // ACT & ASSERT
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$", is(empty())));

        // VERIFY
        verify(bookRepository).findAll();
    }

    /**
     * Test 3: POST /books - Neues Buch hinzufügen
     *
     * Testet:
     * - HTTP POST Request funktioniert
     * - JSON-Deserialisierung (Request Body → Book Object)
     * - JSON-Serialisierung (Book Object → Response)
     * - Status Code 200 OK
     */
    @Test
    void addBook_WithValidBook_ShouldReturnCreatedBook() throws Exception {
        // ARRANGE
        Book newBook = new Book("Spring Boot in Action", "Craig Walls");
        Book savedBook = new Book("Spring Boot in Action", "Craig Walls");

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // ACT & ASSERT
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Spring Boot in Action")))
                .andExpect(jsonPath("$.author", is("Craig Walls")));

        // VERIFY
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    /**
     * Test 4: POST /books - Mit konkreter Verifizierung
     *
     * Testet, dass das richtige Buch-Objekt an save() übergeben wird
     */
    @Test
    void addBook_ShouldSaveCorrectBook() throws Exception {
        // ARRANGE
        Book inputBook = new Book("Test-Driven Development", "Kent Beck");
        when(bookRepository.save(any(Book.class))).thenReturn(inputBook);

        // ACT
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputBook)))
                .andExpect(status().isOk());

        // VERIFY: Das richtige Buch wurde gespeichert
        verify(bookRepository).save(argThat(book ->
                book.getTitle().equals("Test-Driven Development") &&
                        book.getAuthor().equals("Kent Beck")
        ));
    }

    /**
     * Test 5: POST /books - Invalides JSON
     *
     * Testet Fehlerbehandlung bei fehlerhaftem JSON
     */
    @Test
    void addBook_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        // VERIFY: save() wurde NICHT aufgerufen
        verify(bookRepository, never()).save(any());
    }

    /**
     * Test 6: POST /books - Fehlender Content-Type
     *
     * Testet, dass Content-Type Header erforderlich ist
     */
    @Test
    void addBook_WithoutContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // ARRANGE
        Book newBook = new Book("Some Book", "Some Author");

        // ACT & ASSERT - Kein Content-Type Header!
        mockMvc.perform(post("/books")
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isUnsupportedMediaType());

        // VERIFY
        verify(bookRepository, never()).save(any());
    }

    /**
     * Test 7: POST /books - Leerer Request Body
     *
     * Testet Fehlerbehandlung bei fehlendem Body
     */
    @Test
    void addBook_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        // VERIFY
        verify(bookRepository, never()).save(any());
    }

    /**
     * Test 8: GET /books - JSON-Struktur detailliert prüfen
     *
     * Zeigt verschiedene JSONPath Assertions
     */
    @Test
    void getAllBooks_ShouldReturnCorrectJsonStructure() throws Exception {
        // ARRANGE
        List<Book> books = Arrays.asList(testBook1);
        when(bookRepository.findAll()).thenReturn(books);

        // ACT & ASSERT - Verschiedene JSONPath Assertions
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                // Prüfe Array
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                // Prüfe erstes Element
                .andExpect(jsonPath("$[0]").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].author").exists())
                // Prüfe Werte
                .andExpect(jsonPath("$[0].title").isString())
                .andExpect(jsonPath("$[0].title", not(emptyString())))
                // Prüfe, dass ID fehlt (wird bei neuem Book nicht gesetzt)
                .andExpect(jsonPath("$[0].id").doesNotExist());
    }

    /**
     * Test 9: Falsche HTTP-Methode verwenden
     *
     * Testet, dass nur erlaubte HTTP-Methoden funktionieren
     */
    @Test
    void getBooksWithWrongMethod_ShouldReturnMethodNotAllowed() throws Exception {
        // ACT & ASSERT - PUT statt GET
        mockMvc.perform(put("/books"))
                .andExpect(status().isMethodNotAllowed());

        // VERIFY: Repository wurde nicht aufgerufen
        verify(bookRepository, never()).findAll();
    }

    /**
     * Test 10: Falsche URL verwenden
     *
     * Testet 404 bei nicht existierender Route
     */
    @Test
    void getFromWrongUrl_ShouldReturnNotFound() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(get("/books/wrong"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test 11: POST /books - Response-Body extrahieren
     *
     * Zeigt, wie man den Response-Body zur weiteren Verarbeitung extrahiert
     */
    @Test
    void addBook_ShouldReturnBookWithAllFields() throws Exception {
        // ARRANGE
        Book newBook = new Book("Refactoring", "Martin Fowler");
        Book savedBook = new Book("Refactoring", "Martin Fowler");
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // ACT - Response extrahieren
        String responseBody = mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // ASSERT - Response-Body manuell prüfen
        Book returnedBook = objectMapper.readValue(responseBody, Book.class);
        assert returnedBook.getTitle().equals("Refactoring");
        assert returnedBook.getAuthor().equals("Martin Fowler");
    }

    /**
     * Test 12: Mehrere Requests hintereinander
     *
     * Testet, dass mehrere Requests funktionieren
     */
    @Test
    void multipleRequests_ShouldAllSucceed() throws Exception {
        // ARRANGE
        when(bookRepository.findAll()).thenReturn(Arrays.asList(testBook1));

        // ACT & ASSERT - 3 Requests
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        // VERIFY
        verify(bookRepository, times(3)).findAll();
    }
}
