package com.mohammed.loadsimulator.engine;

import java.util.Arrays;
import java.util.List;

import com.mohammed.loadsimulator.dto.LoadTestResult;

final class LoadTestMetricsCalculator {

	private LoadTestMetricsCalculator() {
	}

	static LoadTestResult toLoadTestResult(DurationRunResult run, int durationSeconds) {
		long totalRequests = run.totalRequests();
		if (totalRequests == 0) {
			return LoadTestResult.zero();
		}

		LatencyMetrics latency = LatencyMetrics.from(run.responseTimesMs());
		double requestsPerSecond = durationSeconds > 0
				? (double) totalRequests / durationSeconds
				: 0.0;
		double errorRate = (double) run.failedRequests() / totalRequests * 100.0;

		return new LoadTestResult(
				totalRequests,
				run.successfulRequests(),
				run.failedRequests(),
				latency.averageMs(),
				latency.minMs(),
				latency.maxMs(),
				latency.p95Ms(),
				requestsPerSecond,
				errorRate);
	}

	/**
	 * Nearest-rank 95th percentile: sort ascending, then pick the smallest value
	 * at or above the 95% mark (index = ceil(0.95 * n) - 1).
	 */
	static double percentile95(List<Double> responseTimesMs) {
		int count = responseTimesMs.size();
		if (count == 0) {
			return 0.0;
		}

		double[] sorted = new double[count];
		for (int i = 0; i < count; i++) {
			sorted[i] = responseTimesMs.get(i);
		}
		Arrays.sort(sorted);

		int index = (int) Math.ceil(0.95 * count) - 1;
		index = Math.clamp(index, 0, count - 1);
		return sorted[index];
	}

	private record LatencyMetrics(double averageMs, double minMs, double maxMs, double p95Ms) {

		static LatencyMetrics from(List<Double> responseTimesMs) {
			if (responseTimesMs.isEmpty()) {
				return new LatencyMetrics(0.0, 0.0, 0.0, 0.0);
			}

			double sum = 0.0;
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			for (double responseTimeMs : responseTimesMs) {
				sum += responseTimeMs;
				min = Math.min(min, responseTimeMs);
				max = Math.max(max, responseTimeMs);
			}

			double average = sum / responseTimesMs.size();
			double p95 = percentile95(responseTimesMs);
			return new LatencyMetrics(average, min, max, p95);
		}
	}
}
