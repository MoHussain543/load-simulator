package com.mohammed.loadsimulator.dto;

import java.net.URI;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LocalhostOnlyUrlValidator implements ConstraintValidator<LocalhostOnlyUrl, String> {

	@Override
	public boolean isValid(String url, ConstraintValidatorContext context) {
		if (url == null || url.isBlank()) {
			return true;
		}

		try {
			URI uri = URI.create(url.trim());
			if (!"http".equalsIgnoreCase(uri.getScheme())) {
				return false;
			}

			String host = uri.getHost();
			if (host == null) {
				return false;
			}

			return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1");
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}
}
