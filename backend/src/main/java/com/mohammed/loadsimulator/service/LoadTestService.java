package com.mohammed.loadsimulator.service;

import org.springframework.stereotype.Service;

import com.mohammed.loadsimulator.dto.LoadTestRequest;
import com.mohammed.loadsimulator.dto.LoadTestResult;
import com.mohammed.loadsimulator.engine.LoadTestEngine;

@Service
public class LoadTestService {

	private final LoadTestEngine loadTestEngine;

	public LoadTestService(LoadTestEngine loadTestEngine) {
		this.loadTestEngine = loadTestEngine;
	}

	public LoadTestResult runLoadTest(LoadTestRequest request) {
		return loadTestEngine.execute(request);
	}
}
