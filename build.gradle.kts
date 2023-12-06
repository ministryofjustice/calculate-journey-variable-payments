plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.9.0"
  kotlin("plugin.spring") version "1.9.21"
  kotlin("plugin.jpa") version "1.9.21"
  kotlin("plugin.allopen") version "1.9.21"
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.Embeddable")
  annotation("javax.persistence.MappedSuperclass")
}

dependencyCheck {
  suppressionFiles.add("calculate-journey-variable-payments-suppressions.xml")
}

dependencies {

  val shedlockVersion = "5.8.0"
  listOf(
    "com.beust:klaxon:5.6",
    "com.amazonaws:aws-java-sdk-s3:1.12.555",
    "com.amazonaws:aws-java-sdk-sts:1.12.555",
    "io.sentry:sentry-spring-boot-starter:6.29.0",
    "net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion",
    "net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion",
    "nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0",
    "org.apache.poi:poi-ooxml:5.2.2",
    "org.flywaydb:flyway-core",
    "org.springframework.boot:spring-boot-starter-security",
    "org.springframework.boot:spring-boot-starter-data-jpa",
    "org.springframework.boot:spring-boot-starter-thymeleaf",
    "org.springframework.boot:spring-boot-starter-oauth2-client",
    "org.springframework.boot:spring-boot-starter-oauth2-resource-server",
    "org.springframework.boot:spring-boot-starter-webflux",
    "org.springframework.session:spring-session-jdbc",
    "org.thymeleaf.extras:thymeleaf-extras-springsecurity6",
    "com.google.code.findbugs:jsr305:3.0.2",
    "com.microsoft.azure:applicationinsights-logging-logback:2.6.4"
  ).forEach { implementation(it) }
  implementation(kotlin("script-runtime"))

  // Test versions
  val fluentleniumVersion = "5.0.4"
  val seleniumVersion = "4.12.0"
  listOf(
    "org.wiremock:wiremock:3.1.0",
    "net.sourceforge.htmlunit:htmlunit:2.70.0",
    "org.fluentlenium:fluentlenium-junit-jupiter:$fluentleniumVersion",
    "org.fluentlenium:fluentlenium-assertj:$fluentleniumVersion",
    "org.mockito:mockito-inline:5.2.0",
    "org.seleniumhq.selenium:htmlunit-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-java:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-api:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-remote-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-support:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-chromium-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-manager:$seleniumVersion",
    "org.springframework.boot:spring-boot-starter-test",
    "org.springframework.security:spring-security-test",
    "com.squareup.okhttp3:mockwebserver:4.11.0",
    "com.squareup.okhttp3:okhttp:4.11.0"
  ).forEach { testImplementation(it) }

  testRuntimeOnly("com.h2database:h2:1.4.200")

  runtimeOnly("org.postgresql:postgresql:42.6.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(20))
}

tasks {
  test {
    useJUnitPlatform()
    exclude("**/integration/*")
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "20"
    }
  }

  val testIntegration by registering(Test::class) {
    useJUnitPlatform()
    include("uk/gov/justice/digital/hmpps/pecs/jpc/integration/*")
  }
}
