import { applyHeaderStyles, defaultBodySerializer, defaultPathSerializer, defaultQuerySerializer, isDefaultJsonBody, serializeCookies, } from "./serializers";
import { validateStandardSchema, } from "./standardSchema";
/**
 * Attaches `unwrap()` to a result promise, which rejects with `error` when the result carried one.
 *
 * @example Full result
 * `const { data, error } = await getPetById({ path: { petId: 1 } })`
 *
 * @example Success body only
 * `const pet = await getPetById({ path: { petId: 1 } }).unwrap()`
 */
export function withUnwrap(promise) {
    const unwrappable = promise;
    unwrappable.unwrap = () => promise.then((result) => {
        if (result.error !== undefined)
            throw result.error;
        return result.data;
    });
    return unwrappable;
}
/**
 * Thrown for a non-2xx response, so a resolved call always means success.
 */
export class ResponseError extends Error {
    data;
    status;
    statusText;
    contentType;
    request;
    response;
    constructor(config) {
        super(`Request failed with status ${config.status}${config.statusText ? ` ${config.statusText}` : ""}`);
        this.name = "ResponseError";
        this.data = config.data;
        this.status = config.status;
        this.statusText = config.statusText;
        this.contentType = config.contentType;
        this.request = config.request;
        this.response = config.response;
    }
}
function serializeHeaders(headers) {
    if (!headers)
        return {};
    const entries = Array.isArray(headers) ? headers : Object.entries(headers);
    const result = {};
    for (const [key, value] of entries) {
        if (value === undefined || value === null)
            continue;
        result[key] =
            typeof value === "string"
                ? value
                : typeof value === "object"
                    ? JSON.stringify(value)
                    : String(value);
    }
    return result;
}
function mergeHeaders(...sources) {
    return Object.assign({}, ...sources.map(serializeHeaders));
}
function getHeader(headers, name) {
    const key = Object.keys(headers).find((k) => k.toLowerCase() === name.toLowerCase());
    return key ? headers[key] : undefined;
}
function hasHeader(headers, name) {
    return Object.keys(headers).some((k) => k.toLowerCase() === name.toLowerCase());
}
/**
 * Joins the URL parts, interpolates URL-encoded `{param}` segments, and appends the serialized query, shared by the send path and `getUrl`.
 */
function serializeUrl({ parts, pathParams, search, pathSerializer = defaultPathSerializer, pathStyles, }) {
    const path = parts
        .filter(Boolean)
        .join("")
        .replace(/\{([^{}]+)\}/g, (_, key) => pathSerializer({
        name: key,
        value: pathParams[key],
        options: pathStyles?.[key],
    }));
    return path + (search ? `?${search}` : "");
}
/**
 * Creates a transport-agnostic interceptor channel that runs interceptors in registration order.
 */
export function createInterceptorStack() {
    let entries = [];
    let counter = 0;
    return {
        use(fn) {
            const id = ++counter;
            entries.push({ id, fn });
            return id;
        },
        eject(id) {
            entries = entries.filter((entry) => entry.id !== id);
        },
        update(id, fn) {
            const entry = entries.find((item) => item.id === id);
            if (entry)
                entry.fn = fn;
        },
        async run(value) {
            let result = value;
            for (const entry of entries) {
                result = await entry.fn(result);
            }
            return result;
        },
    };
}
/**
 * Walks the per-operation security in order and places the first resolved token on the request, mutating `headers` / `query` in place.
 */
export async function resolveAuth(params) {
    const { security, auth, headers, query } = params;
    if (!security?.length || auth === undefined)
        return;
    for (const scheme of security) {
        const token = typeof auth === "function" ? await auth(scheme) : auth;
        if (token === undefined)
            continue;
        if (scheme.type === "apiKey") {
            const name = scheme.name ?? "Authorization";
            if (scheme.in === "query") {
                if (query[name] === undefined)
                    query[name] = token;
            }
            else if (scheme.in === "cookie") {
                headers["Cookie"] = [headers["Cookie"], `${name}=${token}`]
                    .filter(Boolean)
                    .join("; ");
            }
            else if (!hasHeader(headers, name)) {
                headers[name] = token;
            }
        }
        else if (!hasHeader(headers, "Authorization")) {
            headers["Authorization"] =
                scheme.scheme === "basic" ? `Basic ${btoa(token)}` : `Bearer ${token}`;
        }
        return;
    }
}
async function runValidator(validator, value) {
    if (!validator)
        return value;
    return validateStandardSchema(validator, value);
}
/**
 * The base media type of a `Content-Type` value, lowercased and stripped of any `; charset=...` parameters.
 */
