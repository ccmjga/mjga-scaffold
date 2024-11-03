val jooqVersion by extra("3.19.13")
val testcontainersVersion by extra("1.20.1")

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.springdoc.openapi-gradle-plugin") version "1.8.0"
    id("pmd")
    id("org.jooq.jooq-codegen-gradle") version "3.19.13"
    id("com.diffplug.spotless") version "6.25.0"
}

sourceSets {
    main {
        java {
            srcDir("build/generated-sources/jooq")
        }
    }
    test {
        java {
            srcDir("build/generated-sources/jooq")
        }
    }
}

group = "com.mjga"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.jooq:jooq-meta:$jooqVersion")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    jooqCodegen("org.postgresql:postgresql")
    jooqCodegen("org.jooq:jooq-codegen:$jooqVersion")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}

jacoco {
    toolVersion = "0.8.12"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

pmd {
    isConsoleOutput = true
    toolVersion = "7.5.0"
    rulesMinimumPriority.set(5)
    ruleSetFiles = files("pmd-rules.xml")
}

spotless {
    format("misc") {
        // define the files to apply `misc` to
        target("*.gradle.kts", "*.md", ".gitignore")
        // define the steps to apply to those files
        trimTrailingWhitespace()
        indentWithSpaces() // or spaces. Takes an integer argument if you don't like 4
        endWithNewline()
    }

    java {
        // don't need to set target, it is inferred from java

        // apply a specific flavor of google-java-format
        googleJavaFormat("1.17.0").reflowLongStrings()
        // fix formatting of type annotations
        formatAnnotations()
    }

    kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        ktlint() // or ktfmt() or prettier()
    }
}

jooq {
    configuration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://${providers.gradleProperty("database.hostPort").get()}/${
                providers.gradleProperty("database.name").get()
            }"
            user = providers.gradleProperty("database.user").get()
            password = providers.gradleProperty("database.password").get()
        }
        generator {
            database {
                includes = ".*"
                excludes = "qrtz_.*"
                inputSchema = "mjga"
                forcedTypes {
                    forcedType {
                        name = "varchar"
                        includeExpression = ".*"
                        includeTypes = "JSONB?"
                    }
                    forcedType {
                        name = "varchar"
                        includeExpression = ".*"
                        includeTypes = "INET"
                    }
                }
            }
            generate {
                // Generate the DAO classes
                isDaos = true
                isRecords = true
                isDeprecated = false
                isImmutablePojos = false
                isFluentSetters = true
                // Annotate DAOs (and other types) with spring annotations, such as @Repository and @Autowired
                // for auto-wiring the Configuration instance, e.g. from Spring Boot's jOOQ starter
                isSpringAnnotations = true

                // Generate Spring-specific DAOs containing @Transactional annotations
                isSpringDao = true
            }
            target {
                // The destination package of your generated classes (within the
                // destination directory)
                //
                // jOOQ may append the schema name to this package if generating multiple schemas,
                // e.g. org.jooq.your.packagename.schema1
                // org.jooq.your.packagename.schema2
                packageName = "org.jooq.generated"

                // The destination directory of your generated classes
//                directory = "build/generated-src/jooq"
            }
        }
    }
}
