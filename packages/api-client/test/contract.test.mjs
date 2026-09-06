import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { test } from "vitest";

test("the generated client owns the platform status operation", async () => {
  const contract = JSON.parse(await readFile(new URL("../../../contracts/openapi/openapi.json", import.meta.url)));
  assert.equal(contract.paths["/api/v1/platform/status"].get.operationId, "getPlatformStatus");
});
