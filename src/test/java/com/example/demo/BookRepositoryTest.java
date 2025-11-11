package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookRepositoryTest {

    /**
     * Integrationstest für BookRepository
     * Verwendet @DataJpaTest - echte In-Memory Datenbank (H2)!
     * KEIN Mockito - testet echte JPA-Operationen
     */


    @Autowired
    private BookRepository bookRepository;

    /**
     * Test 1: Buch speichern und wieder laden
     */
    @Test
    void testSaveAndFindBook() {
        // ARRANGE & ACT: Buch in echter DB speichern
        Book book = new Book("Spring in Action", "Craig Walls");
        Book savedBook = bookRepository.save(book);

        // ASSERT: ID wurde vergeben
        assertNotNull(savedBook.getId());
        assertEquals("Spring in Action", savedBook.getTitle());
        assertEquals("Craig Walls", savedBook.getAuthor());
    }

    /**
     * Test 2: Mehrere Bücher speichern und alle abrufen
     */
    @Test
    void testSaveMultipleBooksAndFindAll() {
        // ARRANGE: Mehrere Bücher speichern
        bookRepository.save(new Book("Clean Code", "Robert C. Martin"));
        bookRepository.save(new Book("Effective Java", "Joshua Bloch"));
        bookRepository.save(new Book("Domain-Driven Design", "Eric Evans"));

        // ACT: Alle Bücher abrufen
        List<Book> allBooks = bookRepository.findAll();

        // ASSERT
        assertEquals(3, allBooks.size());
    }

    /**
     * Test 3: Leere Datenbank
     */
    @Test
    void testFindAll_EmptyDatabase() {
        // ACT: Ohne vorher etwas zu speichern
        List<Book> books = bookRepository.findAll();

        // ASSERT
        assertTrue(books.isEmpty());
        assertEquals(0, books.size());
    }

    /**
     * Test 4: Buch löschen
     */
    @Test
    void testDeleteBook() {
        // ARRANGE: Buch speichern
        Book book = bookRepository.save(new Book("Test Book", "Test Author"));
        Long bookId = book.getId();

        // ACT: Buch löschen
        bookRepository.deleteById(bookId);

        // ASSERT: Buch existiert nicht mehr
        assertFalse(bookRepository.findById(bookId).isPresent());
    }
}

