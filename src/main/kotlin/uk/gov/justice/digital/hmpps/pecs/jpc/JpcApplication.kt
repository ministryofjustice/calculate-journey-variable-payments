package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class JpcApplication

fun main(args: Array<String>) {
	SpringApplication.run(JpcApplication::class.java, *args)
}