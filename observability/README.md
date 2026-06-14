# Observability Configuration

Provisioning and collector configuration for the unified Victoria observability stack.

## Components

| Service | Purpose | Local URL |
|---------|---------|-----------|
| VictoriaMetrics | Metrics storage (OTLP ingestion) | http://localhost:8428 |
| VictoriaLogs | Log storage (OTLP ingestion) | http://localhost:9428 |
| VictoriaTraces | Trace storage + Jaeger query API | http://localhost:9429 |
| Loki | Logs backend for Grafana Logs Drilldown | http://localhost:3100 |
| OpenTelemetry Collector | Receives OTLP and routes each signal | grpc `:4317` / http `:4318` / health `:13133` |
| Grafana | Unified visualization | http://localhost:3000 (admin / admin123) |

## Pipeline

```
Spring Boot App ──OTLP/HTTP──▶ OpenTelemetry Collector
                                   ├─ metrics ▶ VictoriaMetrics   /opentelemetry/v1/metrics
                                   ├─ logs    ▶ VictoriaLogs      /insert/opentelemetry/v1/logs
                                   ├─ logs    ▶ Loki              /otlp/v1/logs (Drilldown compatibility)
                                   └─ traces  ▶ VictoriaTraces    /insert/opentelemetry/v1/traces
                                                       │
                                                  Grafana (6 datasources)
```

## Files

- `otel-collector/otel-collector-config.yml` — OTLP gRPC/HTTP receivers, `memory_limiter` + `batch` processors, OTLP exporters for metrics/traces, and dual log exporters (VictoriaLogs + Loki).
- `grafana/provisioning/datasources/datasources.yml` — provisions Victoria datasources plus compatibility datasources for Prometheus/Tempo/Loki drilldown apps.
- `grafana/provisioning/dashboards/dashboards.yml` — dashboard provider that loads JSON dashboards from `dashboards/json/`.
- `grafana/provisioning/dashboards/json/` — `spring-boot-dashboard.json`, `reliability-dashboard.json`.

## Grafana Datasources

Provisioned with the official VictoriaMetrics Grafana plugins (installed via
`GF_INSTALL_PLUGINS` in `docker-compose.yml`):

| Name | Plugin / Type | URL |
|------|---------------|-----|
| VictoriaMetrics | `victoriametrics-metrics-datasource` (default) | http://victoria-metrics:8428 |
| VictoriaLogs | `victoriametrics-logs-datasource` | http://victoria-logs:9428 |
| VictoriaTraces | `jaeger` | http://victoria-traces:9428/select/jaeger |
| VictoriaMetrics (Prometheus API) | `prometheus` | http://victoria-metrics:8428/prometheus |
| VictoriaTraces (Tempo API) | `tempo` | http://victoria-traces:9428/select/tempo |
| Loki | `loki` | http://loki:3100 |

VictoriaTraces exposes a Jaeger-compatible query API, so the standard Grafana
Jaeger datasource is used for trace exploration.

## Sending Telemetry

Point any OpenTelemetry SDK (or the demo app) at the collector's OTLP/HTTP endpoint:

```
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318
```

Each SDK posts to `/v1/metrics`, `/v1/traces`, and `/v1/logs`; the collector routes
every signal to the matching Victoria backend.

## Dashboards

1. **Spring Boot Application Metrics** — HTTP request rates, latency percentiles, JVM memory.
2. **Application Reliability & SLOs** — error rate, availability, SLI tracking.

Dashboard panels query the **VictoriaMetrics** datasource using PromQL/MetricsQL.

## Maintenance

```bash
# Tail collector logs
docker compose logs -f otel-collector

# Restart Grafana after editing provisioning files
docker compose restart grafana

# Reset all stored data
docker compose down -v
```

## References

- VictoriaMetrics OpenTelemetry: https://docs.victoriametrics.com/guides/getting-started-with-opentelemetry/
- VictoriaLogs OTLP ingestion: https://docs.victoriametrics.com/victorialogs/data-ingestion/opentelemetry/
- VictoriaTraces: https://docs.victoriametrics.com/victoriatraces/
- OpenTelemetry Collector: https://opentelemetry.io/docs/collector/configuration/
