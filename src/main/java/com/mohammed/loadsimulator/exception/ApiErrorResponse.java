package com.mohammed.loadsimulator.exception;

import java.util.Map;

public record ApiErrorResponse(String message, Map<String, String> errors) {

	public static ApiErrorResponse of(String message) {
		return new ApiErrorResponse(message, Map.of());
	}

	public static ApiErrorResponse of(String message, Map<String, String> errors) {
		return new ApiErrorResponse(message, errors);
	}
}
