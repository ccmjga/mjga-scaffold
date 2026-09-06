export type { PlatformStatus } from "./generated/types/PlatformStatus";
export { platformStatusSchema } from "./generated/schemas/platformStatusSchema";
import type { PlatformStatus } from "./generated/types/PlatformStatus";
import { getPlatformStatusQueryOptions as generatedPlatformStatusQueryOptions } from "./generated/queries/platform/useGetPlatformStatus";
export type ApiClientOptions = RequestInit & {
    baseURL?: string;
};
export declare function getPlatformStatus(options?: ApiClientOptions): Promise<PlatformStatus>;
export declare function getPlatformStatusQueryOptions(options?: ApiClientOptions): ReturnType<typeof generatedPlatformStatusQueryOptions>;
