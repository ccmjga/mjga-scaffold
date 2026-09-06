import { spawnSync } from "node:child_process";
import { cp, mkdir, mkdtemp, readFile, readdir, rm, symlink } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";

const clientRoot = path.resolve(import.meta.dirname, "..");
const workspaceRoot = path.resolve(clientRoot, "../..");
const temporaryRoot = await mkdtemp(path.join(tmpdir(), "mjga-kubb-check-"));

try {
  await mkdir(path.join(temporaryRoot, "packages/api-client"), { recursive: true });
  await cp(path.join(clientRoot, "kubb.config.ts"), path.join(temporaryRoot, "packages/api-client/kubb.config.ts"), { recursive: false });
  await cp(path.join(workspaceRoot, "contracts"), path.join(temporaryRoot, "contracts"), { recursive: true });
  await symlink(
    path.join(workspaceRoot, "node_modules"),
    path.join(temporaryRoot, "node_modules"),
    process.platform === "win32" ? "junction" : "dir",
  );
  const executable = path.join(
    workspaceRoot,
    `node_modules/.bin/kubb${process.platform === "win32" ? ".cmd" : ""}`,
  );
  const result = spawnSync(
    executable,
    ["generate", "--config", "kubb.config.ts"],
    {
      cwd: path.join(temporaryRoot, "packages/api-client"),
      encoding: "utf8",
      env: { ...process.env, KUBB_DISABLE_TELEMETRY: "1" },
    },
  );
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error((result.stderr || result.stdout).trim());
  await compareTrees(
    path.join(clientRoot, "src/generated"),
    path.join(temporaryRoot, "packages/api-client/src/generated"),
  );
  console.log("Kubb generated client is current.");
} finally {
  await rm(temporaryRoot, { recursive: true, force: true });
}

async function compareTrees(expectedRoot, actualRoot) {
  const expectedFiles = await files(expectedRoot);
  const actualFiles = await files(actualRoot);
  if (JSON.stringify(expectedFiles) !== JSON.stringify(actualFiles)) {
    throw new Error("Kubb generated client file list drifted");
  }
  for (const file of expectedFiles) {
    const [expected, actual] = await Promise.all([
      readFile(path.join(expectedRoot, file)),
      readFile(path.join(actualRoot, file)),
    ]);
    if (!expected.equals(actual)) throw new Error(`Kubb generated client drifted: ${file}`);
  }
}

async function files(root, current = root) {
  const result = [];
  for (const entry of await readdir(current, { withFileTypes: true })) {
    const absolute = path.join(current, entry.name);
    if (entry.isDirectory()) result.push(...(await files(root, absolute)));
    else if (entry.isFile()) result.push(path.relative(root, absolute));
  }
  return result.sort();
}
