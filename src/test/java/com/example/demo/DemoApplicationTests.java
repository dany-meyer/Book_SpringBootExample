package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Smoke Test: Application Context lädt erfolgreich
	 *
	 * Dieser Test stellt sicher, dass:
	 * - Spring Boot startet ohne Exceptions
	 * - Der Application Context vollständig ist
	 * - Keine Bean-Definitions fehlen
	 */
	@Test
	void contextLoads() {
		// Dieser Test ist erfolgreich, wenn der Context lädt
		// ohne Exception zu werfen
		assertThat(applicationContext).isNotNull();
	}

	/**
	 * Smoke Test: BookController Bean existiert
	 *
	 * Stellt sicher, dass der Controller als Bean registriert ist.
	 */
	@Test
	void bookControllerLoads() {
		BookController controller = applicationContext.getBean(BookController.class);
		assertThat(controller).isNotNull();
	}

}
