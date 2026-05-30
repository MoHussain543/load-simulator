package com.mohammed.loadsimulator.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalhostOnlyUrlValidatorTest {

	private LocalhostOnlyUrlValidator validator;

	@BeforeEach
	void setUp() {
		validator = new LocalhostOnlyUrlValidator();
	}

	@Test
	void localhostUrlsAreAllowed() {
		assertTrue(validator.isValid("http://localhost/", null));
		assertTrue(validator.isValid("http://localhost:8080/actuator", null));
		assertTrue(validator.isValid("  http://LOCALHOST:9090/path  ", null));
	}

	@Test
	void loopbackIpUrlsAreAllowed() {
		assertTrue(validator.isValid("http://127.0.0.1/", null));
		assertTrue(validator.isValid("http://127.0.0.1:8080/actuator", null));
	}

	@Test
	void nonLocalhostHostsAreRejected() {
		assertFalse(validator.isValid("http://example.com/", null));
		assertFalse(validator.isValid("http://192.168.1.10/", null));
		assertFalse(validator.isValid("http://0.0.0.0/", null));
	}

	@Test
	void nonHttpSchemesAreRejected() {
		assertFalse(validator.isValid("https://localhost/", null));
		assertFalse(validator.isValid("ftp://127.0.0.1/", null));
	}

	@Test
	void malformedUrlsAreRejected() {
		assertFalse(validator.isValid("not-a-url", null));
		assertFalse(validator.isValid("http://", null));
	}

	@Test
	void blankValuesAreDeferredToNotBlankValidation() {
		assertTrue(validator.isValid(null, null));
		assertTrue(validator.isValid("", null));
		assertTrue(validator.isValid("   ", null));
	}
}
