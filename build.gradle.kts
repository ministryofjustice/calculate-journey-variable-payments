plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.1.0"
  kotlin("plugin.spring") version "1.8.10"
  kotlin("plugin.jpa") version "1.8.10"
  kotlin("plugin.allopen") version "1.8.10"
}

allOpen {
  annotation("jakarta.persistence.Entity")
  annotation("jakarta.persistence.Embeddable")
  annotation("jakarta.persistence.MappedSuperclass")
}

dependencyCheck {
  suppressionFiles.add("calculate-journey-variable-payments-suppressions.xml")
}

dependencies {

  val shedlockVersion = "5.1.0"
  listOf(
    "com.beust:klaxon:5.6",
    "com.amazonaws:aws-java-sdk-s3:1.12.408",
    "io.sentry:sentry-spring-boot-starter:6.14.0",
    "net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion",
    "net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion",
    "nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.2.0",
    "org.apache.poi:poi-ooxml:5.2.3",
    "org.flywaydb:flyway-core",
    "org.springframework.boot:spring-boot-starter-data-jpa",
    "org.springframework.boot:spring-boot-starter-thymeleaf",
    "org.springframework.boot:spring-boot-starter-oauth2-client",
    "org.springframework.boot:spring-boot-starter-oauth2-resource-server",
    "org.springframework.boot:spring-boot-starter-webflux",
    "org.springframework.session:spring-session-jdbc:3.0.0",
    "org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.1.1.RELEASE"
  ).forEach { implementation(it) { exclude("org.springframework.boot", "spring-boot-starter-logging") } }
  implementation(kotlin("script-runtime"))

  constraints {
    implementation("org.apache.groovy:groovy:4.0.2") {
      because("previous transitive version 4.0.0 pulled in by Thymeleaf has CVE-2020-17521")
    }
  }

  // Test versions
  val fluentleniumVersion = "4.8.0"
  val seleniumVersion = "3.141.59"
  listOf(
    "com.github.tomakehurst:wiremock-standalone:2.27.2",
    "org.fluentlenium:fluentlenium-junit-jupiter:$fluentleniumVersion",
    "org.fluentlenium:fluentlenium-assertj:$fluentleniumVersion",
    "org.mockito:mockito-inline:4.8.0",
    "org.seleniumhq.selenium:htmlunit-driver:2.61.0",
    "org.seleniumhq.selenium:selenium-java:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-api:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-remote-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-support:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion",
    "org.springframework.boot:spring-boot-starter-test",
    "org.springframework.security:spring-security-test",
    "com.squareup.okhttp3:mockwebserver:4.10.0",
    "com.squareup.okhttp3:okhttp:4.10.0"
  ).forEach { testImplementation(it) { exclude(" org.slf4j.helpers", "NOPLoggerFactory") } }
  constraints {
    implementation(" org.apache.commons:commons-text:1.10.0") {
      because("previous transitive version 1.9.0 pulled in by Fluentlenium has CVE-2022-42889")
    }
  }
  testRuntimeOnly("com.h2database:h2:1.4.200")

  runtimeOnly("org.postgresql:postgresql:42.5.4")
}
configurations {
  all {
    exclude(module = "spring-boot-starter-logging")
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  test {
    useJUnitPlatform()
    exclude("**/integration/*")
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }

  val testIntegration by registering(Test::class) {
    useJUnitPlatform()
    include("uk/gov/justice/digital/hmpps/pecs/jpc/integration/*")
  }
}
