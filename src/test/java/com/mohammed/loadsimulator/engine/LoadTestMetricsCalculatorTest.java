package com.mohammed.loadsimulator.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mohammed.loadsimulator.dto.LoadTestResult;

class LoadTestMetricsCalculatorTest {

	@Test
	void zeroRequestsReturnsZeroMetrics() {
		DurationRunResult run = DurationRunResult.empty();

		LoadTestResult result = LoadTestMetricsCalculator.toLoadTestResult(run, 10);

		assertEquals(LoadTestResult.zero(), result);
	}

	@Test
	void allFailedRequestsStillComputeLatencyMetrics() {
		DurationRunResult run = DurationRunResult.of(
				3, 0, 3, List.of(100.0, 200.0, 300.0), 1.0);

		LoadTestResult result = LoadTestMetricsCalculator.toLoadTestResult(run, 5);

		assertEquals(3, result.totalRequests());
		assertEquals(0, result.successfulRequests());
		assertEquals(3, result.failedRequests());
		assertEquals(200.0, result.averageResponseTimeMs());
		assertEquals(100.0, result.minResponseTimeMs());
		assertEquals(300.0, result.maxResponseTimeMs());
		assertEquals(300.0, result.p95ResponseTimeMs());
		assertEquals(0.6, result.requestsPerSecond());
		assertEquals(100.0, result.errorRate());
	}

	@Test
	void mixedOutcomesComputeCountsAndRates() {
		DurationRunResult run = DurationRunResult.of(
				4, 3, 1, List.of(10.0, 20.0, 30.0, 40.0), 2.0);

		LoadTestResult result = LoadTestMetricsCalculator.toLoadTestResult(run, 4);

		assertEquals(4, result.totalRequests());
		assertEquals(3, result.successfulRequests());
		assertEquals(1, result.failedRequests());
		assertEquals(25.0, result.averageResponseTimeMs());
		assertEquals(1.0, result.requestsPerSecond());
		assertEquals(25.0, result.errorRate());
	}

	@Test
	void percentile95UsesNearestRank() {
		assertEquals(10.0, LoadTestMetricsCalculator.percentile95(
				List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)));
		assertEquals(5.0, LoadTestMetricsCalculator.percentile95(
				List.of(1.0, 2.0, 3.0, 4.0, 5.0)));
		assertEquals(200.0, LoadTestMetricsCalculator.percentile95(List.of(100.0, 200.0)));
		assertEquals(42.0, LoadTestMetricsCalculator.percentile95(List.of(42.0)));
	}

	@Test
	void durationRunResultRejectsInconsistentCounts() {
		assertThrows(IllegalArgumentException.class, () -> DurationRunResult.of(
				5, 3, 1, List.of(1.0, 2.0, 3.0, 4.0, 5.0), 1.0));
	}

	@Test
	void durationRunResultRejectsMismatchedResponseTimes() {
		assertThrows(IllegalArgumentException.class, () -> DurationRunResult.of(
				2, 1, 1, List.of(1.0), 1.0));
	}

	@Test
	void fromWorkersAggregatesAllVirtualUsers() {
		List<VirtualUserResult> workers = List.of(
				new VirtualUserResult(2, 2, 0, List.of(10.0, 20.0)),
				new VirtualUserResult(2, 1, 1, List.of(30.0, 40.0)));

		DurationRunResult run = DurationRunResult.fromWorkers(workers, 5.0);

		assertEquals(4, run.totalRequests());
		assertEquals(3, run.successfulRequests());
		assertEquals(1, run.failedRequests());
		assertEquals(List.of(10.0, 20.0, 30.0, 40.0), run.responseTimesMs());
	}
}
