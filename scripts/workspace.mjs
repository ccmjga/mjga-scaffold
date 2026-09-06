import { spawn } from "node:child_process";
import process from "node:process";

const [scope, action] = process.argv.slice(2);

if (scope === "server") {
  const task = { build: "bootJar", test: "test", check: "check" }[action];
  if (!task) throw new Error(`Unknown Server command ${action}`);
  await run(process.platform === "win32" ? "gradlew.bat" : "./gradlew", [task, "--warning-mode=fail", "--no-daemon"], "apps/server");
} else if (scope === "compose") {
  if (action === "up") await run("docker", ["compose", "up", "--build", "--detach", "--wait"]);
  else if (action === "down") await run("docker", ["compose", "down"]);
  else if (action === "verify") {
    const port = process.env.WEB_EXPOSE_PORT ?? "3000";
    const response = await fetch(`http://127.0.0.1:${port}/api/v1/platform/status`);
    if (!response.ok) throw new Error(`Scaffold status returned ${response.status}`);
    const status = await response.json();
    if (status.status !== "UP") throw new Error("Scaffold status is not UP");
    console.log("Full-stack Scaffold is healthy.");
  } else throw new Error(`Unknown Compose command ${action}`);
} else if (scope === "dev") {
  await run("npm", ["run", "dev", "--workspace", "@mjga/web"]);
} else {
  throw new Error(`Unknown workspace command ${[scope, action].filter(Boolean).join(" ")}`);
}

function run(command, args, cwd = process.cwd()) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, { cwd, stdio: "inherit", shell: process.platform === "win32" });
    child.once("error", reject);
    child.once("exit", (code) => code === 0 ? resolve() : reject(new Error(`${command} exited with ${code}`)));
  });
}
