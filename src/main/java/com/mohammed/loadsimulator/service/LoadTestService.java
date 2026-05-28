package com.mohammed.loadsimulator.service;

import org.springframework.stereotype.Service;

import com.mohammed.loadsimulator.dto.LoadTestRequest;
import com.mohammed.loadsimulator.dto.LoadTestResult;

@Service
public class LoadTestService {

	public LoadTestResult runLoadTest(LoadTestRequest request) {
		// MVP: hardcoded sample result to verify endpoint wiring and JSON contract.
		return new LoadTestResult(
				10_000L,
				9_850L,
				150L,
				42.5,
				8.0,
				320.0,
				95.0,
				166.67,
				0.015);
	}
}
