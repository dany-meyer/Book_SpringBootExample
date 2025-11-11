package com.example.demo;


import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * voller Integrationstest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookApiIT {

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    void create_and_get_over_http() {
        var toCreate = new Book("Clean Code", "Martin");

        ResponseEntity<Book> created =
                rest.postForEntity("http://localhost:" + port + "/books", toCreate, Book.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        Long id = requireNonNull(created.getBody()).getId();

        Book found = rest.getForObject("http://localhost:" + port + "/books/" + id, Book.class);
        assertThat(found.getTitle()).isEqualTo("Clean Code");
    }
}
