# Make Java Great Again!

🧧🎖️🥇🏅🏆

![modern](https://img.shields.io/badge/Modern-blue) ![Lightweight](https://img.shields.io/badge/Lightweight-green) ![Test](https://img.shields.io/badge/Comprehensive_Testing-yellow) ![Meticulous coding](https://img.shields.io/badge/Meticulous_coding-red) ![Meticulous coding](https://img.shields.io/badge/Not_all_in_one-purple)

- [English](README_EN.md)
- [中文](README_CN.md)

[MJGA](https://www.mjga.cc) is a modern Java Web scaffold designed based on the following principles.

## Containerization and Cloud Native 🍋

1. Manage the entire lifecycle and configuration of the application through `docker-compose.yml`.
2. Customize all configurations through the `.env` file.
3. Deliver the entire application and its supporting ecosystem components through `docker-compose.yml`.

## Out of box 🍌

1. Integrated with commonly used basic business functions, such as authentication, permission management, and cache
   abstraction.
2. Code Check&Format, CI/CD Plugin, Docker Integration are all ready to use out of the box.
3. Comprehensive, design-oriented, and isolated runtime environment unit tests.

## Modern Philosophy 🍒

1. Modern: technology choices closely follow the trends in the open-source community.
2. Configurable: supports component selection and configuration on a web page.
3. Meticulous coding: considers best practices for every variable, function, module, and component.
4. Not-all-in-one: both now and in the future, [MJGA](https://www.mjga.cc) will maintain its boundaries.

## Stack 🥝

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/)

| Technology Stack | Version | Description                                       |
|------------------|---------|---------------------------------------------------|
| OpenJdk          | 17/21   | LTS                                               |
| SpringBoot       | 3.3.3   | Core Framework                                    |
| SpringDoc        | 2.2.6   | Generate OpenAPI Documentation                    |
| TestContainers   | 1.20.1  | Provide Container Support for Testing Environment |
| Jooq             | 3.19.11 | Generate Type-Safe SQL Queries                    |
| Gradle           | 8.10.0  | Automation Build Tool                             |
| Pmd              | 7.5.0   | Static Code Analysis Tool                         |
| Spotless         | 6.25.0  | Code Formatting                                   |
| ...              | ...     | ...                                               |

## Quick Start 🍉

### Start with Docker (Recommended)

1. Confirm the default environment variable configuration.

```shell
# env will be applied to compose.yaml
less ${projectRoot}/.env
```

2. [Install Docker.](https://docs.docker.com/engine/install/)

3. Start the container.

```shell
cd ${projectRoot}
docker-compose up -d
```

### Start Locally (Optional)

```shell
# confirm .env and make sure process can use it
nano/vim ${projectRoot}/.env
${projectRoot}/gradlew bootRun
```

### Generate DB Mapping Source

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

[More Document](https://www.mjga.cc/doc/db-first)

## Test coverage 🍓

![cover](https://www.mjga.cc/report/cover.png)

![summary](https://www.mjga.cc/report/summary.png)

## 🔗

[![portfolio](https://img.shields.io/badge/mjga-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://www.mjga.cc/)
