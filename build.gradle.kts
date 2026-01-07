import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.7"
  kotlin("plugin.spring") version "2.2.10"
  kotlin("plugin.jpa") version "2.2.10"
  kotlin("plugin.allopen") version "2.2.10"
}
java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
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

  val shedlockVersion = "6.10.0"
  listOf(
    "com.beust:klaxon:5.6",
    "com.amazonaws:aws-java-sdk-s3:1.12.788",
    "com.amazonaws:aws-java-sdk-sts:1.12.788",
    "io.sentry:sentry-spring-boot-starter:8.20.0",
    "net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion",
    "net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion",
    "nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.4.0",
    "org.apache.poi:poi-ooxml:5.4.1",
    "org.flywaydb:flyway-database-postgresql",
    "org.springframework.boot:spring-boot-starter-security",
    "org.springframework.boot:spring-boot-starter-data-jpa",
    "org.springframework.boot:spring-boot-starter-thymeleaf",
    "org.springframework.boot:spring-boot-starter-oauth2-client",
    "org.springframework.boot:spring-boot-starter-oauth2-resource-server",
    "org.springframework.boot:spring-boot-starter-webflux",
    "org.springframework.session:spring-session-jdbc",
    "org.thymeleaf.extras:thymeleaf-extras-springsecurity6",
    "com.github.spotbugs:spotbugs-annotations:4.9.4",
    "com.microsoft.azure:applicationinsights-logging-logback:2.6.4",
    "org.apache.commons:commons-compress:1.28.0",
  ).forEach { implementation(it) }
  implementation(kotlin("script-runtime"))

  listOf(
    "org.wiremock:wiremock:3.13.1",
    "org.htmlunit:htmlunit:4.15.0",
    "org.mockito:mockito-inline:5.2.0",
    "org.apache.commons:commons-compress:1.28.0",
    "org.springframework.boot:spring-boot-starter-test",
    "org.springframework.security:spring-security-test",
    "com.squareup.okhttp3:mockwebserver:4.12.0",
    "com.squareup.okhttp3:okhttp:4.12.0",
    "com.microsoft.playwright:playwright:1.54.0",
  ).forEach { testImplementation(it) }

  testRuntimeOnly("com.h2database:h2:1.4.200")

  runtimeOnly("org.postgresql:postgresql:42.7.7")
}
kotlin {
  jvmToolchain(21)
}

tasks {
  test {
    useJUnitPlatform()
    exclude("**/integrationplaywright/*")
  }

  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
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
