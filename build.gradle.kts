import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.artifacts.dsl.LockMode
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

val jooqVersion = "3.21.7"
val springModulithVersion = "2.1.1"

plugins {
    java
    jacoco
    id("org.springframework.boot") version "4.1.1"
    id("pmd")
    id("org.jooq.jooq-codegen-gradle") version "3.21.7"
    id("com.diffplug.spotless") version "8.10.0"
    id("com.autonomousapps.dependency-analysis") version "3.18.0"
    id("net.ltgt.errorprone") version "5.1.0"
}

group = "com.zl.mjga"
version = "1.0.0"
description = "Human-Agent Ready Contract First service"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

sourceSets.main { java.srcDir("build/generated-sources/jooq") }
sourceSets.test { java.srcDir("build/generated-sources/jooq") }

configurations.compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
val mockitoAgent = configurations.create("mockitoAgent")

repositories { mavenCentral() }

dependencyLocking {
    lockAllConfigurations()
    lockMode = LockMode.LENIENT
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    annotationProcessor(platform(SpringBootPlugin.BOM_COORDINATES))
    developmentOnly(platform(SpringBootPlugin.BOM_COORDINATES))
    implementation(platform("org.springframework.modulith:spring-modulith-bom:$springModulithVersion"))
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
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springframework.modulith:spring-modulith-events-api")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("io.micrometer:micrometer-registry-otlp")
    compileOnly("org.jspecify:jspecify:1.0.1")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
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
    jooqCodegen("org.jooq:jooq-meta-extensions:$jooqVersion")
    errorprone("com.google.errorprone:error_prone_core:2.50.0")
    errorprone("com.uber.nullaway:nullaway:0.13.8")
}

tasks.withType<BootJar> { archiveFileName.set("app.jar") }
tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Xshare:off", "--enable-native-access=ALL-UNNAMED", "-javaagent:${mockitoAgent.asPath}")
}
tasks.test { useJUnitPlatform { excludeTags("unit", "architecture", "integration", "contract") } }

fun registerTaggedTest(
    name: String,
    tag: String,
) = tasks.register<Test>(name) {
    group = "verification"
    description = "Runs the $tag Contract First verification suite."
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
    description = "Regenerates and verifies committed machine-readable Contract First projections."
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

tasks.check { dependsOn(unitTest, architectureTest, integrationTest, contractTest, "verifyGenerated") }

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.compilerArgs.addAll(listOf("-Xlint:all,-processing", "-Werror"))
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        excludedPaths.set(".*/build/generated(?:-sources)?/.*")
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:AnnotatedPackages", "com.zl.mjga")
        if (name.contains("test", ignoreCase = true)) disable("NullAway")
    }
}

tasks.compileJava { dependsOn(tasks.jooqCodegen) }
tasks
    .matching { it.name == "explodeCodeSourceMain" || it.name == "explodeCodeSourceTest" }
    .configureEach { dependsOn(tasks.jooqCodegen) }
tasks.jacocoTestReport {
    dependsOn(tasks.test, unitTest, architectureTest, integrationTest, contractTest)
    executionData(fileTree(layout.buildDirectory.dir("jacoco")) { include("*.exec") })
}
tasks.check { dependsOn(tasks.jacocoTestReport) }

jacoco {
    toolVersion = "0.8.14"
    reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
}

pmd {
    sourceSets = listOf(java.sourceSets.getByName("main"))
    isConsoleOutput = true
    toolVersion = "7.25.0"
    rulesMinimumPriority.set(5)
    ruleSetFiles = files("pmd-rules.xml")
}

spotless {
    format("misc") {
        target("*.gradle.kts", "*.md", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    java {
        target("src/**/*.java")
        googleJavaFormat("1.35.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

dependencyAnalysis {
    structure {
        bundle("spring-boot") { includeGroup("org.springframework.boot") }
        bundle("spring-modulith") { includeGroup("org.springframework.modulith") }
        bundle("testcontainers") { includeGroup("org.testcontainers") }
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

jooq {
    configuration {
        generator {
            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                includes = ".*"
                properties {
                    property {
                        key = "scripts"
                        value = "src/main/resources/db/**/*.sql"
                    }
                    property {
                        key = "sort"
                        value = "semantic"
                    }
                    property {
                        key = "defaultNameCase"
                        value = "lower"
                    }
                }
            }
            generate {
                isDeprecated = false
                isRecords = true
            }
            target { packageName = "com.zl.mjga.generated.persistence" }
        }
    }
}
