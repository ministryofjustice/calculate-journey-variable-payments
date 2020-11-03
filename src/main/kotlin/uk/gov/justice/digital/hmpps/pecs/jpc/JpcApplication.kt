package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.pecs.jpc.import.LocationAndPriceImporter

@SpringBootApplication
class JpcApplication

fun main(args: Array<String>) {
    runApplication<JpcApplication>(*args).let { context ->

        // This is a temporary solution to run an import of locations and prices then terminate bypassing the need to go to an endpoint.
        args.firstOrNull { it == "--run-import" }?.let {
            (context.getBean(LocationAndPriceImporter::class) as LocationAndPriceImporter).let { SpringApplication.exit(context, it) }
        }
    }
}
