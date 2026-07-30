plugins {
    java

    id("org.springframework.boot") version "4.1.0"

    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.facthub"
version = "0.0.1-SNAPSHOT"

description = "FactHub"

java {
    toolchain {
        languageVersion.set(
            JavaLanguageVersion.of(21)
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {

    /*
     * Spring MVC 기반 REST API
     *
     * Controller, RestClient, Jackson,
     * 내장 Tomcat 등을 포함한다.
     */
    implementation(
        "org.springframework.boot:" +
                "spring-boot-starter-webmvc"
    )

    /*
     * Spring Data JPA / Hibernate / HikariCP
     */
    implementation(
        "org.springframework.boot:" +
                "spring-boot-starter-data-jpa"
    )

    /*
     * 세션 인증 / 인가 / CSRF
     */
    implementation(
        "org.springframework.boot:" +
                "spring-boot-starter-security"
    )

    /*
     * @Valid, @NotBlank, @Size 등
     */
    implementation(
        "org.springframework.boot:" +
                "spring-boot-starter-validation"
    )

    /*
     * 회원가입 이메일 인증 메일 발송
     */
    implementation(
        "org.springframework.boot:" +
                "spring-boot-starter-mail"
    )

    /*
     * /actuator/health 등 운영 상태 확인
     */
    implementation(
        "org.springframework.boot:" +
                "spring-boot-starter-actuator"
    )

    /*
     * Flyway 자동 설정
     */
    implementation(
        "org.springframework.boot:" +
                "spring-boot-starter-flyway"
    )

    /*
     * Flyway의 MySQL 지원
     */
    runtimeOnly(
        "org.flywaydb:flyway-mysql"
    )

    /*
     * MySQL JDBC 드라이버
     */
    runtimeOnly(
        "com.mysql:mysql-connector-j"
    )

    /*
     * 개발 중 자동 재시작
     */
    developmentOnly(
        "org.springframework.boot:" +
                "spring-boot-devtools"
    )

    /*
     * JUnit 5 / Mockito / AssertJ / Spring Test
     */
    testImplementation(
        "org.springframework.boot:" +
                "spring-boot-starter-test"
    )

    /*
     * @WithMockUser 등 Security 테스트
     */
    testImplementation(
        "org.springframework.security:" +
                "spring-security-test"
    )

    testRuntimeOnly("com.h2database:h2")

    /*
     * JUnit Platform 실행기
     */
    testRuntimeOnly(
        "org.junit.platform:" +
                "junit-platform-launcher"
    )
}

/*
 * Java 소스 인코딩 및
 * 메서드 매개변수 이름 보존
 */
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    options.compilerArgs.add(
        "-parameters"
    )
}

/*
 * JUnit Platform 사용
 */
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

/*
 * 실행 JAR 파일명 고정
 */
tasks.named<
        org.springframework.boot.gradle.tasks.bundling.BootJar
        >("bootJar") {
    archiveFileName.set(
        "facthub.jar"
    )
}
