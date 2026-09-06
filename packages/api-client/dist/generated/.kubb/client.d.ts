import type { HeadersInit, Serializers, Styles } from "./serializers";
import { type StandardSchemaValidator } from "./standardSchema";
/**
 * HTTP status codes treated as a success, everything else is an error.
 */
export type SuccessStatusCode = "200" | "201" | "202" | "203" | "204" | "205" | "206" | "207" | "208" | "226";
/**
 * The success members of a per-status responses record.
 */
export type SuccessOf<TResponses> = TResponses[Extract<keyof TResponses, SuccessStatusCode>];
/**
 * The error members of a per-status responses record, every documented status that is not a 2xx.
 */
export type ErrorOf<TResponses> = TResponses[Exclude<keyof TResponses, SuccessStatusCode>];
/**
 * Converts a response record's string status key to its numeric literal, leaving non-numeric keys like `default` as `number`.
 */
export type ToStatusNumber<TStatus> = TStatus extends `${infer TNumber extends number}` ? TNumber : number;
/**
 * The plain body of a per-status response, unwrapping the `{ contentType; data }` union so an error result keeps the bare body union on `error`.
 */
export type DataOf<T> = T extends {
    contentType: string;
    data: infer TData;
} ? TData : T;
/**
 * The success variant for a single status, flattened so the negotiated `contentType` sits next to `data` and `switch (result.contentType)` narrows it.
 */
export type SuccessVariant<TStatus, TEntry, TRequest, TResponse> = TEntry extends {
    contentType: string;
    data: unknown;
} ? TEntry extends {
    contentType: infer TContentType;
    data: infer TData;
} ? {
    status: ToStatusNumber<TStatus>;
    data: TData;
    error: undefined;
    contentType: TContentType;
    request: TRequest;
    response: TResponse;
} : never : {
    status: ToStatusNumber<TStatus>;
    data: TEntry;
    error: undefined;
    contentType: string | undefined;
    request: TRequest;
    response: TResponse;
};
/**
 * One result variant for a single documented status, keyed by the numeric `status` so a `switch (result.status)` narrows `data` or `error`.
 */
export type ResultByStatus<TResponses, TStatus extends keyof TResponses, TRequest, TResponse> = TStatus extends SuccessStatusCode ? SuccessVariant<TStatus, TResponses[TStatus], TRequest, TResponse> : {
    status: ToStatusNumber<TStatus>;
    data: undefined;
    error: DataOf<TResponses[TStatus]>;
    contentType: string | undefined;
    request: TRequest;
    response: TResponse;
};
/**
 * The union of every documented status' result variant.
 */
export type ResultUnion<TResponses, TRequest, TResponse> = {
    [TStatus in keyof TResponses]: ResultByStatus<TResponses, TStatus, TRequest, TResponse>;
}[keyof TResponses];
/**
 * The union of just the success (2xx) status variants, selected by status code so an untyped error payload can never widen `data`.
 */
export type SuccessResultUnion<TResponses, TRequest, TResponse> = {
    [TStatus in Extract<keyof TResponses, SuccessStatusCode>]: ResultByStatus<TResponses, TStatus, TRequest, TResponse>;
}[Extract<keyof TResponses, SuccessStatusCode>];
/**
 * The shape every generated function returns, discriminated by the top-level `status`, narrowing to the 2xx variants under `throwOnError` and to every documented status without it.
 */
export type RequestResult<TResponses, ThrowOnError extends boolean = true, TRequest = Request, TResponse = Response> = ThrowOnError extends true ? [SuccessResultUnion<TResponses, TRequest, TResponse>] extends [never] ? {
    status: number;
    data: SuccessOf<TResponses>;
    error: undefined;
    contentType: string | undefined;
    request: TRequest;
    response: TResponse;
} : SuccessResultUnion<TResponses, TRequest, TResponse> : [ResultUnion<TResponses, TRequest, TResponse>] extends [never] ? {
    status: number;
    data: undefined;
    error: undefined;
    contentType: string | undefined;
    request: TRequest;
    response: TResponse;
} : ResultUnion<TResponses, TRequest, TResponse>;
/**
 * A `RequestResult` promise with an extra `unwrap()` method that resolves to the success body.
 */
export type Unwrappable<T extends {
    data: unknown;
    error: unknown;
}> = Promise<T> & {
    unwrap: () => Promise<Extract<T, {
        error: undefined;
    }>["data"]>;
};
/**
 * Attaches `unwrap()` to a result promise, which rejects with `error` when the result carried one.
 *
 * @example Full result
 * `const { data, error } = await getPetById({ path: { petId: 1 } })`
 *
 * @example Success body only
 * `const pet = await getPetById({ path: { petId: 1 } }).unwrap()`
 */
export declare function withUnwrap<T extends {
    data: unknown;
    error: unknown;
}>(promise: Promise<T>): Unwrappable<T>;
/**
 * The data-shaped keys of the grouped options object, which `Options` re-adds typed per operation.
 */
