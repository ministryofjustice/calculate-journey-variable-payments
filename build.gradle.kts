plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.9"
  kotlin("plugin.spring") version "1.5.31"
  kotlin("plugin.jpa") version "1.5.31"
  kotlin("plugin.allopen") version "1.5.31"
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
  implementation("com.amazonaws:aws-java-sdk-s3:1.12.80")
  implementation("io.sentry:sentry-spring-boot-starter:5.2.1")
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.28.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.28.0")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.0.0")
  implementation("org.apache.poi:poi-ooxml:5.0.0")

  constraints {
    implementation("org.apache.xmlgraphics:batik-all:1.14") {
      because("previous transitive version 1.13 pulled from Apache POI 5.0.0 has CVE")
    }
    implementation("org.apache.pdfbox:pdfbox:2.0.24") {
      because("previous transitive version 2.0.23 pulled from Apache POI 5.0.0 has CVE")
    }
    implementation("org.apache.commons:commons-compress:1.21") {
      because("previous transitive version 1.20 pulled from Apache POI 5.0.0 has CVE")
    }
    implementation("org.apache.santuario:xmlsec:2.2.3") {
      because("previous transitive version 2.2.1 pulled from Apache POI 5.0.0 has CVE")
    }
  }

  implementation("org.flywaydb:flyway-core:7.15.0")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.session:spring-session-jdbc:2.5.2")
  implementation("org.springframework.shell:spring-shell-starter:2.0.1.RELEASE")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
  implementation(kotlin("script-runtime"))

  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.fluentlenium:fluentlenium-junit-jupiter:4.8.0")
  testImplementation("org.fluentlenium:fluentlenium-assertj:4.8.0")
  testImplementation("org.mockito:mockito-inline:3.12.4")
  testImplementation("org.seleniumhq.selenium:selenium-support:3.141.59")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.9.2")
  testImplementation("com.squareup.okhttp3:okhttp:4.9.2")
  testImplementation("org.seleniumhq.selenium:selenium-support:3.141.59")
  testImplementation("org.seleniumhq.selenium:selenium-chrome-driver:3.141.59")

  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql:42.2.24")
}

tasks {
  test {
    useJUnitPlatform()
    exclude("**/integration/*", "**/migrations/*")
  }

  withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
  }

  val testIntegration by registering(Test::class) {
    useJUnitPlatform()
    include("uk/gov/justice/digital/hmpps/pecs/jpc/integration/*")
  }
}
