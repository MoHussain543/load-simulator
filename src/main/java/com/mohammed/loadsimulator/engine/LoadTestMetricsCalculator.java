package com.mohammed.loadsimulator.engine;

import java.util.Comparator;
import java.util.List;

import com.mohammed.loadsimulator.dto.LoadTestResult;

final class LoadTestMetricsCalculator {

	private LoadTestMetricsCalculator() {
	}

	static LoadTestResult toLoadTestResult(DurationRunResult run) {
		if (run.totalRequests() == 0) {
			return new LoadTestResult(0, 0, 0, 0, 0, 0, 0, 0, 0);
		}

		List<Double> times = run.responseTimesMs();
		double average = times.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		double min = times.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
		double max = times.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
		double p95 = percentile95(times);
		double requestsPerSecond = run.elapsedSeconds() > 0
				? run.totalRequests() / run.elapsedSeconds()
				: 0.0;
		double errorRate = (double) run.failedRequests() / run.totalRequests();

		return new LoadTestResult(
				run.totalRequests(),
				run.successfulRequests(),
				run.failedRequests(),
				average,
				min,
				max,
				p95,
				requestsPerSecond,
				errorRate);
	}

	private static double percentile95(List<Double> responseTimesMs) {
		List<Double> sorted = responseTimesMs.stream()
				.sorted(Comparator.naturalOrder())
				.toList();
		int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
		index = Math.clamp(index, 0, sorted.size() - 1);
		return sorted.get(index);
	}
}
