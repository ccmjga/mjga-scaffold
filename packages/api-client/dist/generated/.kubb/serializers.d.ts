export type HeaderValue = string | number | boolean | null | undefined | object;
export type HeadersInit = Array<[string, HeaderValue]> | Record<string, HeaderValue>;
/**
 * The OpenAPI query-parameter serialization style. `form` is the default; `spaceDelimited` and
 * `pipeDelimited` join arrays with a space or pipe, and `deepObject` renders objects as
 * `key[prop]=value`.
 */
export type QueryStyle = "form" | "spaceDelimited" | "pipeDelimited" | "deepObject";
/**
 * The serialization metadata shared by the styled parameter locations: the OpenAPI `style` (typed per
 * location through `TStyle`), `explode`, and `allowReserved` (keeps RFC 3986 reserved characters
 * unencoded, used by query and request bodies).
 */
export type SerializationStyle<TStyle = string> = {
    style?: TStyle;
    explode?: boolean;
    allowReserved?: boolean;
};
/**
 * The per-parameter query serialization metadata carried by the generated request.
 */
export type QueryParamStyle = SerializationStyle<QueryStyle>;
/**
 * Serializes the query object into a search string. The optional second argument carries the
 * per-parameter OpenAPI `style` / `explode` / `allowReserved` metadata; without it arrays explode
 * into repeated keys and nested objects use the `deepObject` style.
 */
export type QuerySerializer = (params: Record<string, unknown>, options?: Record<string, QueryParamStyle>) => string;
/**
 * The per-parameter cookie serialization metadata carried by the generated request. Cookies use the
 * OpenAPI `form` style, so only `explode` is configurable.
 */
export type CookieParamStyle = {
    explode?: boolean;
};
/**
 * The per-parameter header serialization metadata carried by the generated request. Headers use the
 * OpenAPI `simple` style, so only `explode` is configurable.
 *
 * @example
 * ```ts
 * // styles.header: { 'X-Ids': { explode: false } }, header [3, 4] -> 'X-Ids: 3,4'
 * // styles.header: { 'X-Filter': { explode: true } }, header { role: 'admin' } -> 'X-Filter: role=admin'
 * ```
 */
export type HeaderParamStyle = {
    explode?: boolean;
};
/**
 * The per-property `encoding` metadata for an `application/x-www-form-urlencoded` or
 * `multipart/form-data` request body. `contentType` overrides the part's media type; `style` /
 * `explode` / `allowReserved` follow the OpenAPI query rules for urlencoded bodies.
 */
export type BodyEncoding = SerializationStyle<QueryStyle> & {
    contentType?: string;
};
/**
 * Serializes the request body. JSON by default; `FormData`, `URLSearchParams`, `Blob`,
 * `ArrayBuffer`, and string bodies pass through untouched. The optional `encoding` argument carries
 * the per-property OpenAPI `encoding` metadata for form bodies.
 */
export type BodySerializer = (args: {
    body: unknown;
    contentType?: string;
    encoding?: Record<string, BodyEncoding>;
}) => BodyInit | undefined;
/**
 * The OpenAPI path-parameter serialization style. `simple` is the default and emits the bare value;
 * `label` prefixes a `.` and `matrix` prefixes a `;name=` segment.
 */
export type PathStyle = "simple" | "label" | "matrix";
/**
 * The per-parameter serialization metadata carried by the generated request. `style` selects the
 * OpenAPI style and `explode` controls how arrays and objects expand.
 */
export type PathParamStyle = SerializationStyle<PathStyle>;
/**
 * Serializes a single path parameter for interpolation into the URL, honoring the OpenAPI `style` /
 * `explode` passed as `options`. Defaults to `simple` style with `explode: false`: primitives are
 * URL-encoded, arrays join their members with commas, and objects flatten to `key,value` pairs.
 */
export type PathSerializer = (args: {
    name: string;
    value: unknown;
    options?: PathParamStyle;
}) => string;
/**
 * The per-concern serializers, grouped so they can be set in one place and overridden per client or
 * per call. Each field falls back to the matching `default*Serializer` when omitted.
 */
export type Serializers = {
    query?: QuerySerializer;
    body?: BodySerializer;
    path?: PathSerializer;
};
/**
 * The per-parameter OpenAPI `style` / `explode` metadata a generated request carries, grouped by
 * location and keyed by parameter name. Mirrors the `serializer` grouping and feeds the default
 * serializers; `body` carries the form `encoding` for a urlencoded or multipart body.
 */
export type Styles = {
    path?: Record<string, PathParamStyle>;
    query?: Record<string, QueryParamStyle>;
    header?: Record<string, HeaderParamStyle>;
    cookie?: Record<string, CookieParamStyle>;
    body?: Record<string, BodyEncoding>;
};
export declare function isDefaultJsonBody(body: unknown): boolean;
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
export declare const defaultBodySerializer: BodySerializer;
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
export declare function serializeCookies(cookies: Record<string, unknown>, styles?: Record<string, CookieParamStyle>): string;
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
export declare const defaultQuerySerializer: QuerySerializer;
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
export declare const defaultPathSerializer: PathSerializer;
/**
 * Serializes array and object header parameters with the OpenAPI `simple` style before they are
 * merged. Header values are not URL-encoded. Primitive values and headers without metadata pass
 * through untouched.
 */
export declare function applyHeaderStyles(headers: HeadersInit | undefined, styles: Record<string, HeaderParamStyle> | undefined): HeadersInit | undefined;
