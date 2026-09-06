export default {
  mutate: [
    "src/**/*.ts",
    "src/**/*.tsx",
    "!src/**/*.test.*",
    "!src/routeTree.gen.ts",
  ],
  testRunner: "vitest",
  reporters: ["clear-text", "html", "json"],
  thresholds: { high: 100, low: 100, break: 100 },
  coverageAnalysis: "perTest",
};
