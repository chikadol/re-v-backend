repositories {
    mavenCentral()
    gradlePluginPortal() // ← 플러그인 해석 못할 때 도움이 됨 (보통 settings에서 선언)
}
plugins {
/*    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"*/
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("jvm") version "2.0.0" // 프로젝트에 맞게
    kotlin("plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
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
    implementation("junit:junit:4.12")

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
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation(kotlin("test"))

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 테스트에서 in-memory로 사용

    // Mockito-Kotlin (Kotlin-friendly any(), whenever 등)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    // WebMvcTest에서 PageImpl 사용 시 필요
    testImplementation("org.springframework.data:spring-data-commons")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0") // 선택
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("org.testcontainers:postgresql:1.20.1")
    testImplementation("au.com.origin.snapshots:java-junit5:3.1.0") // 선택
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
