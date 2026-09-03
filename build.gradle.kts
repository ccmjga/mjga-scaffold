import org.flywaydb.core.Flyway
import org.gradle.api.artifacts.dsl.LockMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

buildscript {
    repositories { mavenCentral() }
    configurations.classpath { resolutionStrategy.activateDependencyLocking() }
    dependencies {
        classpath("org.flywaydb:flyway-core:12.4.0")
        classpath("org.flywaydb:flyway-database-postgresql:12.4.0")
        classpath("org.postgresql:postgresql:42.7.13")
        classpath("org.testcontainers:testcontainers-postgresql:2.0.5")
    }
}

val jooqVersion = "3.21.7"
val springModulithVersion = "2.1.1"

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    id("org.jooq.jooq-codegen-gradle") version "3.21.7"
    id("com.diffplug.spotless") version "8.10.0"
    id("com.autonomousapps.dependency-analysis") version "3.18.0"
    id("dev.detekt") version "2.0.0-alpha.6"
    id("org.jetbrains.kotlinx.kover") version "0.9.9"
    id("info.solidsoft.pitest") version "1.19.0"
}

group = "com.zl.mjga"
version = "1.0.0"
description = "Human-Agent Ready Contract First Kotlin service"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        allWarningsAsErrors.set(true)
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
    sourceSets.main { kotlin.srcDir("build/generated-sources/jooq") }
}

val mockitoAgent = configurations.create("mockitoAgent")

repositories { mavenCentral() }

