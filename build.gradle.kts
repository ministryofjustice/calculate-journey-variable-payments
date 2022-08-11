plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.4.1"
  kotlin("plugin.spring") version "1.7.10"
  kotlin("plugin.jpa") version "1.7.10"
  kotlin("plugin.allopen") version "1.7.10"
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
  implementation("com.beust:klaxon:5.6")
  implementation("com.amazonaws:aws-java-sdk-s3:1.12.279")
  implementation("io.sentry:sentry-spring-boot-starter:6.3.1")
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.39.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.39.0")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.1.0")
  implementation("org.apache.poi:poi-ooxml:5.2.2")
  implementation("org.flywaydb:flyway-core")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.session:spring-session-jdbc:2.7.0")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
  implementation(kotlin("script-runtime"))

  constraints {
    implementation("org.apache.groovy:groovy:4.0.2") {
      because("previous transitive version 4.0.0 pulled in by Thymeleaf has CVE-2020-17521")
    }
  }

  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.fluentlenium:fluentlenium-junit-jupiter:4.8.0")
  testImplementation("org.fluentlenium:fluentlenium-assertj:4.8.0")
  testImplementation("org.mockito:mockito-inline:4.6.1")
  testImplementation("org.seleniumhq.selenium:htmlunit-driver:2.61.0")
  testImplementation("org.seleniumhq.selenium:selenium-java:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-api:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-remote-driver:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-support:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
  testImplementation("com.squareup.okhttp3:okhttp:4.10.0")
  testRuntimeOnly("com.h2database:h2:1.4.200")

  runtimeOnly("org.postgresql:postgresql:42.4.1")
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
