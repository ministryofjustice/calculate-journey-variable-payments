plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "1.1.0"
  kotlin("plugin.spring") version "1.4.21"
  kotlin("plugin.jpa") version "1.4.21"
  kotlin("plugin.allopen") version "1.4.21"
}

allOpen {
  annotation("javax.persistence.Entity")
  annotation("javax.persistence.Embeddable")
  annotation("javax.persistence.MappedSuperclass")
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.apache.poi:poi-ooxml:4.1.2")
  implementation("com.github.kittinunf.result:result:3.1.0")
  implementation("com.github.kittinunf.result:result-coroutines:3.1.0")
  implementation("com.beust:klaxon:5.4")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("com.amazonaws:aws-java-sdk-s3:1.11.930")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.5.2")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity5:3.0.4.RELEASE")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.session:spring-session-jdbc")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.shell:spring-shell-starter:2.0.1.RELEASE")
  implementation(kotlin("script-runtime"))
  implementation("net.javacrumbs.shedlock:shedlock-spring:4.19.1")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:4.16.0")

  testImplementation("org.mockito:mockito-inline:3.6.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")

  runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql:42.2.18")
  runtimeOnly("org.flywaydb:flyway-core:6.5.6")
}
