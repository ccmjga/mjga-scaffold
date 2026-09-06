function isFormBody(body) {
    return (body instanceof FormData ||
        body instanceof URLSearchParams ||
        body instanceof Blob ||
        body instanceof ArrayBuffer ||
        ArrayBuffer.isView(body) ||
        typeof body === "string");
}
export function isDefaultJsonBody(body) {
    return body !== undefined && body !== null && !isFormBody(body);
}
/**
 * Emits a `bigint` (`format: int64`) as a JSON number, which `JSON.stringify` refuses to do itself.
 * Past the safe-integer range it throws, so an id never goes out silently truncated.
 */
function jsonReplacer(_key, value) {
    if (typeof value !== "bigint")
        return value;
    if (value > Number.MAX_SAFE_INTEGER || value < Number.MIN_SAFE_INTEGER)
        throw new TypeError(`Cannot serialize ${value}n as JSON without losing precision, register a serializer.body to send it another way.`);
    return Number(value);
}
function appendFormDataValue({ formData, key, value, contentType, }) {
    if (value === undefined || value === null)
        return;
    if (value instanceof Blob)
        formData.append(key, value);
    else if (typeof value === "object" && !(value instanceof Date)) {
        const json = JSON.stringify(value, jsonReplacer);
        // A part's media type can only be set by wrapping the value in a typed Blob.
        formData.append(key, contentType ? new Blob([json], { type: contentType }) : json);
    }
    else
        formData.append(key, toValue(value));
}
/**
 * Default body serializer: passes binary/form bodies through and JSON-serializes everything else.
 * For `multipart/form-data` plain objects become `FormData` and for
 * `application/x-www-form-urlencoded` they become `URLSearchParams`. When `encoding` is supplied each
 * urlencoded property follows its OpenAPI `style` / `explode` / `allowReserved`.
 *
 * @example
 * ```ts
 * defaultBodySerializer({ body: { name: 'odie' } }) // '{"name":"odie"}'
 * defaultBodySerializer({ body: { field: 'x' }, contentType: 'multipart/form-data' }) // FormData
 * defaultBodySerializer({ body: { tags: ['a', 'b'] }, contentType: 'application/x-www-form-urlencoded', encoding: { tags: { explode: false } } }) // 'tags=a,b'
 * defaultBodySerializer({ body: { meta: { a: 1 } }, contentType: 'multipart/form-data', encoding: { meta: { contentType: 'application/json' } } }) // FormData with a typed Blob part
 * ```
 */
export const defaultBodySerializer = ({ body, contentType, encoding, }) => {
    if (body === undefined || body === null)
        return undefined;
    if (isFormBody(body))
        return body;
    if (contentType?.includes("multipart/form-data")) {
        const formData = new FormData();
        for (const [key, value] of Object.entries(body)) {
            const partContentType = encoding?.[key]?.contentType;
            if (Array.isArray(value))
                for (const item of value)
                    appendFormDataValue({
                        formData,
                        key,
                        value: item,
                        contentType: partContentType,
                    });
            else
                appendFormDataValue({
                    formData,
                    key,
                    value,
                    contentType: partContentType,
                });
        }
        return formData;
    }
    if (contentType?.includes("application/x-www-form-urlencoded")) {
        if (encoding)
            return serializeUrlencodedBody(body, encoding);
        return new URLSearchParams(body);
    }
    return JSON.stringify(body, jsonReplacer);
};
function serializeUrlencodedBody(body, encoding) {
    const parts = [];
    for (const [key, value] of Object.entries(body)) {
        const propertyEncoding = encoding[key];
        parts.push(...(propertyEncoding
            ? serializeStyledQueryParam({ key, value, options: propertyEncoding })
            : serializeDefaultQueryParam(key, value)));
    }
    return parts.join("&");
}
function serializeCookie({ name, value, explode, }) {
    if (Array.isArray(value)) {
        const items = value
            .filter(notNullish)
            .map((item) => encodeURIComponent(toValue(item)));
        return explode
            ? items.map((item) => `${name}=${item}`).join("; ")
            : `${name}=${items.join(",")}`;
    }
    if (isRecord(value)) {
        const entries = Object.entries(value).filter(([, item]) => notNullish(item));
        if (explode)
            return entries
                .map(([key, item]) => `${key}=${encodeURIComponent(toValue(item))}`)
                .join("; ");
        return `${name}=${entries
            .flatMap(([key, item]) => [key, item])
            .map((item) => encodeURIComponent(toValue(item)))
            .join(",")}`;
    }
    return `${name}=${encodeURIComponent(toValue(value))}`;
}
/**
 * Serializes cookie parameters into a `Cookie` header value using the OpenAPI `form` style, joined
 * with `; `. Values are URL-encoded and `explode` is honored per parameter.
 *
 * @example
 * ```ts
 * serializeCookies({ session: 'abc', ids: [1, 2] }) // 'session=abc; ids=1,2'
 * serializeCookies({ ids: [1, 2] }, { ids: { explode: true } }) // 'ids=1; ids=2'
 * ```
 */
