plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.1.0"
  kotlin("plugin.spring") version "1.4.30"
  kotlin("plugin.jpa") version "1.4.30"
  kotlin("plugin.allopen") version "1.4.30"
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.Embeddable")
  annotation("javax.persistence.MappedSuperclass")
}

dependencies {
  implementation("com.github.kittinunf.result:result:4.0.0")
  implementation("com.github.kittinunf.result:result-coroutines:4.0.0")
  implementation("com.beust:klaxon:5.4")
  implementation("com.amazonaws:aws-java-sdk-s3:1.11.950")
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.20.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.20.0")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.5.2")
  implementation("org.apache.poi:poi-ooxml:5.0.0")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.session:spring-session-jdbc:2.4.2")
  implementation("org.springframework.shell:spring-shell-starter:2.0.1.RELEASE")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
  implementation(kotlin("script-runtime"))

  testImplementation("org.mockito:mockito-inline:3.7.7")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")

  runtimeOnly("org.flywaydb:flyway-core:6.5.6")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql:42.2.18")
}
