# Contract First agent guide

- Read `CONTEXT.md` and `.mjga/capabilities.json` before changing domain names or module boundaries.
- Collaborate through a Capability's `api` Named Interface or a Versioned Domain Event.
- Never import another Capability's internal, Adapter, persistence, generated jOOQ, or Jimmer types.
- Run `./gradlew check` before handoff. Use the named diagnostic tasks for focused failures.
- Never edit generated persistence sources or `.mjga/capabilities.json` directly.
- Prefer MJGA Contract Authoring plans for use cases, events, Workflows, Read Models, and Consumer Contracts; inspect the plan before using `--apply`.
- Use `mjga assess extraction` and `mjga plan capability-migration` for destructive architecture changes. These commands deliberately do not apply changes.
