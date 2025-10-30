repositories {
    mavenCentral()
    gradlePluginPortal() // ← 플러그인 해석 못할 때 도움이 됨 (보통 settings에서 선언)
}
plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"

    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"

    id("org.jetbrains.kotlin.plugin.noarg") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.0.0"
    id("org.flywaydb.flyway") version "10.17.0"

}
buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:10.17.0")
        classpath("org.postgresql:postgresql:42.7.4")
    }
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-core")      // 안전빵(Starter가 끌어오지만 명시)

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    val flywayVersion = "10.17.0"
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:10.17.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")   // ← 이 줄 꼭 있어야 함
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

}

flyway {
    // 앱에서 사용하는 spring.datasource.*와 동일하게!
    url = System.getenv("SPRING_DATASOURCE_URL")
        ?: "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require"
    user = System.getenv("SPRING_DATASOURCE_USERNAME") ?: "postgres.gsmbdibjiuwdqhrdvsfo"
    password = System.getenv("SPRING_DATASOURCE_PASSWORD") ?: "chikadol123!"

    // 필요 시 스키마/경로 지정
     schemas = arrayOf("rev")
     locations = arrayOf("classpath:db/migration")
}
tasks.test { useJUnitPlatform() }
