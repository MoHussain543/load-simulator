package com.mohammed.loadsimulator.engine;

import org.springframework.stereotype.Component;

import com.mohammed.loadsimulator.dto.LoadTestRequest;
import com.mohammed.loadsimulator.dto.LoadTestResult;

@Component
public class DurationLoadTestEngine implements LoadTestEngine {

	private final HttpGetRunner httpGetRunner;

	public DurationLoadTestEngine(HttpGetRunner httpGetRunner) {
		this.httpGetRunner = httpGetRunner;
	}

	@Override
	public LoadTestResult execute(LoadTestRequest request) {
		DurationRunResult run = httpGetRunner.runForDuration(
				request.url(),
				request.durationSeconds(),
				request.virtualUsers());
		return LoadTestMetricsCalculator.toLoadTestResult(run, request.durationSeconds());
	}
}
