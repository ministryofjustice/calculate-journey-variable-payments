import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.1.2"
  kotlin("plugin.spring") version "2.0.21"
  kotlin("plugin.jpa") version "2.0.21"
  kotlin("plugin.allopen") version "2.0.21"
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.seleniumhq.selenium" && requested.name == "selenium-http") {
      useVersion("4.14.1")
    }
  }
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
    "com.amazonaws:aws-java-sdk-s3:1.12.663",
    "com.amazonaws:aws-java-sdk-sts:1.12.663",
    "io.sentry:sentry-spring-boot-starter:6.34.0",
    "net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion",
    "net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion",
    "nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0",
    "org.apache.poi:poi-ooxml:5.2.2",
    "org.flywaydb:flyway-database-postgresql",
    "org.springframework.boot:spring-boot-starter-security",
    "org.springframework.boot:spring-boot-starter-data-jpa",
    "org.springframework.boot:spring-boot-starter-thymeleaf",
    "org.springframework.boot:spring-boot-starter-oauth2-client",
    "org.springframework.boot:spring-boot-starter-oauth2-resource-server",
    "org.springframework.boot:spring-boot-starter-webflux",
    "org.springframework.session:spring-session-jdbc",
    "org.thymeleaf.extras:thymeleaf-extras-springsecurity6",
    "com.google.code.findbugs:jsr305:3.0.2",
    "com.microsoft.azure:applicationinsights-logging-logback:2.6.4",
    "org.apache.commons:commons-compress:1.26.0",
  ).forEach { implementation(it) }
  implementation(kotlin("script-runtime"))
  // Test versions
  val fluentleniumVersion = "5.0.4"
  val seleniumVersion = "4.13.0"
  listOf(
    "org.wiremock:wiremock:3.1.0",
    "net.sourceforge.htmlunit:htmlunit:2.70.0",
    "org.fluentlenium:fluentlenium-junit-jupiter:$fluentleniumVersion",
    "org.fluentlenium:fluentlenium-assertj:$fluentleniumVersion",
    "org.mockito:mockito-inline:5.2.0",
    "org.apache.commons:commons-compress:1.26.0",
    "org.seleniumhq.selenium:htmlunit-driver:4.13.0",
    "org.seleniumhq.selenium:selenium-java:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-http-jdk-client:4.13.0",
    "org.seleniumhq.selenium:selenium-api:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-remote-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-support:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-manager:$seleniumVersion",
    "org.springframework.boot:spring-boot-starter-test",
    "org.springframework.security:spring-security-test",
    "com.squareup.okhttp3:mockwebserver:4.11.0",
    "com.squareup.okhttp3:okhttp:4.11.0",
    "com.microsoft.playwright:playwright:1.50.0",
  ).forEach { testImplementation(it) }

  testRuntimeOnly("com.h2database:h2:1.4.200")

  runtimeOnly("org.postgresql:postgresql:42.7.4")
}
kotlin {
  jvmToolchain(21)
}

tasks {
  test {
    useJUnitPlatform()
    exclude("**/integration/*", "**/integrationplaywright/*")
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }

  val testIntegration by registering(Test::class) {
    testLogging.showStandardStreams = true
    testLogging.exceptionFormat = TestExceptionFormat.FULL
    useJUnitPlatform()

    include("uk/gov/justice/digital/hmpps/pecs/jpc/integration/*")
  }
  val testPlayWrightIntegration by registering(Test::class) {
    testLogging.showStandardStreams = true
    testLogging.exceptionFormat = TestExceptionFormat.FULL
    useJUnitPlatform()

    include("uk/gov/justice/digital/hmpps/pecs/jpc/integrationplaywright/*")
  }
}
