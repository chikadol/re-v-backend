plugins {
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"

    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    kotlin("plugin.jpa") version "2.0.0"

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
    // 풀러 URL (SSL 필수)
    url = "jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:6543/postgres?sslmode=require"

    // postgres.<프로젝트ref>
    user = "postgres.gsmbdibjiuwdqhrdvsfo"

    // DB 비밀번호
    password = "chikadol123!"

    // 스키마
    schemas = arrayOf("rev")

    // 마이그레이션 경로 (스프링 기본 사용 중이면 생략 가능)
    locations = arrayOf("classpath:db/migration")
}
tasks.test { useJUnitPlatform() }
