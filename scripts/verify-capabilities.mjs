import assert from "node:assert/strict";
import { existsSync, readFileSync } from "node:fs";

const recipe = readFileSync("mjga-project.yaml", "utf8");
const group = /^\s{2}group:\s*(\S+)$/m.exec(recipe)?.[1];
const persistence = /^\s{2}persistence:\s*(\S+)$/m.exec(recipe)?.[1];
assert.match(group ?? "", /^[a-z][a-z0-9]*(?:\.[a-z][a-z0-9]*)+$/);
assert.equal(persistence, "jimmer");

const declarations = [];
const section = recipe.split(/^capabilityModules:\s*/m)[1] ?? "[]";
for (const match of section.matchAll(
  /^\s*- id:\s*(\S+)\s*\n\s+package(?:Name)?:\s*(\S+)\s*\n\s+schema:\s*(\S+)/gm,
))
  declarations.push({ id: match[1], package: match[2], schema: match[3] });
declarations.sort((left, right) => left.id.localeCompare(right.id));

const manifest = JSON.parse(readFileSync(".mjga/capabilities.json", "utf8"));
assert.equal(manifest.schemaVersion, 1);
assert.equal(manifest.architecture, "contractfirst");
assert.equal(manifest.persistence, persistence);
assert.deepEqual(
  manifest.capabilities.map(({ id, package: packageName, schema }) => ({
    id,
    package: packageName,
    schema,
  })),
  declarations,
  "Capability identities drifted from the Recipe",
);

const root = `src/main/java/${group.replaceAll(".", "/")}`;
for (const capability of manifest.capabilities)
  verifyCapability(capability, root, group);

function verifyCapability(capability, root, groupName) {
  const type = capability.id.split("-").map(capitalize).join("");
  assert.ok(
    existsSync(`${root}/${capability.package}/api/${type}.java`),
    `Missing Capability entry Interface for ${capability.id}`,
  );
  assert.ok(
    existsSync(`src/main/resources/db/capability/${capability.schema}`),
    `Missing owned schema migration directory for ${capability.id}`,
  );
  assert.equal(
    capability.entryInterface,
    `${groupName}.${capability.package}.api.${type}`,
  );
  assert.deepEqual(
    [...new Set(capability.permissions ?? [])],
    capability.permissions ?? [],
    `Duplicate Permission ID in ${capability.id}`,
  );
  for (const useCase of capability.useCases ?? []) {
    assert.ok(
      (capability.permissions ?? []).includes(useCase.permission),
      `Use Case ${useCase.id} lacks its Permission ID`,
    );
    assert.ok(
      existsSync(
        `${root}/${capability.package}/api/${useCase.requestType}.java`,
      ),
      `Missing request contract for ${useCase.id}`,
    );
    assert.ok(
      existsSync(
        `${root}/${capability.package}/api/${useCase.outcomeType}.java`,
      ),
      `Missing outcome contract for ${useCase.id}`,
    );
  }
  for (const event of capability.events?.published ?? []) {
    assert.ok(
      event.version > 0 && existsSync(event.schema),
      `Missing versioned Event Schema ${event.type}`,
    );
    const schema = JSON.parse(readFileSync(event.schema, "utf8"));
    assert.equal(schema["x-mjga-event-type"], event.type);
    assert.equal(schema["x-mjga-schema-version"], event.version);
  }
  if (capability.kind === "read-model")
    assert.equal(
      capability.recovery?.strategy,
      "snapshot-watermark",
      `Read Model ${capability.id} lacks recovery`,
    );
  if (capability.kind === "workflow") {
    assert.equal(
      capability.workflow?.state,
      "explicit",
      `Workflow ${capability.id} lacks explicit state`,
    );
    assert.equal(
      capability.workflow?.terminationTest,
      "required",
      `Workflow ${capability.id} lacks termination evidence`,
    );
  }
}

function capitalize(value) {
  return value[0].toUpperCase() + value.slice(1);
}
