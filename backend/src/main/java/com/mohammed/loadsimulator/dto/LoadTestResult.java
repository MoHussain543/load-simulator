package com.mohammed.loadsimulator.dto;

public record LoadTestResult(
		long totalRequests,
		long successfulRequests,
		long failedRequests,
		double averageResponseTimeMs,
		double minResponseTimeMs,
		double maxResponseTimeMs,
		double p95ResponseTimeMs,
		double requestsPerSecond,
		double errorRate) {

	public static LoadTestResult zero() {
		return new LoadTestResult(0, 0, 0, 0, 0, 0, 0, 0, 0);
	}
}
