import { describe, expect, it } from "vitest";
import { applicationName } from "./runtime-config";

describe("public runtime configuration", () => {
  it("uses the configured public application name", () => {
    expect(applicationName({ appName: "Orders" })).toBe("Orders");
  });

  it("falls back when the public name is absent", () => {
    expect(applicationName(undefined)).toBe("Contract First Scaffold");
  });
});
