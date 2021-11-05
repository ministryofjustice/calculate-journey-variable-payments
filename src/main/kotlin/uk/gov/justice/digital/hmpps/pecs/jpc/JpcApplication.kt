package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class JpcApplication

fun main(args: Array<String>) {
  runApplication<JpcApplication>(*args)
}
