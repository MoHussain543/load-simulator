package com.mohammed.loadsimulator.engine;

import java.util.ArrayList;
import java.util.List;

public record DurationRunResult(
		long totalRequests,
		long successfulRequests,
		long failedRequests,
		List<Double> responseTimesMs,
		double elapsedSeconds) {

	public static DurationRunResult empty() {
		return new DurationRunResult(0, 0, 0, List.of(), 0.0);
	}

	static DurationRunResult fromWorkers(List<VirtualUserResult> workers, double elapsedSeconds) {
		long totalRequests = 0;
		long successfulRequests = 0;
		long failedRequests = 0;
		List<Double> responseTimesMs = new ArrayList<>();

		for (VirtualUserResult worker : workers) {
			totalRequests += worker.totalRequests();
			successfulRequests += worker.successfulRequests();
			failedRequests += worker.failedRequests();
			responseTimesMs.addAll(worker.responseTimesMs());
		}

		return of(totalRequests, successfulRequests, failedRequests, responseTimesMs, elapsedSeconds);
	}

	static DurationRunResult of(
			long totalRequests,
			long successfulRequests,
			long failedRequests,
			List<Double> responseTimesMs,
			double elapsedSeconds) {
		if (totalRequests != successfulRequests + failedRequests) {
			throw new IllegalArgumentException(
					"totalRequests must equal successfulRequests + failedRequests");
		}
		if (responseTimesMs.size() != totalRequests) {
			throw new IllegalArgumentException(
					"responseTimesMs size must match totalRequests");
		}
		return new DurationRunResult(
				totalRequests,
				successfulRequests,
				failedRequests,
				List.copyOf(responseTimesMs),
				elapsedSeconds);
	}
}
