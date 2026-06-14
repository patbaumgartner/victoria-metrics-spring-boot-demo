# VictoriaMetrics Demo App

Spring Boot 4.1 application fully instrumented with OpenTelemetry for metrics, traces, and logs.

## Features

- ✅ **Spring Boot 4.1.0** — Latest Spring Boot with Java 21
- ✅ **OpenTelemetry Integration** — Automatic and manual instrumentation
- ✅ **OTLP Metrics** — Micrometer with OTLP registry
- ✅ **Distributed Tracing** — OTLP/HTTP export via the OpenTelemetry Collector
- ✅ **Spring Actuator** — Health, info, and metrics endpoints
- ✅ **Demo Endpoints** — Multiple endpoints to test observability

## API Endpoints

### Basic Endpoints

**GET /api/hello**
```bash
curl http://localhost:8080/api/hello
```
Returns a simple greeting with timestamp.

**GET /api/data**
```bash
curl http://localhost:8080/api/data
```
Simulates a database fetch operation (~100-300ms).

**POST /api/process**
```bash
curl -X POST http://localhost:8080/api/process \
  -H "Content-Type: application/json" \
  -d '{"value":"test data"}'
```
Processes input data (~150-400ms).

**GET /api/slow-operation**
```bash
curl http://localhost:8080/api/slow-operation
```
Deliberately slow operation (2 seconds) for testing latency tracking.

**GET /api/error**
```bash
curl http://localhost:8080/api/error
```
Throws a simulated error for testing error handling.

### Monitoring Endpoints

**GET /actuator**
```bash
curl http://localhost:8080/actuator
```
Lists all available actuator endpoints.

**GET /actuator/health**
```bash
curl http://localhost:8080/actuator/health
```
Application health status.

**GET /actuator/metrics**
```bash
curl http://localhost:8080/actuator/metrics
```
Available metrics.

## Build & Run Locally

### Prerequisites
- Java 21 (JDK)
- Maven 3.8+

### Build

```bash
cd app
mvn clean package
```

### Run Standalone

```bash
java -jar target/victoria-metrics-demo-1.0.0.jar
```

Or with environment variables:

```bash
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 \
java -jar target/victoria-metrics-demo-1.0.0.jar
```

### Run with Docker Compose

From the project root:

```bash
docker compose up -d spring-boot-app
```

## Environment Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` | OTLP/HTTP base endpoint of the OpenTelemetry Collector. Spring Boot appends `/v1/metrics`, `/v1/traces`, and `/v1/logs` for each signal. |

The service name is derived from `spring.application.name` (`victoria-metrics-demo`); no other OpenTelemetry environment variables are required.

## Instrumentation

### Automatic Instrumentation

The following are automatically instrumented by Spring Boot:

- HTTP Server Requests (all endpoints)
- HTTP Client Calls
- JVM Metrics (memory, GC, threads)
- Application Startup Metrics

### Custom Spans & Metrics

Service methods use Micrometer's `@Observed` annotation. Each observation is
recorded as a timer metric and bridged to an OpenTelemetry span:

```java
@Observed(name = "demo.fetch-data", contextualName = "fetch-data")
public String fetchData() {
    return "Sample data from database";
}
```

`@Observed` support is enabled via `management.observations.annotations.enabled=true`
and the `aspectjweaver` dependency.

## Metrics Generated

### HTTP Server Metrics

- `http_server_request_duration_seconds` - Request latency histogram
- `http_server_requests_total` - Total request count
- `http_server_request_active` - Active requests gauge

### JVM Metrics

- `jvm_memory_usage_bytes` - Memory usage
- `jvm_gc_pause_seconds` - GC pause duration
- `jvm_threads_live` - Live thread count
- `jvm_process_uptime_seconds` - Process uptime

### Custom Metrics

The application generates custom metrics via Spring Boot Actuator that appear in:

```
curl http://localhost:8080/actuator/metrics
```

## Traces Generated

All endpoints generate distributed traces with:

- Root span for HTTP request
- Child spans for service operations
- Custom attributes and events
- Error tracking and exceptions

View traces in:
1. Grafana → VictoriaTraces datasource
2. VictoriaTraces UI (if exposed)

## Performance Testing

### Load Test with Apache Bench

```bash
# 1000 requests, 10 concurrent
ab -n 1000 -c 10 http://localhost:8080/api/hello

# With custom data
ab -n 1000 -c 10 -p data.json -T application/json http://localhost:8080/api/process
```

### Load Test with wrk

```bash
wrk -t4 -c100 -d30s http://localhost:8080/api/hello
```

### Generate Traffic for Observability Demo

```bash
#!/bin/bash
while true; do
    curl -s http://localhost:8080/api/hello > /dev/null
    curl -s http://localhost:8080/api/data > /dev/null
    curl -s -X POST http://localhost:8080/api/process \
        -H "Content-Type: application/json" \
        -d '{"value":"test"}' > /dev/null
    sleep 1
done
```

## Dependencies

- **Spring Boot 4.1.0** - Web framework
- **spring-boot-starter-actuator** - Production-ready endpoints & observability
- **spring-boot-starter-micrometer-metrics** - Metrics integration
- **spring-boot-starter-opentelemetry** - OTLP trace export & SDK log pipeline (Micrometer Tracing bridge)
- **micrometer-registry-otlp** - OTLP metric export
- **opentelemetry-logback-appender-1.0** - Bridges Logback logs into OpenTelemetry for OTLP log export
- **aspectjweaver** - Enables `@Observed` annotation support

## Troubleshooting

### No traces appearing

1. Verify OTel Collector is running:
   ```bash
   docker compose logs otel-collector
   ```

2. Check environment variables:
   ```bash
   docker compose exec spring-boot-app env | grep OTEL
   ```

3. Verify collector health:
   ```bash
   docker compose exec spring-boot-app curl -v http://otel-collector:13133
   ```

### High memory usage

- Adjust batch size in OTel Collector config
- Reduce trace sampling rate in application.yml
- Limit metrics cardinality

### Metrics not appearing in VictoriaMetrics

1. Check available app metric names:
   ```bash
   curl http://localhost:8080/actuator/metrics
   ```

2. Check OTel Collector export logs:
   ```bash
   docker compose logs otel-collector | grep -i -E "metric|export|error"
   ```

## Configuration Files

- **pom.xml** - Maven dependencies and build config
- **Dockerfile** - Multi-stage build for production
- **application.yml** - Spring Boot + OpenTelemetry config
- **DemoApplication.java** - Main application class
- **OpenTelemetryAppenderInitializer.java** - Registers the OpenTelemetry Logback appender programmatically for OTLP log export
- **DemoController.java** - REST endpoints with traces
- **DemoService.java** - Business logic with spans

## Next Steps

1. **Add Custom Metrics** - Implement micrometer `MeterRegistry`
2. **Add Database Layer** - Use Spring Data JPA with tracing
3. **Add Tests** - Unit and integration tests with observability
4. **Performance Optimization** - Analyze traces to find bottlenecks

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [OpenTelemetry Java](https://opentelemetry.io/docs/instrumentation/java/)
- [Micrometer OTLP](https://docs.micrometer.io/micrometer/reference/implementations/otlp.html)
- [OpenTelemetry Spring Boot Starter](https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/spring/spring-boot-autoconfigure)
