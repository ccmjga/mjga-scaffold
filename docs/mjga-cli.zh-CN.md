# mjga-cli 中文使用指南

`mjga-cli` 是 MJGA 的 Contract First 命令行工具，用于创建和持续演进 Full-stack
Scaffold。它面向 Java/Kotlin 与 jOOQ/Jimmer 四种组合；Database First 和 Model First
属于 Archived Template Families，不是 CLI Contract Authoring 的目标。

[产品官网](https://www.mjga.cc/) ·
[GitHub 开源 Scaffold](https://github.com/ccmjga/mjga-scaffold) ·
[问题反馈](https://github.com/ccmjga/mjga-scaffold/issues) ·
[支持项目](https://www.mjga.cc/donate)

## 安装与登录

```sh
npm install --global mjga-cli
mjga login
mjga whoami
```

登录使用 Authorization Code with PKCE，CLI 不接收 MJGA 密码或浏览器 Cookie。macOS
凭据保存在 Login Keychain；其他平台回退到仅当前用户可读的 Credentials File。

Contract First 在登录后免费使用。Authenticated Access Lease 最长允许 30 天离线使用
Catalog、Recipe validation/export、Preview 和现有 Project Workspace 工具。`create` 与
`generate` 始终访问 live Generation Service；CLI 不提供 Offline Materialization。

## 创建 Full-stack Scaffold

```sh
mjga login
mjga create ./order-platform
cd ./order-platform
npm ci
docker compose up -d database
npm run check
```

生成项目包含以下边界：

- `apps/web`：React + Vite Web Application；
- `apps/server`：独立 Gradle Wrapper 的 Spring Boot Server；
- `packages/api-client`：从 OpenAPI 生成的 TypeScript Client；
- `contracts/openapi/openapi.json`：Canonical Full-stack API Contract；
- `compose.yaml`：PostgreSQL、Web 与 Server 的本地运行拓扑。

如需明确控制 Architecture Language、Persistence 和 Stack Options，可以使用 Project
Recipe：

```sh
mjga catalog
mjga recipe validate ./mjga-project.yaml
mjga recipe export ./mjga-project.yaml
mjga preview ./mjga-project.yaml
mjga create ./order-platform --recipe ./mjga-project.yaml
```

Catalog、validation/export 和 Preview 使用 CLI 内置的 Catalog v9 projection，可以在
Authenticated Access Lease 有效时断网运行。`create` 会由 Server 根据 live Catalog
重新校验 Recipe，并验证 Archive Digest、Recipe Identity 与安全解包边界。

## Contract Authoring 示例

所有修改命令均为 Preview First。先不带 `--apply` 查看 deterministic Generation Plan，
确认后再应用：

```sh
mjga add capability billing --package billing --schema billing --project .
mjga add capability billing --package billing --schema billing --project . --apply

mjga add use-case billing issue-invoice --kind command --transport http --project .
mjga add use-case billing issue-invoice --kind command --transport http --project . --apply

mjga add event billing invoice-issued --project . --apply
mjga evolve event billing invoice-issued --version 2 --project . --apply
mjga verify .
```

Contract Authoring 会协同维护 Project Recipe、Generation State、Capability Manifest、
Flyway migration、OpenAPI、Generated TypeScript Client inputs、Event Schema 与 Source
Ownership Boundary，不会静默覆盖 User-Owned Source。

还可以创建 Workflow、Read Model、Consumer Contract 或数据库角色约束：

```sh
mjga add workflow invoice-settlement --project . --apply
mjga add read-model account-balance --consume billing.invoice-issued.v1 --project . --apply
mjga contract consumer storefront --project . --apply
mjga harden database-roles --project . --apply
```

## 本地反馈与 Agent 工具

```sh
mjga workbench . --serve
mjga dev mock --project .
mjga dev mcp --project .
mjga verify .
```

- Contract Workbench：读取已提交契约并生成 HTML projection；
- OpenAPI Mock Server：校验 Request 并返回 deterministic contract-shaped Response；
- Development MCP：通过 stdio 提供 inspection 与 preview，不具备 apply authority；
- `mjga verify`：依次校验 Generated TypeScript Client、React Web 和 Spring Boot Server。

## Upgrade

```sh
mjga upgrade . --recipe <OpenRewrite-recipe>
mjga upgrade . --recipe <OpenRewrite-recipe> --apply
```

Upgrade 默认为 Dry Run，在 `.mjga/upgrades/` 保存 Manifest、Report 与 Patch。`--apply`
只应用经过审计的 Patch，随后运行同一套 Full-stack Verification；CLI 不创建 Commit，
也不会 Push Repository。

运行 `mjga help` 可查看当前版本支持的完整 Command Grammar。
