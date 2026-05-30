package com.mohammed.loadsimulator.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class LoadTestRequestValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Test
	void blankUrlIsRejected() {
		assertViolation(validRequest("", "GET", 1, 5), "url", "url must not be blank");
	}

	@Test
	void blankMethodIsRejected() {
		assertViolation(validRequest("http://localhost:8080/", "", 1, 5), "method", "method must not be blank");
	}

	@Test
	void virtualUsersBelowOneIsRejected() {
		assertViolation(validRequest("http://localhost:8080/", "GET", 0, 5),
				"virtualUsers", "virtualUsers must be between 1 and 1000");
	}

	@Test
	void virtualUsersAboveOneThousandIsRejected() {
		assertViolation(validRequest("http://localhost:8080/", "GET", 1001, 5),
				"virtualUsers", "virtualUsers must be between 1 and 1000");
	}

	@Test
	void durationSecondsBelowOneIsRejected() {
		assertViolation(validRequest("http://localhost:8080/", "GET", 1, 0),
				"durationSeconds", "durationSeconds must be between 1 and 60");
	}

	@Test
	void durationSecondsAboveSixtyIsRejected() {
		assertViolation(validRequest("http://localhost:8080/", "GET", 1, 61),
				"durationSeconds", "durationSeconds must be between 1 and 60");
	}

	@Test
	void getMethodIsAllowed() {
		assertTrue(violations(validRequest("http://localhost:8080/", "GET", 1, 5)).isEmpty());
		assertTrue(violations(validRequest("http://127.0.0.1:8080/", "get", 1, 5)).isEmpty());
	}

	@Test
	void unsupportedMethodsAreRejected() {
		assertViolation(validRequest("http://localhost:8080/", "POST", 1, 5),
				"method", "Only GET is supported for the MVP");
		assertViolation(validRequest("http://localhost:8080/", "PUT", 1, 5),
				"method", "Only GET is supported for the MVP");
	}

	@Test
	void nonLocalhostUrlsAreRejected() {
		assertViolation(validRequest("http://example.com/", "GET", 1, 5),
				"url", "URL must use http://localhost or http://127.0.0.1");
	}

	@Test
	void validLocalhostRequestPassesValidation() {
		assertTrue(violations(validRequest("http://localhost:8080/actuator", "GET", 10, 30)).isEmpty());
		assertTrue(violations(validRequest("http://127.0.0.1:8080/actuator", "GET", 10, 30)).isEmpty());
	}

	private static LoadTestRequest validRequest(String url, String method, int virtualUsers, int durationSeconds) {
		return new LoadTestRequest(url, method, virtualUsers, durationSeconds);
	}

	private static Set<ConstraintViolation<LoadTestRequest>> violations(LoadTestRequest request) {
		return validator.validate(request);
	}

	private static void assertViolation(LoadTestRequest request, String field, String expectedMessage) {
		Set<ConstraintViolation<LoadTestRequest>> result = violations(request);
		assertFalse(result.isEmpty(), "Expected validation failure for field " + field);
		assertTrue(result.stream().anyMatch(v -> field.equals(v.getPropertyPath().toString())
				&& expectedMessage.equals(v.getMessage())),
				() -> "Expected violation on " + field + " but got: " + result);
	}
}
