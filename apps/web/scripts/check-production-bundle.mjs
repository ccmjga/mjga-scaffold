import { readFile, readdir } from "node:fs/promises";
import path from "node:path";

const outputRoot = path.resolve(import.meta.dirname, "../dist");
const forbidden = /@faker-js\/faker|Mock Service Worker|setupWorker/;

for (const file of await files(outputRoot)) {
  if (path.basename(file) === "mockServiceWorker.js") {
    throw new Error("Production bundle contains the MSW Service Worker");
  }
  if (
    /\.(?:html|js|css)$/.test(file) &&
    forbidden.test(await readFile(file, "utf8"))
  ) {
    throw new Error(
      `Production bundle contains test-only mock code: ${path.relative(outputRoot, file)}`,
    );
  }
}

console.log("Production bundle excludes MSW and Faker test assets.");

async function files(directory) {
  const result = [];
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) result.push(...(await files(absolute)));
    else if (entry.isFile()) result.push(absolute);
  }
  return result;
}
