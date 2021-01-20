plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "2.1.2"
  kotlin("plugin.spring") version "1.4.21-2"
  kotlin("plugin.jpa") version "1.4.21-2"
  kotlin("plugin.allopen") version "1.4.21-2"
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.Embeddable")
  annotation("javax.persistence.MappedSuperclass")
}

dependencies {
  implementation("com.github.kittinunf.result:result:3.1.0")
  implementation("com.github.kittinunf.result:result-coroutines:3.1.0")
  implementation("com.beust:klaxon:5.4")
  implementation("com.amazonaws:aws-java-sdk-s3:1.11.940")
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.20.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.20.0")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.5.2")
  implementation("org.apache.poi:poi-ooxml:5.0.0")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.session:spring-session-jdbc")
  implementation("org.springframework.shell:spring-shell-starter:2.0.1.RELEASE")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
  implementation(kotlin("script-runtime"))

  testImplementation("org.mockito:mockito-inline:3.7.7")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")

  runtimeOnly("org.flywaydb:flyway-core:7.5.0")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql:42.2.18")
}
