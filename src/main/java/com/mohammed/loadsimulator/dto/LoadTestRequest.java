package com.mohammed.loadsimulator.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoadTestRequest(
		@NotBlank(message = "url must not be blank")
		@LocalhostOnlyUrl
		String url,
		@NotBlank(message = "method must not be blank")
		@Pattern(regexp = "(?i)^GET$", message = "Only GET is supported for the MVP")
		String method,
		@Min(value = 1, message = "virtualUsers must be between 1 and 1000")
		@Max(value = 1000, message = "virtualUsers must be between 1 and 1000")
		int virtualUsers,
		@Min(value = 1, message = "durationSeconds must be between 1 and 60")
		@Max(value = 60, message = "durationSeconds must be between 1 and 60")
		int durationSeconds) {
}
