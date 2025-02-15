import org.springframework.boot.gradle.tasks.bundling.BootJar

val testcontainersVersion by extra("1.20.4")
val flywayVersion by extra("11.1.0")
val jimmerVersion by extra("0.9.50")
var springModulith by extra("1.3.2")

plugins {
    java
    `java-library`
    jacoco
    id("org.springframework.boot") version "3.3.8"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
    id("pmd")
    id("com.diffplug.spotless") version "7.0.2"
}

group = "com.zl.mjga"
version = "1.0.0"
description = "make java great again!"
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
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.springframework.modulith:spring-modulith-bom:$springModulith")
    implementation("org.springframework.modulith:spring-modulith-api:$springModulith")
    implementation("org.springframework.modulith:spring-modulith-starter-core:$springModulith")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test:$springModulith")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.babyfish.jimmer:jimmer-spring-boot-starter:$jimmerVersion")
    annotationProcessor("org.babyfish.jimmer:jimmer-apt:$jimmerVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    runtimeOnly("org.postgresql:postgresql")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    api("org.jspecify:jspecify:1.0.0")
}

tasks.withType<BootJar> {
    archiveFileName.set("app.jar")
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
    sourceSets = listOf(java.sourceSets.findByName("main"))
    isConsoleOutput = true
    toolVersion = "7.9.0"
    rulesMinimumPriority.set(5)
    ruleSetFiles = files("pmd-rules.xml")
}

spotless {
    format("misc") {
        // define the files to apply `misc` to
        target("*.gradle.kts", "*.md", ".gitignore")
        // define the steps to apply to those files
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    java {
        googleJavaFormat("1.25.2").reflowLongStrings()
        formatAnnotations()
    }

    kotlinGradle {
        target("*.gradle.kts") // default target for kotlinGradle
        ktlint() // or ktfmt() or prettier()
    }
}
