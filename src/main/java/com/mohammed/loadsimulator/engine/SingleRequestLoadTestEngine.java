package com.mohammed.loadsimulator.engine;

import org.springframework.stereotype.Component;

import com.mohammed.loadsimulator.dto.LoadTestRequest;
import com.mohammed.loadsimulator.dto.LoadTestResult;

@Component
public class SingleRequestLoadTestEngine implements LoadTestEngine {

	private final HttpGetRunner httpGetRunner;

	public SingleRequestLoadTestEngine(HttpGetRunner httpGetRunner) {
		this.httpGetRunner = httpGetRunner;
	}

	@Override
	public LoadTestResult execute(LoadTestRequest request) {
		SingleRequestOutcome outcome = httpGetRunner.execute(request.url());
		return toLoadTestResult(outcome);
	}

	private static LoadTestResult toLoadTestResult(SingleRequestOutcome outcome) {
		long successful = outcome.success() ? 1L : 0L;
		long failed = outcome.success() ? 0L : 1L;
		double responseTimeMs = outcome.responseTimeMs();
		double requestsPerSecond = responseTimeMs > 0 ? 1000.0 / responseTimeMs : 0.0;
		double errorRate = outcome.success() ? 0.0 : 1.0;

		return new LoadTestResult(
				1L,
				successful,
				failed,
				responseTimeMs,
				responseTimeMs,
				responseTimeMs,
				responseTimeMs,
				requestsPerSecond,
				errorRate);
	}
}
