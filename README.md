# Make Java Great Again!
![modern](https://img.shields.io/badge/Modern-blue) ![Lightweight](https://img.shields.io/badge/Lightweight-green) ![Test](https://img.shields.io/badge/Comprehensive_Testing-yellow) ![Meticulous coding](https://img.shields.io/badge/Meticulous_coding-red) ![Meticulous coding](https://img.shields.io/badge/Not_all_in_one-purple)

[MJGA](https://www.mjga.cc) is a modern Java web scaffold built based on the following principles:

### One philosophy
1. Manages the entire lifecycle of an application and its environment through docker-compose.yml.
2. Customizes all property variables through the .env file.

### Out of box:

1. Foundational business functions such as authentication, permission management, and cache abstraction.
2. Code Check&Format, CI/CD Plugin, and Docker Integration, all ready to use out of the box.
3. Comprehensive unit testing that is well-designed and isolates the runtime environment.

### Modern, Configurable, Meticulous coding, Not-all-in-one

1. Modern: technology choices closely follow the trends in the open-source community.
2. Configurable: supports component selection and configuration on a web page.
3. Meticulous coding: considers best practices for every variable, function, module, and component.
4. Not-all-in-one: both now and in the future, [MJGA](https://www.mjga.cc) will maintain its boundaries.

## Database First

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/) 

| Technology  | Version | Description                | 
|-------------|---------|----------------------------|
| OpenJdk     | 17      |                            | 
| SpringBoot  | 3.2.1   | Core framework             |
| Jooq        | 3.18.6  | Generates type-safe SQL queries |
| Gradle      | 8.4     | Automation build tool      |
| Pmd         | 6.55.0  | Static code analysis tool  |
| Spotless    | 6.18.0  | Code formatting            |
| ...         | ...     | ...                        |

## Usage

### Docker
Confirm default environment variables
```shell
# env will be applied to compose.yaml
less ${projectRoot}/.env
```

[Install Docker Engine and start the container](https://docs.docker.com/engine/install/)

```shell
cd ${projectRoot}
docker-compose up -d
```

### Local

(Optional) Start on local machine

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



## Test coverage

![cover](https://www.mjga.cc/report/cover.png)

![summary](https://www.mjga.cc/report/summary.png)
## 🔗 
[![portfolio](https://img.shields.io/badge/mjga-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://www.mjga.cc/)
