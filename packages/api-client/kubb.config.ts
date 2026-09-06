import { adapterOas } from "@kubb/adapter-oas";
import { pluginFaker } from "@kubb/plugin-faker";
import { pluginFetch } from "@kubb/plugin-fetch";
import { pluginMsw } from "@kubb/plugin-msw";
import { pluginReactQuery } from "@kubb/plugin-react-query";
import { pluginTs } from "@kubb/plugin-ts";
import { pluginZod } from "@kubb/plugin-zod";
import { defineConfig } from "kubb/config";

const directory = { mode: "directory" as const, barrel: { type: "named" as const } };
const byTag = { type: "tag" as const };

export default defineConfig({
  input: "../../contracts/openapi/openapi.json",
  output: {
    path: "src/generated",
    clean: true,
    format: "prettier",
    lint: false,
  },
  adapter: adapterOas({
    validate: true,
    discriminator: "propagate",
    dateType: "string",
    integerType: "number",
    unknownType: "unknown",
    emptySchemaType: "unknown",
  }),
  plugins: [
    pluginTs({ output: { path: "types", ...directory } }),
    pluginZod({ output: { path: "schemas", ...directory } }),
    pluginFetch({
      output: { path: "clients", ...directory },
      group: byTag,
      validator: { request: "zod", response: "zod" },
    }),
    pluginReactQuery({
      output: { path: "queries", ...directory },
      group: byTag,
      client: "fetch",
      hooks: true,
    }),
    pluginFaker({
      output: { path: "fixtures", ...directory },
      include: [{ type: "schemaName", pattern: /.*/ }],
      seed: 20260905,
      locale: "en",
    }),
    pluginMsw({
      output: { path: "handlers", ...directory },
      group: byTag,
      baseURL: "*",
      parser: "data",
      handlers: false,
    }),
  ],
});
