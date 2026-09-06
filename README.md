# Contract First Full-stack Scaffold

This is a runnable Human-Agent Ready Scaffold with no fictitious business domain. `apps/web` is a
React/Vite application, `apps/server` is an independently buildable Spring Boot application, and
`packages/api-client` is generated from `contracts/openapi/openapi.json`.

```sh
npm ci
docker compose up -d database
mjga add capability order-management --apply
mjga add use-case order-management create-order --kind command --transport http --apply
mjga verify .
npm run dev
```

Contract Authoring previews by default and changes files only with `--apply`. It supports
Capabilities, typed use cases, versioned events, Workflows, Read Models, database-role hardening,
and Consumer Contracts while preserving User-Owned Source. Run `mjga workbench .`, `mjga dev mock`,
or `mjga dev mcp` for local contract feedback. Extraction assessment and Capability migration are
plan-only operations; see `mjga help` for the complete command surface.

Use `npm run build`, `npm test`, and `npm run check` for individual workspace tasks. `mjga verify .`
checks the Web Application and Generated TypeScript Client before running the Server's complete
`clean projectHealth check bootJar` gate. `npm run compose:up` starts the complete local topology,
`npm run compose:verify` checks the same-origin status slice, and `npm run compose:down` stops it.