dependencyLocking {
    lockAllConfigurations()
    lockMode = LockMode.LENIENT
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    developmentOnly(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(platform("org.springframework.modulith:spring-modulith-bom:$springModulithVersion"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-context-support")
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("tools.jackson.core:jackson-databind")
    runtimeOnly("tools.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-events-api")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("io.micrometer:micrometer-registry-otlp")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.testcontainers:testcontainers")
    mockitoAgent(platform(SpringBootPlugin.BOM_COORDINATES))
    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
    jooqCodegen(platform(SpringBootPlugin.BOM_COORDINATES))
    jooqCodegen("org.jooq:jooq-codegen:$jooqVersion")
    jooqCodegen("org.postgresql:postgresql")
}

tasks.withType<BootJar> { archiveFileName.set("app.jar") }
tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Xshare:off", "--enable-native-access=ALL-UNNAMED", "-javaagent:${mockitoAgent.asPath}")
}
tasks.test { useJUnitPlatform { excludeTags("unit", "architecture", "integration", "contract") } }
tasks.named("detekt") { mustRunAfter(tasks.spotlessApply) }

fun registerTaggedTest(
    name: String,
    tag: String,
) = tasks.register<Test>(name) {
    group = "verification"
    description = "Runs the $tag Contract First Kotlin verification suite."
    testClassesDirs =
        sourceSets.test
            .get()
            .output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform { includeTags(tag) }
}

val unitTest = registerTaggedTest("unitTest", "unit")
val architectureTest = registerTaggedTest("architectureTest", "architecture")
val integrationTest = registerTaggedTest("integrationTest", "integration")
val contractTest = registerTaggedTest("contractTest", "contract")

tasks.register("verifyGenerated") {
    group = "verification"
    description = "Regenerates and verifies Kotlin jOOQ output and committed projections."
    dependsOn(tasks.jooqCodegen, architectureTest, contractTest, "verifyTypeScriptClient", "verifyCapabilityManifest")
}
tasks.register<Exec>("verifyCapabilityManifest") {
    group = "verification"
    commandLine("node", "scripts/verify-capabilities.mjs")
}
tasks.register<Exec>("verifyTypeScriptClient") {
    group = "verification"
    commandLine("node", "clients/typescript/scripts/verify.mjs")
}

tasks.register<Exec>("verifyKotlinQuality") {
    group = "verification"
    description = "Enforces Kotlin coverage, CRAP, and mutation baselines."
    dependsOn("koverXmlReport", "detekt", "pitest")
    commandLine(
        "node",
        "scripts/verify-kotlin-quality.mjs",
        "build/reports/kover/report.xml",
        "build/reports/pitest/mutations.xml",
    )
}
tasks.check {
    dependsOn(
        unitTest,
        architectureTest,
        integrationTest,
        contractTest,
        "verifyGenerated",
    )
}
tasks.compileKotlin {
    if (!providers.gradleProperty("skipJooqCodegen").isPresent) dependsOn(tasks.jooqCodegen)
}
tasks.matching { it.name == "explodeCodeSourceMain" || it.name == "explodeCodeSourceTest" }.configureEach {
    mustRunAfter(tasks.jooqCodegen)
}

val jooqInputSchemas =
    fileTree("src/main/resources/db") { include("**/*.sql") }
        .files
        .sortedBy { it.invariantSeparatorsPath }
        .flatMap { migration ->
            Regex("(?i)create\\s+schema\\s+if\\s+not\\s+exists\\s+([a-z][a-z0-9_]*)")
                .findAll(migration.readText())
                .map { it.groupValues[1] }
        }.distinct()
val codegenPostgres =
    PostgreSQLContainer(
        DockerImageName
            .parse(
                "postgres:18.6@sha256:4ef4dbc939d61acea57712655ddb4b4ab27419c913f94cca0cd57cb3ea3c2280",
            ).asCompatibleSubstituteFor("postgres"),
    ).withDatabaseName("mjga_codegen")
val codegenJdbcUrl get() = "${codegenPostgres.jdbcUrl}?options=-c%20client_min_messages=warning"
val prepareJooqDatabase =
    tasks.register("prepareJooqDatabase") {
        notCompatibleWithConfigurationCache("Owns a short-lived PostgreSQL Testcontainer")
        inputs.files(fileTree("src/main/resources/db") { include("**/*.sql") })
        doLast {
            codegenPostgres.start()
            inputs.files.files
                .map { it.parentFile }
                .distinct()
                .sortedBy { it.invariantSeparatorsPath }
                .forEach { location ->
                    val schemas =
                        location
                            .walkTopDown()
                            .filter { it.isFile && it.extension == "sql" }
                            .flatMap { migration ->
                                Regex("(?i)create\\s+schema\\s+if\\s+not\\s+exists\\s+([a-z][a-z0-9_]*)")
                                    .findAll(migration.readText())
                                    .map { it.groupValues[1] }
                            }.distinct()
                            .toList()
                    val schema =
                        schemas.singleOrNull()
                            ?: error("Migration directory ${location.name} must declare exactly one schema: $schemas")
                    Flyway
                        .configure()
                        .dataSource(codegenJdbcUrl, codegenPostgres.username, codegenPostgres.password)
                        .schemas(schema)
                        .table("flyway_schema_history")
                        .locations("filesystem:${location.absolutePath}")
                        .load()
                        .migrate()
                }
        }
    }
val stopJooqDatabase =
    tasks.register("stopJooqDatabase") {
        notCompatibleWithConfigurationCache("Stops the PostgreSQL Testcontainer owned by code generation")
        doLast {
            if (codegenPostgres.isRunning) codegenPostgres.stop()
        }
    }
tasks.jooqCodegen {
    dependsOn(prepareJooqDatabase)
    finalizedBy(stopJooqDatabase)
}

spotless {
    format("misc") {
        target("*.gradle.kts", "*.md", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin {
        target("src/**/*.kt")
        ktlint("1.8.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.8.0")
    }
}

dependencyAnalysis {
    structure {
        bundle("spring-boot") { includeGroup("org.springframework.boot") }
        bundle("spring-modulith") { includeGroup("org.springframework.modulith") }
        bundle("testcontainers") { includeGroup("org.testcontainers") }
        bundle("kotlin") { includeGroup("org.jetbrains.kotlin") }
    }
    issues {
        all {
            onUnusedDependencies { severity("fail") }
            onUsedTransitiveDependencies { severity("ignore") }
            onIncorrectConfiguration { severity("fail") }
            onCompileOnly { severity("fail") }
            onRuntimeOnly { severity("fail") }
            onUnusedAnnotationProcessors { severity("fail") }
            onRedundantPlugins { severity("fail") }
        }
    }
}

pitest {
    pitestVersion.set("1.25.8")
    junit5PluginVersion.set("1.2.3")
    targetClasses.set(setOf("*.platform.events.*"))
    targetTests.set(setOf("*.platform.events.*Test"))
    outputFormats.set(setOf("HTML", "XML"))
    timestampedReports.set(false)
    mutationThreshold.set(41)
    coverageThreshold.set(55)
    jvmArgs.set(listOf("-Xshare:off", "--enable-native-access=ALL-UNNAMED", "-javaagent:${mockitoAgent.asPath}"))
}

kover {
    reports {
        filters { includes { classes("*.platform.events.*") } }
        verify { rule("Kotlin platform coverage") { minBound(55) } }
    }
}

jooq {
    configuration {
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                includes = ".*"
                isIncludeRoutines = false
                schemata {
                    jooqInputSchemas.forEach { schemaName ->
                        schema { inputSchema = schemaName }
                    }
                }
            }
            generate {
                isDeprecated = false
                isRecords = true
                isKotlinNotNullRecordAttributes = true
                isKotlinNotNullPojoAttributes = true
            }
            target { packageName = "com.zl.mjga.generated.persistence" }
        }
    }
    delayedConfiguration {
        jdbc {
            driver = "org.postgresql.Driver"
            url = codegenJdbcUrl
            user = codegenPostgres.username
            password = codegenPostgres.password
        }
    }
}
