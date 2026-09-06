export function applicationName(
  config: Window["__MJGA_RUNTIME_CONFIG__"],
): string {
  const value = config?.appName?.trim();
  return value || "Contract First Scaffold";
}
