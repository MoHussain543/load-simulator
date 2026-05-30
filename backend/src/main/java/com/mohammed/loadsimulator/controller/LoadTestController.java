package com.mohammed.loadsimulator.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mohammed.loadsimulator.dto.LoadTestRequest;
import com.mohammed.loadsimulator.dto.LoadTestResult;
import com.mohammed.loadsimulator.service.LoadTestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class LoadTestController {

	private final LoadTestService loadTestService;

	public LoadTestController(LoadTestService loadTestService) {
		this.loadTestService = loadTestService;
	}

	@PostMapping("/load-test")
	public LoadTestResult runLoadTest(@Valid @RequestBody LoadTestRequest request) {
		return loadTestService.runLoadTest(request);
	}
}
