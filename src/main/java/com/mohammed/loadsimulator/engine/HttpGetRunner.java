package com.mohammed.loadsimulator.engine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

	public DurationRunResult runForDuration(String url, int durationSeconds, int virtualUsers) {
		HttpRequest httpRequest = buildRequest(url);
		if (httpRequest == null) {
			return DurationRunResult.empty();
		}

		long runStartNanos = System.nanoTime();
		long deadlineNanos = runStartNanos + Duration.ofSeconds(durationSeconds).toNanos();

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<VirtualUserResult>> futures = new ArrayList<>(virtualUsers);
			for (int i = 0; i < virtualUsers; i++) {
				futures.add(executor.submit(() -> runVirtualUser(httpRequest, deadlineNanos)));
			}

			long totalRequests = 0;
			long successfulRequests = 0;
			long failedRequests = 0;
			List<Double> responseTimesMs = new ArrayList<>();

			for (Future<VirtualUserResult> future : futures) {
				VirtualUserResult result = future.get();
				totalRequests += result.totalRequests();
				successfulRequests += result.successfulRequests();
				failedRequests += result.failedRequests();
				responseTimesMs.addAll(result.responseTimesMs());
			}

			double elapsedSeconds = nanosToSeconds(System.nanoTime() - runStartNanos);
			return new DurationRunResult(
					totalRequests,
					successfulRequests,
					failedRequests,
					responseTimesMs,
					elapsedSeconds);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return DurationRunResult.empty();
		}
		catch (ExecutionException ex) {
			throw new IllegalStateException("Virtual user task failed", ex.getCause());
		}
	}

	private VirtualUserResult runVirtualUser(HttpRequest httpRequest, long deadlineNanos) {
		long totalRequests = 0;
		long successfulRequests = 0;
		long failedRequests = 0;
		List<Double> responseTimesMs = new ArrayList<>();

		while (System.nanoTime() < deadlineNanos) {
			SingleRequestOutcome outcome = executeRequest(httpRequest);
			totalRequests++;
			if (outcome.success()) {
				successfulRequests++;
			}
			else {
				failedRequests++;
			}
			responseTimesMs.add(outcome.responseTimeMs());
		}

		return new VirtualUserResult(totalRequests, successfulRequests, failedRequests, responseTimesMs);
	}

	private HttpRequest buildRequest(String url) {
		try {
			URI uri = URI.create(url.trim());
			return HttpRequest.newBuilder()
					.uri(uri)
					.timeout(REQUEST_TIMEOUT)
					.GET()
					.build();
		}
		catch (IllegalArgumentException ex) {
			return null;
		}
	}

	private SingleRequestOutcome executeRequest(HttpRequest httpRequest) {
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

	private static double nanosToSeconds(long nanos) {
		return nanos / 1_000_000_000.0;
	}

	private record VirtualUserResult(
			long totalRequests,
			long successfulRequests,
			long failedRequests,
			List<Double> responseTimesMs) {
	}
}
