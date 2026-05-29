package com.mohammed.loadsimulator.engine;

import java.util.List;

record VirtualUserResult(
		long totalRequests,
		long successfulRequests,
		long failedRequests,
		List<Double> responseTimesMs) {

	VirtualUserResult {
		if (totalRequests != successfulRequests + failedRequests) {
			throw new IllegalArgumentException(
					"totalRequests must equal successfulRequests + failedRequests");
		}
		if (responseTimesMs.size() != totalRequests) {
			throw new IllegalArgumentException(
					"responseTimesMs size must match totalRequests");
		}
	}
}
