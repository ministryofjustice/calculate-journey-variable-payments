import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("uk.gov.justice.hmpps.gradle-spring-boot") version "1.0.2"
	kotlin("plugin.spring") version "1.4.0"
	kotlin("plugin.jpa") version "1.4.0"
	kotlin("plugin.allopen") version "1.4.0"
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
	implementation("com.amazonaws:aws-java-sdk-s3:1.11.860")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
}
