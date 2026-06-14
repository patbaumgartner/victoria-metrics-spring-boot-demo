# VictoriaMetrics Observability Stack (Logs + Metrics + Traces)

This demo is a unified OpenTelemetry pipeline with Victoria backends and Grafana visualization.

## Architecture

```text
Spring Boot App
  -> OTLP/HTTP -> OpenTelemetry Collector
       -> Metrics -> VictoriaMetrics (/opentelemetry/v1/metrics)
       -> Logs    -> VictoriaLogs (/insert/opentelemetry/v1/logs)
       -> Traces  -> VictoriaTraces (/insert/opentelemetry/v1/traces)

Grafana reads from all three backends.
```

## Why this is better

- One collector for all signals
- No redundant scrape/storage layer
- Native OTel routes for each Victoria backend
- Stable local ports with no collisions
- Grafana datasource UIDs wired for cross-navigation

## Container layout

- `victoriametrics/victoria-metrics:latest`
- `victoriametrics/victoria-logs:latest`
- `victoriametrics/victoria-traces:latest`
- `otel/opentelemetry-collector-contrib:latest`
- `grafana/grafana:latest`
- Spring Boot demo app

## Port map

- `3000` Grafana
- `8080` Demo app
- `8428` VictoriaMetrics
- `9428` VictoriaLogs
- `9429` VictoriaTraces query API
- `4317` OTel Collector OTLP/gRPC ingest
- `4318` OTel Collector OTLP/HTTP ingest (used by the demo app)
- `13133` OTel Collector health

## Key config choices

- Metrics export uses `otlp_http/metrics`:
  - `metrics_endpoint: http://victoria-metrics:8428/opentelemetry/v1/metrics`
- Logs export uses `otlp_http/logs`:
  - `logs_endpoint: http://victoria-logs:9428/insert/opentelemetry/v1/logs`
- Traces export uses `otlp_http/traces`:
  - `traces_endpoint: http://victoria-traces:9428/insert/opentelemetry/v1/traces`
- Collector pipeline kept minimal: `memory_limiter` + `batch` processors only
- Micrometer exports cumulative OTLP metrics, which VictoriaMetrics ingests directly

## Health model

- Docker healthchecks on core services
- OTel Collector extension health endpoint (`:13133`)
- Startup script checks all backend health endpoints before declaring ready

## Data persistence

Docker volumes:

- `victoria-metrics-data`
- `victoria-logs-data`
- `victoria-traces-data`
- `grafana-data`

## Operational commands

```bash
# start / rebuild
./start-observability.sh

# list status
docker compose ps

# tail collector logs
docker compose logs -f otel-collector

# stop
docker compose down

# stop + wipe state
docker compose down -v
```

## Source references

- VictoriaMetrics OpenTelemetry guide:
  - https://docs.victoriametrics.com/guides/getting-started-with-opentelemetry/
- VictoriaLogs OpenTelemetry ingestion:
  - https://docs.victoriametrics.com/victorialogs/data-ingestion/opentelemetry/
- OpenTelemetry Collector configuration:
  - https://opentelemetry.io/docs/collector/configuration/
