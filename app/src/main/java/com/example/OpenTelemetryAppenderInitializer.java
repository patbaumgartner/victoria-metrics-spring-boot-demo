package com.example;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Registers and installs the OpenTelemetry Logback appender programmatically so
 * application logs are exported over OTLP. Doing this in code keeps all
 * telemetry
 * configuration in {@code application.yml} and avoids a separate
 * {@code logback-spring.xml} file. Spring Boot's default console appender is
 * left
 * untouched; the OpenTelemetry appender is simply added to the root logger and
 * wired to the Spring-managed {@link OpenTelemetry} instance.
 */
@Component
class OpenTelemetryAppenderInitializer implements InitializingBean {

    private final OpenTelemetry openTelemetry;

    OpenTelemetryAppenderInitializer(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void afterPropertiesSet() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        OpenTelemetryAppender appender = new OpenTelemetryAppender();
        appender.setOpenTelemetry(this.openTelemetry);
        appender.setCaptureExperimentalAttributes(true);
        appender.setCaptureCodeAttributes(true);
        appender.setCaptureMdcAttributes("*");
        appender.setContext(loggerContext);
        appender.start();

        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(appender);
    }
}
