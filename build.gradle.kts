plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.8.8"
  kotlin("plugin.spring") version "1.8.0"
  kotlin("plugin.jpa") version "1.8.0"
  kotlin("plugin.allopen") version "1.8.0"
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

  val shedlockVersion = "4.42.0"
  listOf(
    "com.beust:klaxon:5.6",
    "com.amazonaws:aws-java-sdk-s3:1.12.279",
    "io.sentry:sentry-spring-boot-starter:6.4.2",
    "net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion",
    "net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion",
    "nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.1.0",
    "org.apache.poi:poi-ooxml:5.2.2",
    "org.flywaydb:flyway-core",
    "org.springframework.boot:spring-boot-starter-data-jpa",
    "org.springframework.boot:spring-boot-starter-thymeleaf",
    "org.springframework.boot:spring-boot-starter-oauth2-client",
    "org.springframework.boot:spring-boot-starter-oauth2-resource-server",
    "org.springframework.boot:spring-boot-starter-webflux",
    "org.springframework.session:spring-session-jdbc:2.7.0",
    "org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE"
  ).forEach { implementation(it) }
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
    "org.mockito:mockito-inline:4.11.0",
    "org.seleniumhq.selenium:htmlunit-driver:3.64.0",
    "org.seleniumhq.selenium:selenium-java:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-api:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-remote-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-support:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion",
    "org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion",
    "org.springframework.boot:spring-boot-starter-test",
    "org.springframework.security:spring-security-test",
    "com.squareup.okhttp3:mockwebserver:4.10.0",
    "com.squareup.okhttp3:okhttp:4.10.0"
  ).forEach { testImplementation(it) }
  constraints {
    implementation(" org.apache.commons:commons-text:1.10.0") {
      because("previous transitive version 1.9.0 pulled in by Fluentlenium has CVE-2022-42889")
    }
  }
  testRuntimeOnly("com.h2database:h2:2.1.214")

  runtimeOnly("org.postgresql:postgresql:42.5.4")
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