function baseContentType(value) {
    if (!value)
        return undefined;
    return value.split(";")[0].trim().toLowerCase() || undefined;
}
/**
 * Reads the negotiated response content type from the response headers as a base media type.
 */
function getResponseContentType(headers) {
    if (!headers)
        return undefined;
    const value = headers instanceof Headers
        ? headers.get("Content-Type")
        : (headers["Content-Type"] ?? headers["content-type"]);
    return baseContentType(value);
}
/**
 * Normalizes the `contentType` option to its `{ request, response }` form, treating a bare string as the request content type.
 */
function resolveContentType(contentType) {
    if (typeof contentType === "string")
        return { request: contentType };
    return contentType ?? {};
}
/**
 * The per-concern serializers for a call, the per-call serializer winning over the client's and
 * falling back to the defaults.
 */
function resolveSerializers({ config, requestConfig, }) {
    return {
        querySerializer: requestConfig.serializer?.query ??
            config.serializer?.query ??
            defaultQuerySerializer,
        bodySerializer: requestConfig.serializer?.body ??
            config.serializer?.body ??
            defaultBodySerializer,
        pathSerializer: requestConfig.serializer?.path ??
            config.serializer?.path ??
            defaultPathSerializer,
    };
}
/**
 * Resolves everything a call needs before it touches the transport: merged headers with the
 * negotiated content type, auth on headers or query, serialized cookies, the validated and
 * serialized body, and the full URL.
 */
async function resolveRequest({ config, requestConfig, }) {
    const { querySerializer, bodySerializer, pathSerializer } = resolveSerializers({ config, requestConfig });
    const codecs = { ...config.codecs, ...requestConfig.codecs };
    const headers = mergeHeaders(config.headers, applyHeaderStyles(requestConfig.headers, requestConfig.styles?.header));
    const { request: requestContentTypeOption, response: responseContentType } = resolveContentType(requestConfig.contentType);
    const requestContentType = requestContentTypeOption ?? getHeader(headers, "content-type");
    if (responseContentType && !hasHeader(headers, "accept")) {
        headers["Accept"] = responseContentType;
    }
    const query = {
        ...(requestConfig.query ?? requestConfig.params),
    };
    await resolveAuth({
        security: requestConfig.security,
        auth: requestConfig.auth ?? config.auth,
        headers,
        query,
    });
    if (requestConfig.cookies) {
        const cookie = serializeCookies(requestConfig.cookies, requestConfig.styles?.cookie);
        if (cookie)
            headers["Cookie"] = [headers["Cookie"], cookie]
                .filter(Boolean)
                .join("; ");
    }
    const validatedBody = await runValidator(requestConfig.validator?.request, requestConfig.body);
    const requestContentTypeBase = baseContentType(requestContentType);
    const contentCodec = requestContentTypeBase
        ? codecs[requestContentTypeBase]
        : undefined;
    const usesDefaultBodySerializer = !contentCodec?.serialize && bodySerializer === defaultBodySerializer;
    const body = contentCodec?.serialize
        ? contentCodec.serialize(validatedBody, requestContentType)
        : bodySerializer({
            body: validatedBody,
            contentType: requestContentType,
            encoding: requestConfig.styles?.body,
        });
    // A FormData body must keep its Content-Type unset so the runtime appends the multipart boundary.
    if (body instanceof FormData) {
        for (const key of Object.keys(headers)) {
            if (key.toLowerCase() === "content-type")
                delete headers[key];
        }
    }
    else if (requestContentTypeOption) {
        headers["Content-Type"] = requestContentTypeOption;
    }
    else if (usesDefaultBodySerializer &&
        isDefaultJsonBody(validatedBody) &&
        !hasHeader(headers, "content-type")) {
        headers["Content-Type"] = "application/json";
    }
    const url = serializeUrl({
        parts: [requestConfig.baseURL ?? config.baseURL, requestConfig.url],
        pathParams: requestConfig.path ?? {},
        search: querySerializer(query, requestConfig.styles?.query),
        pathSerializer,
        pathStyles: requestConfig.styles?.path,
    });
    const options = config.options || requestConfig.options
        ? { ...config.options, ...requestConfig.options }
        : undefined;
    return {
        codecs,
        request: {
            url,
            method: (requestConfig.method ?? "GET").toUpperCase(),
            headers,
            body,
            signal: requestConfig.signal,
            credentials: requestConfig.credentials,
            options,
            responseType: requestConfig.responseType,
        },
    };
}
/**
 * Turns a transport result into the call result: decodes the body through the matching codec,
 * validates it, and throws a `ResponseError` (after running the error interceptors) for a non-2xx
 * response under `throwOnError`.
 */
