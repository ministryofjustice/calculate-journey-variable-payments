package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.LocationAndPriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.SupplierReportsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import java.time.LocalDate

@SpringBootApplication
class JpcApplication

fun main(args: Array<String>) {
    runApplication<JpcApplication>(*args).let { context ->
        // This is a temporary solution to run an import of locations and prices then terminate bypassing the need to go to an endpoint.
        context.environment.getProperty("import-locations-and-prices")?.let {
            (context.getBean(LocationAndPriceImporter::class) as LocationAndPriceImporter).let { SpringApplication.exit(context, it.import()) }
        }

        // This is a temporary solution to run an import of both supplier reports then terminate bypassing the need to go to an endpoint.
        context.environment.getProperty("import-supplier-reports")?.let { dates ->
            if (dates.isNotEmpty()) {
                val from = LocalDate.parse(dates.split(",")[0].trim())
                val to = LocalDate.parse(dates.split(",")[1].trim())

                (context.getBean(SupplierReportsImporter::class) as SupplierReportsImporter).let { reportImporter ->
                    SpringApplication.exit(context, reportImporter.import(from, to)) }
            } else {
                (context.getBean(TimeSource::class) as TimeSource).let { ts ->
                    val to = ts.date().minusDays(1)
                    val from = to.minusDays(1)

                    (context.getBean(SupplierReportsImporter::class) as SupplierReportsImporter).let { reportImporter ->
                        SpringApplication.exit(context, reportImporter.import(from, to))
                    }
                }
            }
        }
    }
}
