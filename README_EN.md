# 🔥Model First

- [中文](README.md)
- [English](README_EN.md)

## More other engineering paradigms?
### Model First
Build RESTful, stateless, cloud-native, Docker-based, domain-driven applications with models, domains, and modules as first-class citizens around the revolutionary modern ORM.

### [Database First](https://github.com/ccmjga/mjga-scaffold)
Build RESTful, stateless, cloud-native, three-tier Docker-based architectural applications around type-safe, reusable, Debug-enabled SQL with databases as first-class citizens.
### 🥇 Model and domain-centered architecture design
![stack_modelfirstfirst.png](asset/architect_modelfirst.png)

### 😉 Interface files defined using Typescript
![stack_modelfirstfirst_en.png](asset/modelfirst_ts.png)

### 🍅 Optional field-specific components

![stack_en.png](asset/option_modelfirst_en.png)

### 🍹 Customizing meta information

![meta_enpng](asset/meta_modelfirst_en.png)


### Quick Start

```shell
# confirm .env and make sure process can use it
vim ${projectRoot}/.env
# confirm gradlew executable (unix-like OS)
chmod 755 ${projectRoot}/gradlew
```
**[Install Docker and start the container](https://docs.docker.com/engine/install/)**

```shell
cd ${projectRoot}
docker compose up -d database
docker compose build web
docker compose up -d web
````

**(Optional) Starting locally**
```shell
# confirm .env and make sure process can use it
docker compose up -d database
${projectRoot}/gradlew bootRun
```

### Common Tools

**Compile the project to generate Model derivatives and custom Dto's.**
```shell
# generate schema mapping codes
./gradlew build
# output ->
# projectRootDir/build/generated/sources/annotationProcessor/java/main
# └── your.package 
#    ├── model -> model artifact
#    └── dto -> dto artifact

```

**Global code formatting**
```shell
./gradlew spotlessApply
```
**Global code checking**
```shell
./gradlew pmdMain
```

**Unit test**
```shell
# will automatically generate jacocoTestReport
./gradlew test
```

[For more documents, please click...](https://www.mjga.cc/doc/modelfirst)

## 🍓 Test Report

![cover](https://www.mjga.cc/modelfirst/test-cover.png)

## 🍟 Miscellaneous

1. This repository is primarily for code display and issue collection. The code may lag behind the version available for
   download from the product's official website.
2. Please submit any feedback, discussions, or bugs to the issue tracker, and I will handle them seriously.
3. I also welcome any ideas from other communities and will actively participate in the replies.
4. More new video tutorials are being recorded, please stay tuned.

## 🔮 User Community

[![Static Badge](https://img.shields.io/badge/blog-black?style=flat&logo=dev.to&logoSize=auto)](https://dev.to/ccmjga)
[![Static Badge](https://img.shields.io/badge/homepage-white?style=flat&logo=homepage&logoColor=%23FF0074)](https://www.mjga.cc)
[![Static Badge](https://img.shields.io/badge/twitter-blue?style=flat&logo=x)](https://x.com/Mjga212318)
[![Static Badge](https://img.shields.io/badge/discord-white?style=flat&logo=discord)](https://discord.com/invite/3XhyjEPn)