export function serializeCookies(cookies, styles) {
    const parts = [];
    for (const [name, value] of Object.entries(cookies)) {
        if (value === undefined || value === null)
            continue;
        parts.push(serializeCookie({
            name,
            value,
            explode: styles?.[name]?.explode ?? false,
        }));
    }
    return parts.join("; ");
}
function appendQueryValue({ search, key, value, }) {
    if (value === undefined || value === null)
        return;
    if (Array.isArray(value)) {
        for (const item of value)
            appendQueryValue({ search, key, value: item });
        return;
    }
    if (isRecord(value)) {
        for (const [prop, propValue] of Object.entries(value)) {
            appendQueryValue({ search, key: `${key}[${prop}]`, value: propValue });
        }
        return;
    }
    search.append(key, toValue(value));
}
const queryDelimiters = {
    form: ",",
    spaceDelimited: "%20",
    pipeDelimited: "|",
    deepObject: ",",
};
function notNullish(value) {
    return value !== undefined && value !== null;
}
/**
 * Renders a primitive parameter value as a string, serializing `Date` to ISO-8601 so dates are
 * stable across path, query, cookie, and header locations rather than locale-dependent.
 */
function toValue(value) {
    return value instanceof Date ? value.toISOString() : String(value);
}
/**
 * Percent-encodes a value, keeping RFC 3986 reserved characters intact (used when `allowReserved` is set).
 */
function encodeReserved(value) {
    return encodeURI(toValue(value));
}
/**
 * Percent-encodes a value, escaping reserved characters (the default query/path encoder).
 */
function encodeComponent(value) {
    return encodeURIComponent(toValue(value));
}
/**
 * Whether a value should expand into bracketed/keyed parts. Arrays and `Date` are excluded so they
 * are serialized as a unit (a `Date` becomes an ISO string, not its enumerable own properties).
 */
function isRecord(value) {
    return (typeof value === "object" &&
        value !== null &&
        !Array.isArray(value) &&
        !(value instanceof Date));
}
/**
 * Expands an object or array into `deepObject` query parts, recursing into nested values so
 * `{ a: { b: { c: 1 } } }` becomes `a[b][c]=1`. Primitives terminate the recursion.
 */
function serializeDeepObject({ key, value, encode, }) {
    if (value === undefined || value === null)
        return [];
    if (Array.isArray(value))
        return value.flatMap((item, index) => serializeDeepObject({ key: `${key}[${index}]`, value: item, encode }));
    if (isRecord(value)) {
        return Object.entries(value)
            .filter(([, item]) => notNullish(item))
            .flatMap(([prop, item]) => serializeDeepObject({ key: `${key}[${prop}]`, value: item, encode }));
    }
    return [`${encode(key)}=${encode(value)}`];
}
function serializeStyledQueryArray({ key, value, options, encode, }) {
    const items = value.filter(notNullish);
    if (options.explode ?? true)
        return items.map((item) => `${encode(key)}=${encode(item)}`);
    return [
        `${encode(key)}=${items.map(encode).join(queryDelimiters[options.style ?? "form"])}`,
    ];
}
function serializeStyledQueryObject({ key, value, options, encode, }) {
    if ((options.style ?? "form") === "deepObject")
        return serializeDeepObject({ key, value, encode });
    const entries = Object.entries(value).filter(([, item]) => notNullish(item));
    if (options.explode ?? true)
        return entries.map(([prop, item]) => `${encode(prop)}=${encode(item)}`);
    return [
        `${encode(key)}=${entries
            .flatMap(([prop, item]) => [prop, item])
            .map(encode)
            .join(",")}`,
    ];
}
function serializeStyledQueryParam({ key, value, options, }) {
    if (value === undefined || value === null)
        return [];
    const encode = options.allowReserved ? encodeReserved : encodeComponent;
    if (Array.isArray(value))
        return serializeStyledQueryArray({ key, value, options, encode });
    if (isRecord(value))
        return serializeStyledQueryObject({ key, value, options, encode });
    return [`${encode(key)}=${encode(value)}`];
}
function serializeDefaultQueryParam(key, value) {
    const search = new URLSearchParams();
    appendQueryValue({ search, key, value });
    const result = search.toString();
    return result ? [result] : [];
}
/**
 * Default query serializer. Members with `options` metadata follow their OpenAPI `style` / `explode`
 * / `allowReserved`. Members without it keep the defaults: arrays explode into repeated keys and
 * nested objects use the `deepObject` style (`key[prop]=value`).
 *
 * @example
 * ```ts
 * defaultQuerySerializer({ id: [3, 4, 5] }) // 'id=3&id=4&id=5'
 * defaultQuerySerializer({ id: [3, 4, 5] }, { id: { style: 'form', explode: false } }) // 'id=3,4,5'
 * defaultQuerySerializer({ id: [3, 4, 5] }, { id: { style: 'spaceDelimited', explode: false } }) // 'id=3%204%205'
 * defaultQuerySerializer({ id: [3, 4, 5] }, { id: { style: 'pipeDelimited', explode: false } }) // 'id=3|4|5'
 * defaultQuerySerializer({ a: { b: 1 } }, { a: { style: 'deepObject' } }) // 'a%5Bb%5D=1'
 * defaultQuerySerializer({ a: { b: { c: 1 } } }, { a: { style: 'deepObject' } }) // 'a%5Bb%5D%5Bc%5D=1'
 * ```
 */
