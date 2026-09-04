import org.gradle.api.artifacts.dsl.LockMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

val jimmerVersion = "0.11.7"
val springModulithVersion = "2.1.1"

plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.spring") version "2.3.21"
    id("com.google.devtools.ksp") version "2.3.11"
    id("org.springframework.boot") version "4.1.1"
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
}

repositories { mavenCentral() }

configurations.matching { it.name.startsWith("test") }.configureEach {
    exclude(group = "org.mockito")
}

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
    implementation("org.babyfish.jimmer:jimmer-spring-boot-starter:$jimmerVersion")
    implementation("org.babyfish.jimmer:jimmer-sql-kotlin:$jimmerVersion")
    ksp("org.babyfish.jimmer:jimmer-ksp:$jimmerVersion")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("io.micrometer:micrometer-registry-otlp")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.mockito")
    }
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("io.kotest:kotest-property-jvm:6.1.4")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.testcontainers:testcontainers")
}

tasks.withType<BootJar> { archiveFileName.set("app.jar") }
tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Xshare:off", "--enable-native-access=ALL-UNNAMED", "-XX:+EnableDynamicAgentLoading")
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
    description = "Verifies Jimmer KSP output and committed Contract First projections."
    dependsOn("kspKotlin", architectureTest, contractTest, "verifyTypeScriptClient", "verifyCapabilityManifest")
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
        bundle("jimmer") { includeGroup("org.babyfish.jimmer") }
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
    jvmArgs.set(listOf("-Xshare:off", "--enable-native-access=ALL-UNNAMED", "-XX:+EnableDynamicAgentLoading"))
}

kover {
    reports {
        filters { includes { classes("*.platform.events.*") } }
        verify { rule("Kotlin platform coverage") { minBound(55) } }
    }
}
