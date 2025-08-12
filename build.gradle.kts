import org.gradle.api.tasks.JavaExec

plugins {
    kotlin("jvm") version "2.1.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

application{
    mainClass.set("ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("io.dropwizard:dropwizard-bom:4.0.15"))
    implementation("io.dropwizard:dropwizard-core")
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("io.dropwizard:dropwizard-testing:4.0.7")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

tasks.named<JavaExec>("run") {
    args = listOf("server", "src/main/resources/config.yml")
}