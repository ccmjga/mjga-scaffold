import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { TanStackRouterVite } from "@tanstack/router-plugin/vite";
import tailwindcss from "@tailwindcss/vite";
import type { Plugin } from "vite";

function rejectTestOnlyProductionModules(): Plugin {
  return {
    name: "reject-test-only-production-modules",
    generateBundle(_options, bundle) {
      for (const output of Object.values(bundle)) {
        if (output.type !== "chunk") continue;
        const forbidden = Object.keys(output.modules).find((moduleId) =>
          /[\\/]node_modules[\\/](?:@faker-js[\\/]faker|msw)[\\/]/.test(
            moduleId,
          ),
        );
        if (forbidden)
          this.error(
            `Production bundle contains a test-only module: ${forbidden}`,
          );
      }
    },
  };
}

export default defineConfig({
  plugins: [
    tailwindcss(),
    TanStackRouterVite({ target: "react", autoCodeSplitting: true }),
    react(),
    rejectTestOnlyProductionModules(),
  ],
  server: {
    port: 5173,
    proxy: { "/api": { target: "http://127.0.0.1:8080", changeOrigin: false } },
  },
});
