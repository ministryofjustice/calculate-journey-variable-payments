import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.5.3"
  kotlin("plugin.spring") version "2.4.0"
  kotlin("plugin.jpa") version "2.4.0"
  kotlin("plugin.allopen") version "2.4.0"
}
java {
  sourceCompatibility = JavaVersion.VERSION_25
  targetCompatibility = JavaVersion.VERSION_25
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
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:2.5.0")
  implementation("com.beust:klaxon:5.6")
  implementation("com.amazonaws:aws-java-sdk-s3:1.12.797")
  implementation("com.amazonaws:aws-java-sdk-sts:1.12.797")
  implementation("io.sentry:sentry-spring-boot-starter:8.46.0")
  implementation("net.javacrumbs.shedlock:shedlock-spring:7.7.0")
  implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:7.7.0")
  implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:4.0.1")
  implementation("org.apache.poi:poi-ooxml:5.5.1")
  implementation("org.flywaydb:flyway-core")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-webclient")
  implementation("org.springframework.boot:spring-boot-actuator")
  implementation("org.springframework.session:spring-session-jdbc")
  implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
  implementation("com.github.spotbugs:spotbugs-annotations:4.10.2")
  implementation("com.microsoft.azure:applicationinsights-logging-logback:2.6.4")
  implementation("org.apache.commons:commons-compress:1.28.0")
  implementation(kotlin("script-runtime"))

  runtimeOnly("org.postgresql:postgresql:42.7.11")

  testImplementation("org.wiremock:wiremock:3.13.2")
  testImplementation("org.htmlunit:htmlunit:5.2.0")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.apache.commons:commons-compress:1.28.0")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.squareup.okhttp3:mockwebserver:5.4.0")
  testImplementation("com.squareup.okhttp3:okhttp:5.4.0")
  testImplementation("com.microsoft.playwright:playwright:1.61.0")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:2.5.0")

  testRuntimeOnly("com.h2database:h2:2.3.232")
}
kotlin {
  jvmToolchain(25)
}

tasks {
  test {
    useJUnitPlatform()
    exclude("**/integrationplaywright/*")
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
  }

  val baseTest = named<Test>("test")

  val testPlayWrightIntegration by registering(Test::class) {
    dependsOn("testClasses")

    // Use standard test source set outputs/classpath
    val sourceSets = project.extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer
    testClassesDirs = sourceSets.getByName("test").output.classesDirs
    classpath = sourceSets.getByName("test").runtimeClasspath

    // Diagnostics
    doFirst {
      println("[testPlayWrightIntegration] testClassesDirs: ${'$'}{testClassesDirs.files}")
      testClassesDirs.files.forEach { f -> println("[classesDir] ${'$'}f exists=${'$'}{f.exists()} contains=${'$'}{f.listFiles()?.size ?: 0}") }
    }

    testLogging.showStandardStreams = true
    testLogging.exceptionFormat = TestExceptionFormat.FULL
    useJUnitPlatform()

    filter { isFailOnNoMatchingTests = true }

    reports.junitXml.required.set(true)
    reports.junitXml.outputLocation.set(layout.buildDirectory.dir("test-results/testPlayWrightIntegration").get().asFile)
    reports.html.required.set(true)
  }
}
