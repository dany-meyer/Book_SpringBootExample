package com.example.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit-Test für BookController mit Mockito
 * Das Repository wird gemockt - keine echte Datenbank!
 */
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookRepository mockRepository;

    @InjectMocks
    private BookController bookController;

    private Book testBook1;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        // Testdaten vorbereiten
        testBook1 = new Book("Clean Code", "Robert C. Martin");
        testBook2 = new Book("Effective Java", "Joshua Bloch");
    }

    /**
     * Test 1: Alle Bücher abrufen
     * Zeigt: when().thenReturn() mit Liste
     */
    @Test
    void testGetAll_ReturnsAllBooks() {
        // ARRANGE: Mock vorbereiten
        List<Book> expectedBooks = Arrays.asList(testBook1, testBook2);
        when(mockRepository.findAll()).thenReturn(expectedBooks);

        // ACT: Controller-Methode aufrufen
        List<Book> actualBooks = bookController.getAll();

        // ASSERT: Ergebnis prüfen
        assertNotNull(actualBooks);
        assertEquals(2, actualBooks.size());
        assertEquals("Clean Code", actualBooks.get(0).getTitle());
        assertEquals("Effective Java", actualBooks.get(1).getTitle());

        // VERIFY: Repository wurde aufgerufen
        verify(mockRepository, times(1)).findAll();
    }

    /**
     * Test 2: Leere Liste zurückgeben
     * Zeigt: Grenzfall mit leerer Liste
     */
    @Test
    void testGetAll_EmptyList() {
        // ARRANGE
        when(mockRepository.findAll()).thenReturn(Arrays.asList());

        // ACT
        List<Book> actualBooks = bookController.getAll();

        // ASSERT
        assertNotNull(actualBooks);
        assertEquals(0, actualBooks.size());
        assertTrue(actualBooks.isEmpty());

        // VERIFY
        verify(mockRepository).findAll();
    }

    /**
     * Test 3: Neues Buch hinzufügen
     * Zeigt: any() Matcher und save() Operation
     */
    @Test
    void testAddBook_Success() {
        // ARRANGE: Buch das gespeichert werden soll
        Book newBook = new Book("Spring Boot in Action", "Craig Walls");

        // Mock: save() gibt das Buch mit generierter ID zurück
        Book savedBook = new Book("Spring Boot in Action", "Craig Walls");
        // Simuliere, dass die Datenbank eine ID vergibt
        // (In Realität würde JPA das machen)

        when(mockRepository.save(any(Book.class))).thenReturn(savedBook);

        // ACT
        Book result = bookController.addBook(newBook);

        // ASSERT
        assertNotNull(result);
        assertEquals("Spring Boot in Action", result.getTitle());
        assertEquals("Craig Walls", result.getAuthor());

        // VERIFY: save() wurde mit einem Book-Objekt aufgerufen
        verify(mockRepository, times(1)).save(any(Book.class));
    }

    /**
     * Test 4: Spezifisches Buch speichern und verifizieren
     * Zeigt: ArgumentCaptor (optional, fortgeschritten)
     */
    @Test
    void testAddBook_VerifyCorrectBookSaved() {
        // ARRANGE
        Book inputBook = new Book("Test-Driven Development", "Kent Beck");
        when(mockRepository.save(any(Book.class))).thenReturn(inputBook);

        // ACT
        bookController.addBook(inputBook);

        // VERIFY: Das richtige Buch wurde an save() übergeben
        verify(mockRepository).save(argThat(book ->
                book.getTitle().equals("Test-Driven Development") &&
                        book.getAuthor().equals("Kent Beck")
        ));
    }

    /**
     * Test 5: Multiple Aufrufe
     * Zeigt: Mehrfache Interaktionen
     */
    @Test
    void testMultipleGetAllCalls() {
        // ARRANGE
        List<Book> books = Arrays.asList(testBook1);
        when(mockRepository.findAll()).thenReturn(books);

        // ACT: Mehrfach aufrufen
        bookController.getAll();
        bookController.getAll();
        bookController.getAll();

        // VERIFY: findAll() wurde 3x aufgerufen
        verify(mockRepository, times(3)).findAll();
    }

    /**
     * Test 6: Keine unerwarteten Interaktionen
     * Zeigt: verifyNoMoreInteractions()
     */
    @Test
    void testNoUnexpectedInteractions() {
        // ARRANGE
        when(mockRepository.findAll()).thenReturn(Arrays.asList(testBook1));

        // ACT
        bookController.getAll();

        // VERIFY: Nur findAll() wurde aufgerufen, nichts anderes
        verify(mockRepository).findAll();
        verifyNoMoreInteractions(mockRepository);
    }
}
