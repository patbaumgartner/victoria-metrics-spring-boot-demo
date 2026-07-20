package com.example.service;

import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class DemoService {

	private static final Logger log = LoggerFactory.getLogger(DemoService.class);

	@Observed(name = "demo.fetch-data", contextualName = "fetch-data")
	public String fetchData() {
		log.info("Fetching data");
		sleep(100 + ThreadLocalRandom.current().nextInt(200));
		return "Sample data from database";
	}

	@Observed(name = "demo.process-data", contextualName = "process-data")
	public String processData(String input) {
		log.info("Processing data: {}", input);
		sleep(150 + ThreadLocalRandom.current().nextInt(250));
		return "Processed: " + input.toUpperCase();
	}

	@Observed(name = "demo.slow-operation", contextualName = "slow-operation")
	public void slowOperation(long durationMs) {
		log.info("Starting slow operation for {} ms", durationMs);
		sleep(durationMs);
		log.info("Slow operation completed");
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
