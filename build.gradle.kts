plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.0"
  kotlin("plugin.spring") version "1.5.20"
  kotlin("plugin.jpa") version "1.5.20"
  kotlin("plugin.allopen") version "1.5.20"
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
  implementation("com.github.kittinunf.result:result:4.0.0")
  implementation("com.github.kittinunf.result:result-coroutines:4.0.0")
  implementation("com.beust:klaxon:5.5")
  implementation("com.amazonaws:aws-java-sdk-s3:1.12.14")
  implementation("io.sentry:sentry-spring-boot-starter:5.0.1")
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.24.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.24.0")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.5.3")
  implementation("org.apache.poi:poi-ooxml:5.0.0")

  constraints {
    implementation("org.apache.xmlgraphics:batik-all:1.14") {
      because("previous transitive version 1.13 pulled from Apache POI 5.0.0 has CVE")
    }
    implementation("org.apache.pdfbox:pdfbox:2.0.24") {
      because("previous transitive version 2.0.23 pulled from Apache POI 5.0.0 has CVE")
    }
  }

  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.session:spring-session-jdbc:2.5.1")
  implementation("org.springframework.shell:spring-shell-starter:2.0.1.RELEASE")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
  implementation(kotlin("script-runtime"))

  testImplementation("org.seleniumhq.selenium:selenium-support:3.141.59")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.fluentlenium:fluentlenium-junit-jupiter:4.8.0")
  testImplementation("org.fluentlenium:fluentlenium-assertj:4.8.0")
  testImplementation("org.mockito:mockito-inline:3.11.2")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.9.1")
  testImplementation("com.squareup.okhttp3:okhttp:4.9.1")
  testImplementation("org.testcontainers:postgresql:1.15.3")

  runtimeOnly("org.flywaydb:flyway-core:7.10.0")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql:42.2.22")
}

tasks {
  test {
    useJUnitPlatform()
    exclude("**/integration/*", "**/migrations/*")
  }

  val testIntegration by registering(Test::class) {
    useJUnitPlatform()
    include("uk/gov/justice/digital/hmpps/pecs/jpc/integration/*")
  }
}
