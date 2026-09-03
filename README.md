# Contract First Kotlin service

This is an idiomatic Kotlin Human-Agent Ready Scaffold with no fictitious business domain.

```bash
docker compose up -d database
./gradlew check
mjga add capability order-management --apply
```

Capability APIs are the only business calling surface. PostgreSQL schemas, migrations, transactions, persistence types, and event recovery remain owned by their declaring Capability or the MJGA platform baseline.

Continue the Typed Contract Chain with preview-first Contract Authoring:

```bash
mjga add use-case order-management create-order --kind command --transport http --apply
mjga add event order-management order-created --apply
mjga evolve event order-management order-created --version 2 --apply
mjga add workflow order-fulfillment --apply
mjga add read-model order-overview --consume order-management.order-created.v2 --apply
mjga workbench .
```

Use `mjga workbench . --serve` with `MJGA_MANAGEMENT_SERVER`, `MJGA_MANAGEMENT_USER`, and `MJGA_MANAGEMENT_PASSWORD` to inspect redacted recovery summaries without exposing payloads.

`mjga dev mock` serves the OpenAPI contract locally, while `mjga dev mcp` exposes the same read and preview Interfaces to a coding Agent. Database-role hardening, Consumer Contracts, Capability migration planning, and extraction assessment are available through `mjga --help`. Run `mjga verify .` after applying a plan.
