plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.freefair.lombok") version "8.14"
}

fun getGitHash(): String {
	return providers.exec {
		commandLine("git", "rev-parse", "--short", "HEAD")
	}.standardOutput.asText.get().trim()
}

group = "kr.hhplus.be"
version = getGitHash()

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
	}
}

dependencies {
    // Spring
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
    // DB
	runtimeOnly("com.mysql:mysql-connector-j")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mysql")
	testImplementation("org.testcontainers:testcontainers:1.19.0")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")

	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0")

	// H2 Database
	runtimeOnly("com.h2database:h2")
	// docker
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	implementation("org.springframework.retry:spring-retry:1.3.3")
	implementation("org.springframework:spring-aspects:5.3.22")

	// queryDSL
	annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")

	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
	implementation("org.glassfish:jakarta.el:4.0.2")
	implementation("jakarta.validation:jakarta.validation-api:3.0.2")
	testImplementation("org.testcontainers:junit-jupiter:1.19.0")
	implementation("org.redisson:redisson-spring-boot-starter:3.37.0")

}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("user.timezone", "UTC")
}
