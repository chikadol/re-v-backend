plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    kotlin("plugin.jpa") version "2.0.0"
}

group = "com.rev"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("io.micrometer:micrometer-tracing-bridge-otel:1.3.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
    implementation("org.locationtech.jts:jts-core:1.19.0")
    implementation("com.github.javafaker:javafaker:1.0.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
