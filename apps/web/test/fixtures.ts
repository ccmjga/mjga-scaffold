import { expect, test as baseTest } from "vitest";
import type { SetupWorker } from "msw/browser";
import { worker } from "./msw";

export const test = baseTest.extend<{ worker: SetupWorker }>({
  worker: [
    async ({}, use) => {
      await worker.start({
        onUnhandledRequest: "error",
        serviceWorker: { url: "/mockServiceWorker.js" },
      });
      await use(worker);
      worker.resetHandlers();
      worker.stop();
    },
    { auto: true },
  ],
});

export { expect };
