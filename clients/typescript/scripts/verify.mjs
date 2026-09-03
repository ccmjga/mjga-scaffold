import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const contract = JSON.parse(
  await readFile(path.resolve(root, "../../openapi/openapi.json"), "utf8"),
);
const client = await readFile(path.join(root, "src/index.ts"), "utf8");

assert.equal(contract.openapi, "3.1.0");
const operations = Object.values(contract.paths).flatMap((pathItem) =>
  Object.values(pathItem),
);
if (!operations.length) assert.match(client, /export \{\};/);
for (const operation of operations) {
  assert.match(operation.operationId, /^[a-z][A-Za-z0-9]+$/);
  assert.match(
    client,
    new RegExp(`export async function ${operation.operationId}\\(`),
  );
  for (const schema of [
    operation.requestBody?.content?.["application/json"]?.schema,
    operation.responses?.["200"]?.content?.["application/json"]?.schema,
  ]) {
    const name = schema?.$ref?.split("/").at(-1);
    if (name)
      assert.ok(
        contract.components.schemas[name],
        `Missing OpenAPI schema ${name}`,
      );
  }
}
console.log(
  `OpenAPI contract and ${operations.length} generated TypeScript operation(s) are current.`,
);
