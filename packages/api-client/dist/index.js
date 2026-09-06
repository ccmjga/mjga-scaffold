export { platformStatusSchema } from "./generated/schemas/platformStatusSchema";
import { getPlatformStatus as requestPlatformStatus } from "./generated/clients/platform/getPlatformStatus";
import { getPlatformStatusQueryOptions as generatedPlatformStatusQueryOptions } from "./generated/queries/platform/useGetPlatformStatus";
export async function getPlatformStatus(options) {
    const { baseURL, ...fetchOptions } = options ?? {};
    const response = await requestPlatformStatus({
        baseURL,
        options: fetchOptions,
        throwOnError: false,
    });
    if (response.error !== undefined) {
        const problem = response.error;
        throw new Error(problem.code ? `Platform status failed (${problem.code})` : "Platform status failed");
    }
    return response.data;
}
export function getPlatformStatusQueryOptions(options) {
    return {
        ...generatedPlatformStatusQueryOptions(),
        queryFn: ({ signal }) => getPlatformStatus({ ...options, signal }),
    };
}
