# Security Policy

## Supported Versions

This is a demonstration project maintained on a best-effort basis. Security
fixes are applied to the `main` branch only.

| Version | Supported |
|---------|-----------|
| `main`  | ✅ |

## Reporting a Vulnerability

If you discover a security vulnerability, please report it **privately** so it
can be addressed before public disclosure.

- Use GitHub's [private vulnerability reporting](https://github.com/patbaumgartner/victoria-metrics-spring-boot-demo/security/advisories/new)
  ("Report a vulnerability" under the **Security** tab), **or**
- Open a minimal issue asking to be contacted privately — **without** including
  exploit details.

Please include, where possible:

- A description of the vulnerability and its impact.
- Steps to reproduce or a proof of concept.
- Affected files, dependencies, or configuration.

You can expect an acknowledgement within a few days. Once a fix is available,
we will coordinate disclosure.

## Scope

This repository demonstrates an observability stack intended for **local and
development use**. The default credentials, open ports, and permissive settings
are **not** suitable for production. Hardening recommendations:

- Change the default Grafana credentials (`admin` / `admin123`).
- Restrict exposed ports and place services behind a network boundary.
- Enable authentication/TLS on the Victoria components and the OTLP endpoints.
- Pin container images to specific digests instead of `:latest`.
