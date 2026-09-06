import { fakerEN } from "@faker-js/faker";
import {
  createPlatformStatus,
  getPlatformStatusHandler,
  getPlatformStatusHandlerResponse200,
  getPlatformStatusHandlerResponse500,
} from "@mjga/api-client/mocks";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
  RouterProvider,
  createMemoryHistory,
  createRouter,
} from "@tanstack/react-router";
import { delay } from "msw";
import { render } from "vitest-browser-react";
import { expect, test } from "../../test/fixtures";
import { routeTree } from "../routeTree.gen";

function testRouter(queryClient: QueryClient) {
  return createRouter({
    routeTree,
    history: createMemoryHistory({ initialEntries: ["/"] }),
    context: { queryClient },
  });
}

async function renderRouter(queryClient: QueryClient) {
  const router = testRouter(queryClient);
  const screen = await render(
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
  return { router, screen };
}

test.beforeEach(() => fakerEN.seed(20260905));

test("renders loading and the typed platform status", async ({ worker }) => {
  const status = createPlatformStatus({
    status: "UP",
    application: "demo",
    version: "1.0.0",
  });
  worker.use(
    getPlatformStatusHandler(async () => {
      await delay(50);
      return getPlatformStatusHandlerResponse200(status);
    }),
  );
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  const { screen } = await renderRouter(queryClient);
  await expect
    .element(screen.getByRole("status"))
    .toHaveTextContent("Loading platform status");
  await expect
    .element(screen.getByRole("heading", { name: "Platform status" }))
    .toBeVisible();
  await expect.element(screen.getByText("UP")).toBeVisible();
});

test("renders a stable recoverable error", async ({ worker }) => {
  worker.use(
    getPlatformStatusHandler(() =>
      getPlatformStatusHandlerResponse500({
        type: "about:blank",
        title: "Unavailable",
        status: 500,
        code: "platform-unavailable",
      }),
    ),
  );
  const { screen } = await renderRouter(
    new QueryClient({ defaultOptions: { queries: { retry: false } } }),
  );

  await expect
    .element(screen.getByRole("alert"))
    .toHaveTextContent(
      "Couldn’t load platform status. Refresh the page to try again.",
    );
});

test("retries once and reuses the successful cached result", async ({
  worker,
}) => {
  let calls = 0;
  const status = createPlatformStatus({
    status: "UP",
    application: "demo",
    version: "1.0.0",
  });
  worker.use(
    getPlatformStatusHandler(() => {
      calls += 1;
      return calls === 1
        ? getPlatformStatusHandlerResponse500({
            type: "about:blank",
            title: "Unavailable",
            status: 500,
            code: "platform-unavailable",
          })
        : getPlatformStatusHandlerResponse200(status);
    }),
  );
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: 1, retryDelay: 0, staleTime: Number.POSITIVE_INFINITY },
    },
  });

  const first = await renderRouter(queryClient);
  await expect.element(first.screen.getByText("UP")).toBeVisible();
  const second = await renderRouter(queryClient);
  await expect.element(second.screen.getByText("UP")).toBeVisible();
  expect(calls).toBe(2);
});