export type DataShape = {
    body?: unknown;
    cookies?: unknown;
    headers?: unknown;
    path?: unknown;
    query?: unknown;
};
export type RequestCredentials = "omit" | "same-origin" | "include";
export type ResponseType = "arraybuffer" | "blob" | "document" | "json" | "text" | "stream";
/**
 * Turns a raw response body into a parsed value, registered per media type as a codec's `deserialize` to handle formats the runtime does not decode itself.
 */
export type Deserializer<T = unknown> = (raw: unknown, contentType: string) => T | Promise<T>;
/**
 * Serializes a request body for a single media type, registered per content type as a codec's `serialize` to encode formats the default serializer does not handle.
 */
export type ContentBodySerializer = (body: unknown, contentType?: string) => BodyInit | undefined;
/**
 * A per-content-type codec registered on `codecs`, keyed by content type. `serialize` encodes the
 * request body for that media type and `deserialize` decodes the response body. Either half is
 * optional, so a codec can handle one direction.
 */
export type Codec = {
    serialize?: ContentBodySerializer;
    deserialize?: Deserializer;
};
/**
 * The per-call content type selection, where a bare string sets the request content type and the object form also sets the response format sent as `Accept`.
 */
export type ContentType = string | {
    request?: string;
    response?: string;
};
/**
 * A Standard Schema validator (zod, valibot, arktype) that parses a value before it is sent or after
 * it is received. `runValidator` runs it through `validateStandardSchema`. Wired through the per-call
 * `validator.request` / `validator.response` / `validator.error` hooks (`error` runs on the error body when a
 * non-2xx call does not throw).
 */
export type Validator<T = unknown> = StandardSchemaValidator<T>;
/**
 * A resolved security scheme carried on each generated call's `security` array and passed to the `auth` resolver.
 */
export type Auth = {
    type: "http" | "apiKey" | "oauth2" | "openIdConnect";
    scheme?: "bearer" | "basic";
    name?: string;
    in?: "header" | "query" | "cookie";
};
/**
 * The raw token a consumer returns for a scheme (or `user:password` for basic), or `undefined` to skip it.
 */
export type AuthToken = string | undefined;
/**
 * Resolves the token for a security scheme, either a static token or a callback called per scheme until one returns a token.
 */
export type AuthResolver = AuthToken | ((auth: Auth) => AuthToken | Promise<AuthToken>);
/**
 * Extra `fetch` init the transport spreads onto every `Request`, an escape hatch for fields the runtime does not set itself such as `cache`, `redirect`, and Next.js's `next`.
 */
export type FetchOptions = RequestInit & {
    next?: Record<string, unknown>;
};
/**
 * The request a generated function hands to the runtime, with `body` / `headers` / `path` / `query` from the grouped options.
 */
export type RequestConfig<TBody = unknown, TRequest = Request, TResponse = Response> = {
    baseURL?: string;
    url?: string;
    method?: "GET" | "PUT" | "PATCH" | "POST" | "DELETE" | "OPTIONS" | "HEAD";
    path?: Record<string, unknown>;
    query?: unknown;
    params?: unknown;
    cookies?: Record<string, unknown>;
    body?: TBody;
    headers?: HeadersInit;
    styles?: Styles;
    signal?: AbortSignal;
    credentials?: RequestCredentials;
    options?: FetchOptions;
    contentType?: ContentType;
    responseType?: ResponseType;
    throwOnError?: boolean;
    client?: ClientInstance<TRequest, TResponse>;
    transport?: Transport<TRequest, TResponse>;
    serializer?: Serializers;
    codecs?: Record<string, Codec>;
    validator?: {
        request?: Validator;
        response?: Validator;
        error?: Validator;
    };
    security?: Array<Auth>;
    auth?: AuthResolver;
};
/**
 * The grouped options object passed to every generated function: the request config minus the
 * data-shaped keys and the literal `url`, plus the per-operation `<Name>Request`.
 */
export type Options<TData extends DataShape, ThrowOnError extends boolean = true, TRequest = Request, TResponse = Response> = Omit<RequestConfig<unknown, TRequest, TResponse>, keyof DataShape | "url"> & TData & {
    client?: ClientInstance<TRequest, TResponse>;
    throwOnError?: ThrowOnError;
};
/**
 * Client-level configuration shared by every call an instance makes, overridden by the per-call `RequestConfig`.
 */
export type ClientConfig<TRequest = Request, TResponse = Response> = {
    baseURL?: string;
    headers?: HeadersInit;
    credentials?: RequestCredentials;
    options?: FetchOptions;
    throwOnError?: boolean;
    transport?: Transport<TRequest, TResponse>;
    serializer?: Serializers;
    codecs?: Record<string, Codec>;
    auth?: AuthResolver;
};
/**
 * The normalized request the transport receives, with all serialization, auth, and header work already done.
 */
export type ResolvedRequest = {
    url: string;
    method: string;
    headers: Record<string, string>;
    body?: BodyInit;
    signal?: AbortSignal;
    credentials?: RequestCredentials;
    options?: FetchOptions;
    responseType?: ResponseType;
};
/**
 * What a transport returns: the parsed body plus the native request and response objects.
 */
