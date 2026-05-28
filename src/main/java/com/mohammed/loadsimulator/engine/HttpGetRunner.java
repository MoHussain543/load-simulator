package com.mohammed.loadsimulator.engine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.stereotype.Component;

@Component
public class HttpGetRunner {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

	private final HttpClient httpClient;

	public HttpGetRunner() {
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(CONNECT_TIMEOUT)
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}

	public SingleRequestOutcome execute(String url) {
		URI uri;
		try {
			uri = URI.create(url.trim());
		}
		catch (IllegalArgumentException ex) {
			return SingleRequestOutcome.failure(0.0);
		}

		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(REQUEST_TIMEOUT)
				.GET()
				.build();

		long startNanos = System.nanoTime();
		try {
			HttpResponse<Void> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
			double responseTimeMs = nanosToMillis(System.nanoTime() - startNanos);
			boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
			return new SingleRequestOutcome(success, responseTimeMs);
		}
		catch (IOException | InterruptedException ex) {
			if (ex instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			double responseTimeMs = nanosToMillis(System.nanoTime() - startNanos);
			return SingleRequestOutcome.failure(responseTimeMs);
		}
	}

	private static double nanosToMillis(long nanos) {
		return nanos / 1_000_000.0;
	}
}
