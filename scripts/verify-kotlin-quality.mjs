#!/usr/bin/env node

import { readFile } from "node:fs/promises";

const [coveragePath, mutationPath] = process.argv.slice(2);
if (!coveragePath || !mutationPath) {
  throw new Error(
    "Usage: verify-kotlin-quality.mjs <kover-xml> <pitest-mutations-xml>",
  );
}

const [coverageXml, mutationXml] = await Promise.all([
  readFile(coveragePath, "utf8"),
  readFile(mutationPath, "utf8"),
]);

function counter(body, type) {
  const match = body.match(
    new RegExp(`<counter type=["']${type}["'] missed=["'](\\d+)["'] covered=["'](\\d+)["']\\s*/>`),
  );
  return match
    ? { missed: Number(match[1]), covered: Number(match[2]) }
    : { missed: 0, covered: 0 };
}

const crapFailures = [];
let analyzedMethods = 0;
for (const method of coverageXml.matchAll(
  /<method name=["']([^"']+)["'] desc=["']([^"']+)["'][^>]*>([\s\S]*?)<\/method>/g,
)) {
  const lines = counter(method[3], "LINE");
  const complexity = counter(method[3], "COMPLEXITY");
  const branches = counter(method[3], "BRANCH");
  const lineTotal = lines.missed + lines.covered;
  const reportedComplexity = complexity.missed + complexity.covered;
  const cyclomatic =
    reportedComplexity > 0
      ? reportedComplexity
      : branches.missed + branches.covered + 1;
  if (lineTotal === 0 || cyclomatic === 0) continue;
  analyzedMethods += 1;
  const coverage = lines.covered / lineTotal;
  const crap = cyclomatic ** 2 * (1 - coverage) ** 3 + cyclomatic;
  if (crap > 30) {
    crapFailures.push(
      `${method[1]}${method[2]} has CRAP ${crap.toFixed(2)} (maximum 30)`,
    );
  }
}
if (analyzedMethods === 0) {
  throw new Error("Kover report did not contain any analyzed Kotlin methods");
}

const statuses = {};
for (const mutation of mutationXml.matchAll(
  /<mutation\s+[^>]*status=["']([^"']+)["'][^>]*>/g,
)) {
  statuses[mutation[1]] = (statuses[mutation[1]] ?? 0) + 1;
}
const killed = statuses.KILLED ?? 0;
const survived = statuses.SURVIVED ?? 0;
const noCoverage = statuses.NO_COVERAGE ?? 0;
const scored = killed + survived + noCoverage;
const covered = killed + survived;
if (scored === 0) {
  throw new Error("PIT report did not contain any scored Kotlin mutations");
}
const mutationScore = (killed / scored) * 100;
const testStrength = covered === 0 ? 100 : (killed / covered) * 100;
const mutationFailures = [];
if (mutationScore + 1e-9 < 41.1) {
  mutationFailures.push(
    `mutation score ${mutationScore.toFixed(2)}% is below 41.1%`,
  );
}
if (testStrength + 1e-9 < 61.9) {
  mutationFailures.push(
    `test strength ${testStrength.toFixed(2)}% is below 61.9%`,
  );
}
if (survived > 27) {
  mutationFailures.push(`${survived} mutants survived; maximum is 27`);
}
if (noCoverage > 36) {
  mutationFailures.push(`${noCoverage} mutants have no coverage; maximum is 36`);
}

const failures = [...crapFailures, ...mutationFailures];
if (failures.length > 0) {
  console.error(failures.map((failure) => `Kotlin quality gate: ${failure}`).join("\\n"));
  process.exitCode = 1;
} else {
  console.log(
    `Kotlin quality gate passed: ${analyzedMethods} CRAP-analyzed methods; mutation ${mutationScore.toFixed(2)}%; test strength ${testStrength.toFixed(2)}%.`,
  );
}
