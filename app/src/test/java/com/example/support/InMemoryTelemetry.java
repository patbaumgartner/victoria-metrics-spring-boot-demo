package com.example.support;

import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Captures OpenTelemetry signals in memory so tests can assert on telemetry that
 * genuinely travelled through the SDK, rather than mocking it away.
 * <p>
 * Signals are exported through {@code Simple*Processor} implementations so every record
 * is handed to the exporter synchronously — assertions never have to wait for a batch
 * flush, which keeps the tests fast and free of timing flakiness.
 */
@TestConfiguration(proxyBeanMethods = false)
public class InMemoryTelemetry {

	@Bean
	InMemoryLogRecordExporter inMemoryLogRecordExporter() {
		return InMemoryLogRecordExporter.create();
	}

	@Bean
	LogRecordProcessor inMemoryLogRecordProcessor(InMemoryLogRecordExporter exporter) {
		return SimpleLogRecordProcessor.create(exporter);
	}

	@Bean
	InMemorySpanExporter inMemorySpanExporter() {
		return InMemorySpanExporter.create();
	}

	@Bean
	SpanProcessor inMemorySpanProcessor(InMemorySpanExporter exporter) {
		return SimpleSpanProcessor.create(exporter);
	}

}
