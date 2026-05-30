package com.mohammed.loadsimulator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoadTestControllerValidationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void postLoadTestReturnsBadRequestForInvalidBody() throws Exception {
		mockMvc.perform(post("/api/load-test")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "url": "",
						  "method": "GET",
						  "virtualUsers": 1,
						  "durationSeconds": 5
						}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.url").value("url must not be blank"));
	}
}