async function settleResult({ result, codecs, throwOnError, validator, errorInterceptors, }) {
    const isSuccess = result.status >= 200 && result.status < 300;
    const contentType = result.contentType ?? getResponseContentType(result.headers);
    let decoded = result.data;
    if (contentType) {
        const codec = codecs[contentType];
        if (codec?.deserialize)
            decoded = await codec.deserialize(result.data, contentType);
    }
    if (isSuccess) {
        const data = await runValidator(validator?.response, decoded);
        return {
            status: result.status,
            data,
            error: undefined,
            contentType,
            request: result.request,
            response: result.response,
        };
    }
    const error = await runValidator(validator?.error, decoded);
    if (throwOnError) {
        const responseError = new ResponseError({
            data: error,
            status: result.status,
            statusText: result.statusText,
            contentType,
            request: result.request,
            response: result.response,
        });
        await errorInterceptors.run(responseError);
        throw responseError;
    }
    return {
        status: result.status,
        data: undefined,
        error,
        contentType,
        request: result.request,
        response: result.response,
    };
}
/**
 * Builds the shared client core bound to a transport, exported by each plugin as `client` plus a `createClient` factory.
 */
export function createClientCore(options) {
    const { defaultTransport, ...initialConfig } = options;
    let config = { ...initialConfig };
    const interceptors = {
        request: createInterceptorStack(),
        response: createInterceptorStack(),
        error: createInterceptorStack(),
    };
    const client = (async (requestConfig) => {
        const transport = requestConfig.transport ?? config.transport ?? defaultTransport;
        const { request, codecs } = await resolveRequest({ config, requestConfig });
        const resolvedRequest = await interceptors.request.run(request);
        const result = await interceptors.response.run(await transport(resolvedRequest));
        return settleResult({
            result,
            codecs,
            throwOnError: requestConfig.throwOnError ?? config.throwOnError ?? true,
            validator: requestConfig.validator,
            errorInterceptors: interceptors.error,
        });
    });
    client.getConfig = () => config;
    client.setConfig = (next) => {
        config = {
            ...config,
            ...next,
            headers: {
                ...serializeHeaders(config.headers),
                ...serializeHeaders(next.headers),
            },
        };
        return config;
    };
    client.getUrl = (requestConfig) => {
        const { querySerializer, pathSerializer } = resolveSerializers({
            config,
            requestConfig,
        });
        const query = {
            ...(requestConfig.query ?? requestConfig.params),
        };
        return serializeUrl({
            parts: [requestConfig.baseURL ?? config.baseURL, requestConfig.url],
            pathParams: requestConfig.path ?? {},
            search: querySerializer(query, requestConfig.styles?.query),
            pathSerializer,
            pathStyles: requestConfig.styles?.path,
        });
    };
    client.interceptors = interceptors;
    client.createClient = (next) => createClientCore({ defaultTransport, ...config, ...next });
    return client;
}
/**
 * Picks a `responseType` from a `Content-Type` header, or `undefined` when it is not recognized.
 */
