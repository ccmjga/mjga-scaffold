import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import { playwright } from "@vitest/browser-playwright";

export default defineConfig({
  plugins: [react()],
  publicDir: "test/public",
  optimizeDeps: {
    include: [
      "@faker-js/faker",
      "@tanstack/react-query",
      "@tanstack/react-router",
      "i18next",
      "react-dom/client",
      "vitest-browser-react",
      "zod",
    ],
  },
  test: {
    projects: [
      {
        test: {
          name: "unit",
          environment: "node",
          include: ["src/**/*.test.ts"],
        },
      },
      {
        plugins: [react()],
        publicDir: "test/public",
        optimizeDeps: {
          include: ["i18next", "react-dom/client"],
        },
        test: {
          name: "browser",
          include: ["src/**/*.browser.test.tsx"],
          browser: {
            enabled: true,
            provider: playwright(),
            instances: [{ browser: "chromium" }],
          },
        },
      },
    ],
  },
});
