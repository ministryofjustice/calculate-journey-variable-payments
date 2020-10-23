plugins {
	id("uk.gov.justice.hmpps.gradle-spring-boot") version "1.0.6"
	kotlin("plugin.spring") version "1.4.10"
	kotlin("plugin.jpa") version "1.4.10"
	kotlin("plugin.allopen") version "1.4.10"
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
	implementation("com.amazonaws:aws-java-sdk-s3:1.11.880")
	implementation("org.ocpsoft.prettytime:prettytime-nlp:4.0.4.Final")
	implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.0.5")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

	testImplementation("org.mockito:mockito-inline:3.5.13")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql:42.2.17")
}
