package com.mohammed.loadsimulator.engine;

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
}