export type TransportResult<TData = unknown, TRequest = Request, TResponse = Response> = {
    data: TData;
    status: number;
    statusText: string;
    headers: Headers;
    contentType?: string;
    request: TRequest;
    response: TResponse;
};
/**
 * The per-plugin send, supplied to `createClientCore` as `defaultTransport`.
 */
export type Transport<TRequest = Request, TResponse = Response> = (request: ResolvedRequest) => Promise<TransportResult<unknown, TRequest, TResponse>>;
/**
 * The result a resolved call produces before it is cast to `RequestResult` by the generated wrapper.
 */
export type CallResult<TRequest = Request, TResponse = Response> = {
    status: number;
    data: unknown;
    error: unknown;
    contentType: string | undefined;
    request: TRequest;
    response: TResponse;
};
export type InterceptorFn<T> = (value: T) => T | Promise<T>;
/**
 * A single interceptor channel with a transport-agnostic `use` / `eject` / `update` API.
 */
export type InterceptorStack<T> = {
    use: (fn: InterceptorFn<T>) => number;
    eject: (id: number) => void;
    update: (id: number, fn: InterceptorFn<T>) => void;
    run: (value: T) => Promise<T>;
};
/**
 * The three interceptor channels every client instance exposes.
 */
export type Interceptors<TRequest = Request, TResponse = Response> = {
    request: InterceptorStack<ResolvedRequest>;
    response: InterceptorStack<TransportResult<unknown, TRequest, TResponse>>;
    error: InterceptorStack<ResponseError<unknown, TRequest, TResponse>>;
};
/**
 * A client instance: the callable send plus configuration, interceptors, and an isolated
 * `createClient` factory bound to the same transport.
 */
export type ClientInstance<TRequest = Request, TResponse = Response> = {
    <TBody = unknown>(config: RequestConfig<TBody, TRequest, TResponse>): Promise<CallResult<TRequest, TResponse>>;
    getConfig: () => ClientConfig<TRequest, TResponse>;
    setConfig: (config: ClientConfig<TRequest, TResponse>) => ClientConfig<TRequest, TResponse>;
    getUrl: <TBody = unknown>(config: RequestConfig<TBody, TRequest, TResponse>) => string;
    interceptors: Interceptors<TRequest, TResponse>;
    createClient: (config?: ClientConfig<TRequest, TResponse>) => ClientInstance<TRequest, TResponse>;
};
/**
 * Thrown for a non-2xx response, so a resolved call always means success.
 */
export declare class ResponseError<TError = unknown, TRequest = Request, TResponse = Response> extends Error {
    data: TError;
    status: number;
    statusText: string;
    contentType: string | undefined;
    request: TRequest;
    response: TResponse;
    constructor(config: {
        data: TError;
        status: number;
        statusText: string;
        contentType?: string;
        request: TRequest;
        response: TResponse;
    });
}
export type ResponseErrorConfig<TError = unknown> = ResponseError<TError>;
/**
 * Creates a transport-agnostic interceptor channel that runs interceptors in registration order.
 */
export declare function createInterceptorStack<T>(): InterceptorStack<T>;
/**
 * Walks the per-operation security in order and places the first resolved token on the request, mutating `headers` / `query` in place.
 */
export declare function resolveAuth(params: {
    security: Array<Auth> | undefined;
    auth: AuthResolver | undefined;
    headers: Record<string, string>;
    query: Record<string, unknown>;
}): Promise<void>;
/**
 * Builds the shared client core bound to a transport, exported by each plugin as `client` plus a `createClient` factory.
 */
export declare function createClientCore<TRequest = Request, TResponse = Response>(options: {
    defaultTransport: Transport<TRequest, TResponse>;
} & ClientConfig<TRequest, TResponse>): ClientInstance<TRequest, TResponse>;
/**
 * One decoded Server-Sent Event, with `data` parsed as JSON when valid and kept as the raw string otherwise.
 */
export type ServerSentEvent<TData = unknown> = {
    data: TData;
    event?: string;
    id?: string;
    retry?: number;
};
/**
 * Parses a `text/event-stream` body into typed Server-Sent Events, consumed with `for await` and stopped early by breaking the loop.
 */
export declare function parseEventStream<TData = unknown>(stream: ReadableStream<Uint8Array> | AsyncIterable<Uint8Array>): AsyncGenerator<ServerSentEvent<TData>>;
/**
 * The resolved shape returned by a generated `text/event-stream` operation: the typed event
 * `stream` plus the native `response`.
 */
export type EventStreamResult<TData = unknown, TResponse = Response> = {
    stream: AsyncGenerator<ServerSentEvent<TData>>;
    response: TResponse;
};
/**
 * Wraps a transport result whose `data` is a streaming body into an `EventStreamResult`, exposing
 * the parsed events as a typed async iterator. Generated SSE operations call this.
 */
export declare function toEventStream<TData = unknown>(result: Promise<{
    data: unknown;
    response: Response;
}>): Promise<EventStreamResult<TData>>;
export declare const client: ClientInstance<Request, Response>;
export declare const createClient: (config?: Parameters<typeof client.createClient>[0]) => ClientInstance<Request, Response>;
