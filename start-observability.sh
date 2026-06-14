#!/bin/bash

# VictoriaMetrics Observability Stack - Setup Script

set -e

echo "🚀 Starting VictoriaMetrics Observability Stack..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${YELLOW}⚠️  Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

if ! docker compose version &> /dev/null; then
    echo -e "${YELLOW}⚠️  Docker Compose v2 is not available. Please install Docker Compose first.${NC}"
    exit 1
fi

echo -e "${BLUE}✓ Docker and Docker Compose are installed${NC}"

# Navigate to script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo -e "${BLUE}📁 Project directory: $(pwd)${NC}"

# Pull latest images
echo -e "${BLUE}📥 Pulling latest Docker images...${NC}"
docker compose pull

# Start services
echo -e "${BLUE}🐳 Starting services...${NC}"
docker compose up -d

# Wait for services to be healthy
echo -e "${BLUE}⏳ Waiting for services to be healthy...${NC}"

services=("victoria-metrics" "victoria-logs" "victoria-traces" "grafana" "otel-collector" "spring-boot-app")

for service in "${services[@]}"; do
    echo -n "  Checking $service..."
    for i in {1..30}; do
        if docker compose exec -T "$service" echo "ok" &> /dev/null; then
            echo -e " ${GREEN}✓${NC}"
            break
        fi
        sleep 1
    done
done

# Wait for specific readiness checks
echo -e "${BLUE}🔍 Running health checks...${NC}"

# VictoriaMetrics
echo -n "  VictoriaMetrics readiness..."
until curl -sf http://localhost:8428/health > /dev/null 2>&1; do sleep 1; done
echo -e " ${GREEN}✓${NC}"

# VictoriaLogs
echo -n "  VictoriaLogs readiness..."
until curl -sf http://localhost:9428/health > /dev/null 2>&1; do sleep 1; done
echo -e " ${GREEN}✓${NC}"

# VictoriaTraces
echo -n "  VictoriaTraces readiness..."
until curl -sf http://localhost:9429/health > /dev/null 2>&1; do sleep 1; done
echo -e " ${GREEN}✓${NC}"

# OpenTelemetry Collector
echo -n "  OTel Collector readiness..."
until curl -sf http://localhost:13133/ > /dev/null 2>&1; do sleep 1; done
echo -e " ${GREEN}✓${NC}"

# Grafana
echo -n "  Grafana readiness..."
until curl -sf http://localhost:3000/api/health > /dev/null 2>&1; do sleep 1; done
echo -e " ${GREEN}✓${NC}"

# Spring Boot App
echo -n "  Spring Boot App readiness..."
until curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; do sleep 1; done
echo -e " ${GREEN}✓${NC}"

# Display endpoints
echo ""
echo -e "${GREEN}✅ VictoriaMetrics Stack is ready!${NC}"
echo ""
echo -e "${BLUE}📊 Access your services at:${NC}"
echo -e "  ${GREEN}Grafana:${NC}                  http://localhost:3000 (admin / admin123)"
echo -e "  ${GREEN}Spring Boot App:${NC}          http://localhost:8080"
echo -e "  ${GREEN}VictoriaMetrics:${NC}          http://localhost:8428"
echo -e "  ${GREEN}VictoriaLogs:${NC}             http://localhost:9428"
echo -e "  ${GREEN}VictoriaTraces:${NC}           http://localhost:9429"
echo -e "  ${GREEN}OTel Collector:${NC}           grpc://localhost:4317, http://localhost:4318, health://localhost:13133"
echo ""
echo -e "${BLUE}📚 Documentation:${NC} Check QUICK_START.md or observability/README.md"
echo -e "${BLUE}🛑 To stop the stack:${NC} docker compose down"
echo -e "${BLUE}🗑️  To reset data:${NC} docker compose down -v"
