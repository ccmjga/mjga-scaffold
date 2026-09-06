/**
 * Thrown by `validateStandardSchema` when validation fails. Carries the raw `issues` array from
 * the schema's `validate` result so callers receive a uniform error shape regardless of which
 * schema library is in use.
 */
export class ParseError extends Error {
    issues;
    constructor({ issues, message, }) {
        super(message ?? "Validation failed");
        this.name = "ParseError";
        this.issues = issues;
    }
}
/**
 * Validates `value` against a Standard Schema-compatible `schema`. Returns the parsed output on
 * success; throws `ParseError` with the schema's `issues` on failure. Handles both sync and async
 * `validate` implementations transparently.
 *
 * @example
 * const pet = await validateStandardSchema(PetSchema, rawData)
 */
export async function validateStandardSchema(schema, value) {
    const result = await schema["~standard"].validate(value);
    if (result.issues) {
        throw new ParseError({ issues: result.issues });
    }
    return result.value;
}
