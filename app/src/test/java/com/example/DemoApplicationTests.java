package com.example;

import com.example.support.InMemoryTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the application starts with the complete telemetry pipeline active.
 * <p>
 * {@link AutoConfigureMetrics} and {@link AutoConfigureTracing} are required: Spring Boot
 * disables metrics and tracing in tests by default, and without them this test would boot
 * a context that does not resemble production and would miss telemetry wiring failures.
 */
@SpringBootTest
@AutoConfigureMetrics
@AutoConfigureTracing
@ActiveProfiles("test")
@Import(InMemoryTelemetry.class)
class DemoApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(DemoApplicationTests.class);

	@Autowired
	private OpenTelemetry openTelemetry;

	@Autowired
	private InMemoryLogRecordExporter logRecords;

	@Test
	void startsWithTheOpenTelemetrySdkConfigured() {
		assertThat(this.openTelemetry).isInstanceOf(OpenTelemetrySdk.class);
	}

	/**
	 * Regression test for an OpenTelemetry version skew that made the application fail to
	 * start.
	 * <p>
	 * The Logback appender used to drag in a newer {@code opentelemetry-api-incubator}
	 * than the Spring Boot managed SDK, so recording a log record that carries an
	 * exception blew up with {@code NoClassDefFoundError:
	 * io/opentelemetry/api/incubator/common/ExtendedAttributeKey}. Asserting that a log
	 * record with a throwable reaches an exporter pins the whole bridge — Logback
	 * appender, SDK and incubator artifacts — to one working combination.
	 */
	@Test
	void bridgesLogbackEventsWithExceptionsIntoOpenTelemetry() {
		this.logRecords.reset();

		log.error("simulated failure", new IllegalStateException("boom"));

		assertThat(this.logRecords.getFinishedLogRecordItems()).anySatisfy((logRecord) -> {
			assertThat(logRecord.getBodyValue()).isNotNull();
			assertThat(logRecord.getBodyValue().asString()).isEqualTo("simulated failure");
			assertThat(logRecord.getSeverity()).isEqualTo(Severity.ERROR);
			assertThat(logRecord.getAttributes().get(AttributeKey.stringKey("exception.message"))).isEqualTo("boom");
		});
	}

}
