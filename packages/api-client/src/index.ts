export type { PlatformStatus } from "./generated/types/PlatformStatus";
export { platformStatusSchema } from "./generated/schemas/platformStatusSchema";
import type { PlatformStatus } from "./generated/types/PlatformStatus";
import { getPlatformStatus as requestPlatformStatus } from "./generated/clients/platform/getPlatformStatus";
import { getPlatformStatusQueryOptions as generatedPlatformStatusQueryOptions } from "./generated/queries/platform/useGetPlatformStatus";

type Problem = { code?: string };
export type ApiClientOptions = RequestInit & { baseURL?: string };

export async function getPlatformStatus(options?: ApiClientOptions): Promise<PlatformStatus> {
  const { baseURL, ...fetchOptions } = options ?? {};
  const response = await requestPlatformStatus({
    baseURL,
    options: fetchOptions,
    throwOnError: false,
  });
  if (response.error !== undefined) {
    const problem = response.error as Problem;
    throw new Error(
      problem.code ? `Platform status failed (${problem.code})` : "Platform status failed",
    );
  }
  return response.data;
}

export function getPlatformStatusQueryOptions(
  options?: ApiClientOptions,
): ReturnType<typeof generatedPlatformStatusQueryOptions> {
  return {
    ...generatedPlatformStatusQueryOptions(),
    queryFn: ({ signal }: { signal: AbortSignal }) => getPlatformStatus({ ...options, signal }),
  };
}
