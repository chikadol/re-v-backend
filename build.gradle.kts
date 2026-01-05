//import jdk.tools.jlink.resources.plugins
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.language.jvm.tasks.ProcessResources


plugins {
    // Kotlin & Spring
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.spring") version "2.0.0"
    kotlin("plugin.jpa") version "1.9.25" // JPA엔 1.9.x 안정적
    id("org.jetbrains.kotlin.plugin.noarg") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.0.0"

    // Spring Boot & Dependency Management
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"

    // Flyway & Jacoco
    id("org.flywaydb.flyway") version "10.17.0"
    jacoco
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

// Kotlin 2.0 compilerOptions (kotlinOptions deprecated 대체)
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// JPA용 open/no-arg
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
    // --- Main ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Database (runtime)
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    // H2 Database (개발 환경용 - 인메모리)
    runtimeOnly("com.h2database:h2")

    // Flyway (runtime 마이그레이션)
    val flywayVersion = "10.17.0"
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    // OpenAPI (선택)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // JWT (선택)
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // HTML Parsing (크롤링용)
    implementation("org.jsoup:jsoup:1.17.2")
    
    // Selenium (브라우저 자동화 크롤링용)
    implementation("org.seleniumhq.selenium:selenium-java:4.15.0")
    implementation("io.github.bonigarcia:webdrivermanager:5.6.2")

    // OAuth2 Client (소셜 로그인)
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    
    // Redis (캐싱)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    
    // 구조화된 로깅 (JSON)
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    
    // Rate Limiting
    implementation("com.bucket4j:bucket4j-core:8.10.1")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit") // JUnit4 제외
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")

    // Mockito & Mockito-Kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
   // testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")

    // Jackson (테스트에서 objectMapper 필요 시)
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Testcontainers (리포지토리 테스트용)
    testImplementation("org.testcontainers:junit-jupiter:1.20.2")
    testImplementation("org.testcontainers:postgresql:1.20.2")

    // 스냅샷 테스트 (문제났던 좌표를 안정 버전으로 고정)
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("io.github.origin-energy:java-snapshot-testing-junit5:4.0.8")
    testImplementation("io.github.origin-energy:java-snapshot-testing-plugin-jackson:4.0.8")

    // H2 (원하면 in-memory 테스트 대체용)
    testRuntimeOnly("com.h2database:h2:2.3.232")

    // Kotlin Test
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Mockito 추가 의존성
    testImplementation("org.mockito:mockito-junit-jupiter:5.13.0")
    testImplementation("org.mockito:mockito-inline:5.2.0") // final class / final method 모킹용
}

tasks.test {
    useJUnitPlatform()
//    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    // buildDir deprecated 경고 회피
    val buildDirFile = layout.buildDirectory.get().asFile
    executionData.setFrom(
        fileTree(buildDirFile).include("jacoco/test.exec", "jacoco/*.exec")
    )
}

// ---- replace the old jacocoTestCoverageVerification block with this ----
val buildDirFile = layout.buildDirectory.get().asFile

val maybeExisting = tasks.findByName("jacocoTestCoverageVerification")
if (maybeExisting is JacocoCoverageVerification) {
    maybeExisting.apply {
        dependsOn(tasks.test)
        executionData.setFrom(fileTree(buildDirFile).include("jacoco/test.exec", "jacoco/*.exec"))
        sourceDirectories.setFrom(files("src/main/kotlin"))
        classDirectories.setFrom(files("build/classes/kotlin/main"))
        violationRules {
            rule {
                limit {
                    minimum = "0.60".toBigDecimal()
                }
            }
        }
    }
} else {
    tasks.register("jacocoTestCoverageVerification", JacocoCoverageVerification::class) {
        dependsOn(tasks.test)
        executionData.setFrom(fileTree(buildDirFile).include("jacoco/test.exec", "jacoco/*.exec"))
        sourceDirectories.setFrom(files("src/main/kotlin"))
        classDirectories.setFrom(files("build/classes/kotlin/main"))
        violationRules {
            rule {
                limit {
                    minimum = "0.60".toBigDecimal()
                }
            }
        }
    }
}


// Flyway Gradle 플러그인 설정
// 주의: Flyway Gradle 플러그인은 PostgreSQL 드라이버를 찾지 못할 수 있습니다.
// 해결 방법: Spring Boot 자동 마이그레이션 사용 (권장)
//   - application.yml에서 flyway.enabled: true 설정됨
//   - ./gradlew bootRun 실행 시 자동으로 마이그레이션 실행
// 또는 Flyway CLI 사용:
//   brew install flyway
//   flyway -url=jdbc:postgresql://... -user=... -password=... -schemas=rev migrate
flyway {
    locations = arrayOf("classpath:db/migration")
    // gradle.properties에서 설정을 읽어옴
    // PostgreSQL 드라이버 클래스패스 문제로 인해 Gradle 플러그인 사용 시 오류 발생 가능
}

sourceSets {
    test {
        resources {
            srcDir("src/test/resources")
        }
    }
}

tasks.named<org.gradle.language.jvm.tasks.ProcessResources>("processTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

