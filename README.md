# Make Java Great Again!
![modern](https://img.shields.io/badge/Modern-blue) ![Lightweight](https://img.shields.io/badge/Lightweight-green) ![Test](https://img.shields.io/badge/Comprehensive_Testing-yellow) ![Meticulous coding](https://img.shields.io/badge/Meticulous_coding-red) ![Meticulous coding](https://img.shields.io/badge/Not_all_in_one-purple)

[MJGA](https://www.mjga.cc) is a modern Java web scaffold built based on the following principles:

* Modern technology stack: The scaffold is designed to be international and keeps up with the trends in the open-source community.
* Lightweight: Select the components and modules you prefer on the website with a lightweight footprint.
* Comprehensive testing: Rigorous unit tests designed from the ground up and executed in isolated environments.
* Meticulous coding: Every variable, function, module, and component follows best practices.
* Not-all-in-one: MJGA maintains its boundaries to adapt to both present and future needs.

## Database First Template

[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](https://choosealicense.com/licenses/mit/) 

| Technology  | Version | Description                | 
|-------------|---------|----------------------------|
| OpenJdk     | 17      |                            | 
| SpringBoot  | 3.1.2   | Core framework             |
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
# change DATABASE_HOST_PORT
nano/vim ${projectRoot}/.env
${projectRoot}/gradlew bootRun
```

## Test coverage

![cover](https://www.mjga.cc/report/cover.png)

![summary](https://www.mjga.cc/report/summary.png)
## 🔗 
[![portfolio](https://img.shields.io/badge/mjga-000?style=for-the-badge&logo=ko-fi&logoColor=white)](https://www.mjga.cc/)
