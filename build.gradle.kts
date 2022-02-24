plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.0.4"
  kotlin("plugin.spring") version "1.6.10"
  kotlin("plugin.jpa") version "1.6.10"
  kotlin("plugin.allopen") version "1.6.10"
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
  implementation("com.beust:klaxon:5.5")
  implementation("com.amazonaws:aws-java-sdk-s3:1.12.165")
  implementation("io.sentry:sentry-spring-boot-starter:5.6.1")
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.33.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.33.0")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")
  implementation("org.apache.poi:poi-ooxml:5.2.0")
  implementation("org.flywaydb:flyway-core:8.5.1")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.session:spring-session-jdbc:2.6.2")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
  implementation(kotlin("script-runtime"))

  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.fluentlenium:fluentlenium-junit-jupiter:4.8.0")
  testImplementation("org.fluentlenium:fluentlenium-assertj:4.8.0")
  testImplementation("org.mockito:mockito-inline:4.2.0")
  testImplementation("org.seleniumhq.selenium:selenium-java:4.1.2")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.9.3")
  testImplementation("com.squareup.okhttp3:okhttp:4.9.3")
  testRuntimeOnly("com.h2database:h2")

  runtimeOnly("org.postgresql:postgresql:42.3.3")
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
