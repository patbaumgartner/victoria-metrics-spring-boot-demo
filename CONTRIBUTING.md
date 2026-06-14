# Contributing

Thanks for your interest in improving this project! This is a demonstration
repository, but contributions that make it clearer, more correct, or more useful
as a learning resource are very welcome.

## Ways to Contribute

- Report bugs or unexpected behavior via [issues](https://github.com/patbaumgartner/victoria-metrics-spring-boot-demo/issues).
- Suggest improvements to the observability setup, dashboards, or docs.
- Submit pull requests for fixes or enhancements.

## Development Setup

**Prerequisites:** JDK 21, Docker, and Docker Compose v2.

```bash
git clone https://github.com/patbaumgartner/victoria-metrics-spring-boot-demo.git
cd victoria-metrics-spring-boot-demo

# Run the full stack
docker compose up -d --build

# Or build/test just the app
cd app
./mvnw clean verify
```

## Pull Request Guidelines

1. **Fork** the repository and create a feature branch from `main`:
   ```bash
   git checkout -b feature/short-description
   ```
2. **Keep changes focused.** One logical change per pull request.
3. **Build and test** before submitting:
   ```bash
   cd app && ./mvnw clean verify
   ```
4. **Verify the stack still runs** end to end if you touch the compose file,
   collector config, or telemetry wiring:
   ```bash
   docker compose up -d --build
   # generate traffic, then confirm metrics/logs/traces appear in Grafana
   ```
5. **Update documentation** (`README.md`, `QUICK_START.md`, etc.) when behavior
   or configuration changes.
6. **Write clear commit messages.** A short imperative summary line
   (e.g. `Fix log export by installing OpenTelemetry appender`) is ideal.

## Coding Conventions

- Java code targets **JDK 21** and follows standard Spring Boot conventions.
- Keep configuration declarative in `application.yml` where Spring Boot supports it.
- Prefer minimal, well-commented changes over broad refactors.
- YAML and shell scripts use **LF** line endings (enforced via `.gitattributes`).

## Reporting Security Issues

Please do **not** open public issues for security vulnerabilities. Follow the
process described in [SECURITY.md](SECURITY.md).

## Code of Conduct

By participating, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).
