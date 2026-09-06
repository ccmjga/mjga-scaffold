import { getPlatformStatusQueryOptions } from "@mjga/api-client";
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/")({
  loader: ({ context }) =>
    context.queryClient.ensureQueryData(getPlatformStatusQueryOptions()),
  pendingMs: 0,
  pendingComponent: () => <p role="status">Loading platform status…</p>,
  errorComponent: () => (
    <p role="alert">
      Couldn’t load platform status. Refresh the page to try again.
    </p>
  ),
  component: StatusPage,
});

export function StatusPage() {
  const status = Route.useLoaderData();
  return (
    <section aria-labelledby="platform-status">
      <h2 id="platform-status">Platform status</h2>
      <p className="status">
        <span aria-hidden="true" />
        {status.status}
      </p>
      <dl>
        <dt>Application</dt>
        <dd>{status.application}</dd>
        <dt>Version</dt>
        <dd>{status.version}</dd>
      </dl>
    </section>
  );
}