function detectResponseType(contentType) {
    if (!contentType)
        return undefined;
    if (contentType.includes("text/event-stream"))
        return "stream";
    if (contentType.includes("application/json") ||
        contentType.includes("text/json"))
        return "json";
    if (contentType.includes("text/"))
        return "text";
    if (contentType.includes("image/") ||
        contentType.includes("application/octet-stream"))
        return "blob";
    return undefined;
}
/**
 * Parses a `fetch` response body using the `responseType` (explicit or detected from the `Content-Type`), falling back to JSON-then-text.
 */
async function parseResponse(response, responseType) {
    if (response.status === 204 ||
        response.status === 205 ||
        response.status === 304 ||
        !response.body) {
        return undefined;
    }
    switch (responseType ??
        detectResponseType(response.headers.get("Content-Type"))) {
        case "text":
        case "document":
            return response.text();
        case "blob":
            return response.blob();
        case "arraybuffer":
            return response.arrayBuffer();
        case "stream":
            return response.body ?? undefined;
        case "json": {
            // An empty body with a JSON content-type would make response.json() throw, so treat it as no data.
            const body = await response.text();
            return body ? JSON.parse(body) : undefined;
        }
    }
    const text = await response.text();
    if (!text)
        return undefined;
    try {
        return JSON.parse(text);
    }
    catch {
        return text;
    }
}
/**
 * The default transport that sends the resolved request through `globalThis.fetch` and returns the parsed body with the native request and response.
 */
const defaultTransport = async (request) => {
    const init = {
        ...request.options, // cache, mode, redirect, keepalive, duplex, next, …
        method: request.method,
        headers: request.headers,
        body: request.body,
        signal: request.signal,
    };
    if (request.credentials)
        init.credentials = request.credentials;
    const nativeRequest = new Request(request.url, init);
    const response = await globalThis.fetch(nativeRequest);
    const data = await parseResponse(response, request.responseType);
    return {
        data,
        status: response.status,
        statusText: response.statusText,
        headers: response.headers,
        contentType: getResponseContentType(response.headers),
        request: nativeRequest,
        response,
    };
};
async function* readBytes(stream) {
    if (!("getReader" in stream)) {
        yield* stream;
        return;
    }
    const reader = stream.getReader();
    try {
        while (true) {
            const { done, value } = await reader.read();
            if (done)
                return;
            yield value;
        }
    }
    finally {
        await reader.cancel().catch(() => { });
    }
}
function parseEvent(raw) {
    const data = [];
    const event = { data: undefined };
    let seen = false;
    for (const line of raw.split("\n")) {
        if (!line || line.startsWith(":"))
            continue;
        seen = true;
        const index = line.indexOf(":");
        const field = index === -1 ? line : line.slice(0, index);
        const value = index === -1 ? "" : line.slice(index + 1).replace(/^ /, "");
        if (field === "data")
            data.push(value);
        else if (field === "event")
            event.event = value;
        else if (field === "id")
            event.id = value;
        else if (field === "retry" && Number.isFinite(Number(value)))
            event.retry = Number(value);
    }
    if (!seen)
        return undefined;
    if (data.length) {
        const joined = data.join("\n");
        try {
            event.data = JSON.parse(joined);
        }
        catch {
            event.data = joined;
        }
    }
    return event;
}
/**
 * Parses a `text/event-stream` body into typed Server-Sent Events, consumed with `for await` and stopped early by breaking the loop.
 */
export async function* parseEventStream(stream) {
    const decoder = new TextDecoder();
    const normalize = (text) => text.replace(/\r\n|\r/g, "\n");
    let buffer = "";
    for await (const chunk of readBytes(stream)) {
        const blocks = normalize(buffer + decoder.decode(chunk, { stream: true })).split("\n\n");
        buffer = blocks.pop() ?? "";
        for (const block of blocks) {
            const event = parseEvent(block);
            if (event)
                yield event;
        }
    }
    const event = parseEvent(normalize(buffer + decoder.decode()));
    if (event)
        yield event;
}
/**
 * Wraps a transport result whose `data` is a streaming body into an `EventStreamResult`, exposing
 * the parsed events as a typed async iterator. Generated SSE operations call this.
 */
export async function toEventStream(result) {
    const { data, response } = await result;
    return {
        response,
        stream: parseEventStream(data),
    };
}
export const client = createClientCore({ defaultTransport });
export const createClient = (config) => client.createClient(config);
