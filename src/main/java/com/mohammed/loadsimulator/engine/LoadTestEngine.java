package com.mohammed.loadsimulator.engine;

import com.mohammed.loadsimulator.dto.LoadTestRequest;
import com.mohammed.loadsimulator.dto.LoadTestResult;

public interface LoadTestEngine {

	LoadTestResult execute(LoadTestRequest request);
}
