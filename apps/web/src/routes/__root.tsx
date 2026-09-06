import type { QueryClient } from "@tanstack/react-query";
import { Outlet, createRootRouteWithContext } from "@tanstack/react-router";
import { applicationName } from "../runtime-config";

function Shell() {
  const appName = applicationName(window.__MJGA_RUNTIME_CONFIG__);
  return (
    <main>
      <header>
        <span className="eyebrow">Human-Agent Ready</span>
        <h1>{appName}</h1>
      </header>
      <Outlet />
    </main>
  );
}

function ErrorBoundary({ error }: { error: Error }) {
  return (
    <section role="alert">
      <h2>Status unavailable</h2>
      <p>{error.message}</p>
      <button onClick={() => window.location.reload()}>Try again</button>
    </section>
  );
}

export const Route = createRootRouteWithContext<{ queryClient: QueryClient }>()(
  {
    component: Shell,
    errorComponent: ErrorBoundary,
    notFoundComponent: () => <p>Page not found.</p>,
  },
);
