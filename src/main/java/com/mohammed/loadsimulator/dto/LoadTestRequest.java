package com.mohammed.loadsimulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record LoadTestRequest(
		@NotBlank String url,
		@NotBlank String method,
		@Positive int virtualUsers,
		@Positive int durationSeconds) {
}
