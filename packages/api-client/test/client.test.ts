import { fakerEN } from "@faker-js/faker";
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from "vitest";
import { setupServer } from "msw/node";
import { QueryClient } from "@tanstack/react-query";
import { getPlatformStatus, getPlatformStatusQueryOptions } from "../src/index";
import {
  createPlatformStatus,
  getPlatformStatusHandler,
  getPlatformStatusHandlerResponse200,
  getPlatformStatusHandlerResponse500,
} from "../src/mocks";

const server = setupServer();
const clientOptions = { baseURL: "http://localhost" };

beforeAll(() => server.listen({ onUnhandledRequest: "error" }));
beforeEach(() => {
  fakerEN.seed(20260905);
  vi.setSystemTime(new Date("2026-09-05T00:00:00.000Z"));
});
afterEach(() => {
  server.resetHandlers();
  vi.useRealTimers();
});
afterAll(() => server.close());

describe("Generated TypeScript Client", () => {
  it("returns a validated platform status through its stable interface", async () => {
    const status = createPlatformStatus({
      status: "UP",
      application: "orders",
      version: "1.2.3",
    });
    server.use(getPlatformStatusHandler(status));

    await expect(getPlatformStatus(clientOptions)).resolves.toEqual(status);
  });

  it("maps Problem Details to a stable error", async () => {
    server.use(
      getPlatformStatusHandler(() =>
        getPlatformStatusHandlerResponse500({
          type: "https://example.test/problems/unavailable",
          title: "Unavailable",
          status: 500,
          code: "platform-unavailable",
        }),
      ),
    );

    await expect(getPlatformStatus(clientOptions)).rejects.toThrow(
      "Platform status failed (platform-unavailable)",
    );
  });

  it("rejects malformed success payloads", async () => {
    server.use(
      getPlatformStatusHandler({
        status: "UP",
        application: "orders",
        version: "not-semver",
      }),
    );

    await expect(getPlatformStatus(clientOptions)).rejects.toThrow();
  });

  it("reuses the generated query key through one QueryClient", async () => {
    let calls = 0;
    const status = createPlatformStatus({
      status: "UP",
      application: "orders",
      version: "1.2.3",
    });
    server.use(
      getPlatformStatusHandler(() => {
        calls += 1;
        return getPlatformStatusHandlerResponse200(status);
      }),
    );
    const queryClient = new QueryClient();
    const options = getPlatformStatusQueryOptions(clientOptions);

    await queryClient.fetchQuery(options);
    await queryClient.fetchQuery({ ...options, staleTime: Number.POSITIVE_INFINITY });

    expect(calls).toBe(1);
  });
});
