/**
 * A Standard Schema-compatible validator: a minimal duck-type covering Zod v3/v4, valibot, and
 * arktype schemas. Only the `~standard.validate` method is required at runtime.
 */
export type StandardSchemaValidator<TOutput = unknown> = {
  readonly "~standard": {
    validate(
      value: unknown,
    ): StandardSchemaResult<TOutput> | Promise<StandardSchemaResult<TOutput>>;
  };
};

/**
 * The two possible outcomes of a Standard Schema `validate` call. A successful result carries
 * `value`; a failed result carries `issues`.
 */
export type StandardSchemaResult<TOutput> =
  | { readonly value: TOutput; readonly issues?: undefined }
  | { readonly issues: ReadonlyArray<StandardSchemaIssue> };

/**
 * One validation issue from a Standard Schema `validate` call.
 */
export type StandardSchemaIssue = {
  readonly message?: string;
  readonly path?: ReadonlyArray<PropertyKey | { readonly key: PropertyKey }>;
};

/**
 * Thrown by `validateStandardSchema` when validation fails. Carries the raw `issues` array from
 * the schema's `validate` result so callers receive a uniform error shape regardless of which
 * schema library is in use.
 */
export class ParseError extends Error {
  readonly issues: ReadonlyArray<StandardSchemaIssue>;

  constructor({
    issues,
    message,
  }: {
    issues: ReadonlyArray<StandardSchemaIssue>;
    message?: string;
  }) {
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
export async function validateStandardSchema<TOutput>(
  schema: StandardSchemaValidator<TOutput>,
  value: unknown,
): Promise<TOutput> {
  const result = await schema["~standard"].validate(value);
  if (result.issues) {
    throw new ParseError({ issues: result.issues });
  }
  return (result as { value: TOutput }).value;
}
