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
    implementation(platform("io.dropwizard:dropwizard-bom:4.0.15")) //ensure version of all dropwizard
    implementation("io.dropwizard:dropwizard-core") //core functionality like REST API, config
    implementation(kotlin("stdlib")) //basic kotlin functionality
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin") //json serialize and deserialize
    implementation("org.jdbi:jdbi3-kotlin:3.45.2") //sql orm (object relational mapper)
    implementation("io.dropwizard:dropwizard-jdbi3:4.0.7") //jdbi v3
    implementation("org.postgresql:postgresql:42.7.3") //postgres connection
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1") //java 8+ local date
    testImplementation(kotlin("test"))
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
    jvmArgs = listOf("-Duser.timezone=Asia/Kolkata")
}