export const defaultQuerySerializer = (params, options) => {
    const parts = [];
    for (const [key, value] of Object.entries(params)) {
        const paramOptions = options?.[key];
        parts.push(...(paramOptions
            ? serializeStyledQueryParam({ key, value, options: paramOptions })
            : serializeDefaultQueryParam(key, value)));
    }
    return parts.join("&");
};
function serializePathPrimitive({ name, value, style, }) {
    const encoded = encodeComponent(value);
    if (style === "label")
        return `.${encoded}`;
    if (style === "matrix")
        return `;${name}=${encoded}`;
    return encoded;
}
function serializePathArray({ name, value, style, explode, }) {
    const items = value.map(encodeComponent);
    if (style === "label")
        return `.${items.join(explode ? "." : ",")}`;
    if (style === "matrix")
        return explode
            ? items.map((item) => `;${name}=${item}`).join("")
            : `;${name}=${items.join(",")}`;
    return items.join(",");
}
function serializePathObject({ name, value, style, explode, }) {
    const members = Object.entries(value).map(([key, item]) => explode
        ? `${encodeComponent(key)}=${encodeComponent(item)}`
        : `${encodeComponent(key)},${encodeComponent(item)}`);
    if (style === "label")
        return `.${members.join(explode ? "." : ",")}`;
    if (style === "matrix")
        return explode
            ? members.map((member) => `;${member}`).join("")
            : `;${name}=${members.join(",")}`;
    return members.join(",");
}
/**
 * Default path serializer honoring the OpenAPI `style` / `explode` metadata. Without metadata it
 * falls back to `simple` style with `explode: false`. Replaces the previous `String(value)`
 * interpolation, which emitted `[object Object]` for object path params.
 *
 * @example
 * ```ts
 * defaultPathSerializer({ name: 'id', value: [3, 4, 5] }) // '3,4,5'
 * defaultPathSerializer({ name: 'id', value: [3, 4, 5], options: { style: 'label', explode: true } }) // '.3.4.5'
 * defaultPathSerializer({ name: 'id', value: [3, 4, 5], options: { style: 'matrix', explode: true } }) // ';id=3;id=4;id=5'
 * defaultPathSerializer({ name: 'pt', value: { x: 1, y: 2 } }) // 'x,1,y,2'
 * ```
 */
export const defaultPathSerializer = ({ name, value, options, }) => {
    if (value === undefined || value === null)
        return "";
    const style = options?.style ?? "simple";
    const explode = options?.explode ?? false;
    if (Array.isArray(value))
        return serializePathArray({ name, value, style, explode });
    if (isRecord(value))
        return serializePathObject({ name, value, style, explode });
    return serializePathPrimitive({ name, value, style });
};
function serializeHeaderValue(value, explode) {
    if (Array.isArray(value))
        return value.filter(notNullish).map(toValue).join(",");
    if (!isRecord(value))
        return toValue(value);
    const entries = Object.entries(value).filter(([, item]) => notNullish(item));
    if (explode)
        return entries.map(([key, item]) => `${key}=${toValue(item)}`).join(",");
    return entries
        .flatMap(([key, item]) => [key, item])
        .map(toValue)
        .join(",");
}
/**
 * Serializes array and object header parameters with the OpenAPI `simple` style before they are
 * merged. Header values are not URL-encoded. Primitive values and headers without metadata pass
 * through untouched.
 */
export function applyHeaderStyles(headers, styles) {
    if (!headers || !styles)
        return headers;
    const entries = Array.isArray(headers) ? headers : Object.entries(headers);
    return entries.map(([key, value]) => {
        const style = styles[key];
        if (!style ||
            value === undefined ||
            value === null ||
            typeof value !== "object")
            return [key, value];
        return [key, serializeHeaderValue(value, style.explode ?? false)];
    });
}
