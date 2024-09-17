# Make Java Great Again!

- [English](README_EN.md)
- [中文](README_CN.md)

🧧🎖️🥇🏅🏆

![modern](https://img.shields.io/badge/Modern-blue) ![Lightweight](https://img.shields.io/badge/Lightweight-green) ![Test](https://img.shields.io/badge/Comprehensive_Testing-yellow) ![Meticulous coding](https://img.shields.io/badge/Meticulous_coding-red) ![Meticulous coding](https://img.shields.io/badge/Not_all_in_one-purple)

Mjga 是一款全新设计并打造的 Java Web 脚手架，带给你一种现代化的 Java 编程体验。

**脚手架主页**

[![portfolio](https://img.shields.io/badge/mjga-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://www.mjga.cc/)

**视频教程（持续更新中）**

[1. 快速启动脚手架与常用命令简介](https://www.bilibili.com/video/BV1Erpje8ERF/?share_source=copy_web\&vd_source=ad025dc4328969941f5a59161d7af2b7)

[2. 文件结构详解和 Docker 集成思路](https://www.bilibili.com/video/BV1octse4ERa/?share_source=copy_web\&vd_source=ad025dc4328969941f5a59161d7af2b7)


## 容器化与云原生 🍋

1. 通过 `docker-compose.yml` 管理应用程序的整个生命周期与配置。
2. 通过 `.env` 文件自定义所有配置。
3. 通过 `docker-compose.yml` 交付整个应用程序和配套的生态组件。

## 开箱即用 🍌

1. 集成了常用的基础业务功能，如身份验证、权限管理和缓存抽象。
2. Code Check&Format、CI/CD Plugin 、Docker Integration 全部开箱即用。
3. 完善的、从设计出发的、隔离了运行环境的单元测试。

## 现代化 🍒

1. 现代化：技术选型紧跟开源社区风向。
2. 可配置化：支持在网页上选配组件。
3. 考究的编码：每个变量和函数、每个模块和组件都考虑最佳实践。
4. Not-all-in-one：现在和未来，MJGA 都将守好自己的边界。

## 技术选型 🥝

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)

| 技术栈            | 版本号     |                |
|----------------|---------|----------------|
| OpenJdk        | 17/21   | LTS            |
| SpringBoot     | 3.3.3   | 核心框架           |
| SpringDoc      | 2.6.0   | 生成 OpenAPI 文档  |
| TestContainers | 1.20.1  | 为测试环境提供容器支持    |
| Jooq           | 3.19.11 | 生成类型安全的 SQL 查询 |
| Gradle         | 8.10.0  | 自动化构建工具        |
| Pmd            | 7.5.0   | 静态代码分析工具       |
| Spotless       | 6.25.0  | 代码格式化          |
| ...            | ...     | ...            |

## 快速开始 🍉

### 通过容器启动（推荐）

一、确认默认的环境变量配置。

```shell
# env will be applied to compose.yaml
less ${projectRoot}/.env
```

二、[安装 Docker。](https://docs.docker.com/engine/install/)

三、启动容器。

```shell
cd ${projectRoot}
docker-compose up -d
```

### 在本机启动（可选）

```shell
# confirm .env and make sure process can use it
nano/vim ${projectRoot}/.env
${projectRoot}/gradlew bootRun
```

### 创建数据库 Mapping Source

```shell
# confirm .env and make sure process can use it
nano/vim ${projectRoot}/.env
# generate schema mapping codes
./gradlew generateJooq
# output ->
# projectRootDir/build/generated-src
# └── jooq
#    └── tables # table mapping
#       ├── daos # Data Access Layer
#       ├── pojos # mapping dto
#       └── records # jooq query record

```

[更多文档请点击...](https://www.mjga.cc/doc/db-first)

## 测试覆盖率 🍓

![cover](https://www.mjga.cc/report/cover.png)

![summary](https://www.mjga.cc/report/summary.png)

## 产品社区 🔗

[![QQ](https://img.shields.io/badge/QQ-910248188-blue)](https://qm.qq.com/q/pz7B8vEL0k)
