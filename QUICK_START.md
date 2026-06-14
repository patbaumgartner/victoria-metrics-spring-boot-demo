# Quick Start - VictoriaMetrics + Logs + Traces Demo

## Start

```bash
chmod +x start-observability.sh
./start-observability.sh
```

Or:

```bash
docker compose up -d --build
```

## What Runs

- VictoriaMetrics (`8428`) for metrics
- VictoriaLogs (`9428`) for logs
- VictoriaTraces (`9429`) for trace query UI/API
- OpenTelemetry Collector (`4317`, `4318`, `13133`) for ingest/routing
- Grafana (`3000`) for unified visualization
- Spring Boot demo app (`8080`) generating telemetry

## Access

- Grafana: http://localhost:3000 (`admin` / `admin123`)
- Spring Boot app: http://localhost:8080
- VictoriaMetrics VMUI/API: http://localhost:8428
- VictoriaLogs UI/API: http://localhost:9428
- VictoriaTraces query API: http://localhost:9429
- OTel Collector health: http://localhost:13133

## Smoke Test

```bash
curl http://localhost:8080/api/hello
curl http://localhost:8080/api/data
curl -X POST http://localhost:8080/api/process -H "Content-Type: application/json" -d '{"value":"demo"}'
curl http://localhost:8080/api/slow-operation
curl http://localhost:8080/api/error
```

## Generate Demo Traffic

```bash
while true; do
  curl -s http://localhost:8080/api/hello > /dev/null
  curl -s http://localhost:8080/api/data > /dev/null
  curl -s -X POST http://localhost:8080/api/process \
    -H "Content-Type: application/json" \
    -d '{"value":"load-test"}' > /dev/null
  sleep 1
done
```

## Grafana Datasources

Provisioned automatically using the official VictoriaMetrics Grafana plugins:

- VictoriaMetrics (`victoriametrics-metrics-datasource`, default)
- VictoriaLogs (`victoriametrics-logs-datasource`)
- VictoriaTraces (`jaeger` — VictoriaTraces exposes a Jaeger-compatible query API)

## Troubleshooting

### No metrics

```bash
docker compose logs otel-collector | grep -i -E "error|metric|export"
curl -s http://localhost:13133
curl -s http://localhost:8428/health
```

### No logs

```bash
docker compose logs otel-collector | grep -i -E "error|log|victorialogs"
curl -s http://localhost:9428/health
```

### No traces

```bash
docker compose logs otel-collector | grep -i -E "error|trace|victoriatraces"
curl -s http://localhost:9429/health
```

### Reset everything

```bash
docker compose down -v
```

## Version Strategy

This demo intentionally uses `:latest` images for Victoria components, Grafana, and OTel Collector so you are always on the newest stable container versions available at pull time.
