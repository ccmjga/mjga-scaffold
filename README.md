# MJGA

MJGA 是面向 Human Developer 与 Coding Agent 的 Contract First Full-stack Scaffold
Platform。它将 Project Recipe、Canonical OpenAPI Contract、React Web、Spring Boot
Server 和 Generated TypeScript Client 组织成一个可以生成、验证和持续演进的工程边界。

[产品官网](https://www.mjga.cc/) · [npm CLI](https://www.npmjs.com/package/mjga-cli) ·
[CLI 中文指南](./docs/mjga-cli.zh-CN.md) · [Issues](https://github.com/ccmjga/mjga-scaffold/issues)

## 选择可运行的 Scaffold

以下分支不是源码模板，而是由 MJGA Generation Service 生成、完整验证并原子发布的
Default Scaffold。四个分支属于同一个 Contract First Template Family，区别仅在
Architecture Language 与 Persistence Adapter。

| 模板 | 代码分支 |
| --- | --- |
| Java + Jimmer | [`contract-first/java-jimmer`](https://github.com/ccmjga/mjga-scaffold/tree/contract-first/java-jimmer) |
| Java + jOOQ | [`contract-first/java-jooq`](https://github.com/ccmjga/mjga-scaffold/tree/contract-first/java-jooq) |
| Kotlin + Jimmer | [`contract-first/kotlin-jimmer`](https://github.com/ccmjga/mjga-scaffold/tree/contract-first/kotlin-jimmer) |
| Kotlin + jOOQ | [`contract-first/kotlin-jooq`](https://github.com/ccmjga/mjga-scaffold/tree/contract-first/kotlin-jooq) |

## 全栈架构

```text
contracts/openapi/openapi.json
             │
             ├── generates ──> packages/api-client ──> apps/web
             └── implemented by apps/server

compose.yaml ──> PostgreSQL + Web + Server
mjga-project.yaml + .mjga/ ──> Recipe, Generation State and ownership evidence
```

- **React + Vite Web Application**：TanStack Router/Query/Form、Zod，不直接拼装 Server API。
- **Spring Boot Server**：Java 或 Kotlin，Spring Modulith，PostgreSQL，jOOQ 或 Jimmer；
  `apps/server` 使用自己的 Gradle Wrapper，可独立构建。
- **Full-stack API Contract**：`contracts/openapi/openapi.json` 是跨栈 HTTP authority；
  `packages/api-client` 是可重建且经过测试的 Generated TypeScript Client。
- **Cloud-native Runtime**：Web 与 Server 使用独立、non-root OCI image；Caddy 提供同源
  `/api` routing；Compose 提供 health check 与 workstation runtime。
- **Human-Agent Ready**：Project Recipe、Generation State、Capability Manifest、Source
  Ownership 和 Verification Evidence 都是可提交、可检查、可复现的显式契约。

Default Scaffold 不虚构 Order、User 等业务模型。业务代码从真实 Capability 开始，并由
Contract Authoring 同步维护 OpenAPI、Event Schema、Migration 与 Generated Client inputs。

## 快速开始

选择任一代码分支后 clone，或通过官网/CLI 创建新项目。对已生成项目：

```sh
npm ci
docker compose up -d database
mjga add capability order-management --project . --apply
mjga add use-case order-management create-order --kind command --transport http --project . --apply
mjga verify .
npm run dev
```

## CLI 功能

```sh
npm install --global mjga-cli
mjga login
mjga catalog
mjga preview ./mjga-project.yaml
mjga create ./my-service --recipe ./mjga-project.yaml
```

`mjga-cli` 当前覆盖：

- Catalog discovery、Recipe validation/export、Generation Plan Preview；
- Online Generation、Archive Digest verification 与 atomic installation；
- Capability、Use Case、versioned Event、Workflow、Read Model、Consumer Contract；
- Database Role hardening、Extraction Assessment、Capability Migration Plan；
- Contract Workbench、OpenAPI Mock Server、read-only Development MCP；
- OpenRewrite Upgrade 与 Full-stack Verification。

所有修改命令都是 Preview First，只有显式 `--apply` 才能写入 Project Workspace。
Catalog、Recipe validation/export、Preview 与现有工作区工具可以在有效 Authenticated
Access Lease 内离线运行；`create` 和 `generate` 始终使用 live Generation Service。

CLI 完整示例参见 [中文指南](./docs/mjga-cli.zh-CN.md) 或
[npm package](https://www.npmjs.com/package/mjga-cli)。

## 产品与访问边界

- Contract First 是唯一持续演进的 Current Template Family，登录后免费，无需购买。
- Database First 与 Model First 已归档；只有历史 Archive Entitlement 可以读取旧订单资产。
- Donation 只是自愿支持项目，不授予产品权限，也不触发支付购买流程。
- Configurator、CLI 与 Remote MJGA MCP 共用 Generation Service 的认证和 Recipe authority。
- Remote MJGA MCP 只提供 Catalog 与 Planning，不生成 Archive，也不修改本地 Workspace。

打开 [MJGA Configurator](https://www.mjga.cc/) 可以选择 Java/Kotlin、jOOQ/Jimmer、JDK
和 Stack Options，预览 Project Recipe 与 Generation Plan 后下载完整工程。

## 仓库维护方式

本仓库是私有 MJGA Platform Source 发布出的 read-only Generated Code Projection：

- `main` 只保存产品说明和分支导航；
- 四个 `contract-first/*` 分支保存可运行的 Generated Scaffold；
- 问题必须在 Generation Source、Catalog 或 Base Template Variant 中修复，再重新生成；
- 四个 Scaffold 与 `main` 通过同一笔 atomic publication transaction 更新；
- 发布前必须通过 build、test、architecture、reproducibility、browser、system 与 container gates。

欢迎通过 [Issues](https://github.com/ccmjga/mjga-scaffold/issues) 提交问题，但不要把生成分支
当作手工维护的 Template Source。

当前投影来源：`08b516457bfb34745a2d3d8adb8ce45fb66541ee`
