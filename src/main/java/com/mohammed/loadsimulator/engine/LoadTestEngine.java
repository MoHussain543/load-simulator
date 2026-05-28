package com.mohammed.loadsimulator.engine;

import com.mohammed.loadsimulator.dto.LoadTestRequest;
import com.mohammed.loadsimulator.dto.LoadTestResult;

/**
 * Contract for the load-test execution engine. Not wired in the MVP; the service
 * returns a hardcoded result until this is implemented.
 */
public interface LoadTestEngine {

	LoadTestResult execute(LoadTestRequest request);
}
