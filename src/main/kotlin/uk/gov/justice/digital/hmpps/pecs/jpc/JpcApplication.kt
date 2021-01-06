package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ManualLocationImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ManualPriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ReportsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

@SpringBootApplication
class JpcApplication

fun main(args: Array<String>) {
  runApplication<JpcApplication>(*args).let { context ->
    // This is a temporary solution to run an import of locations and prices then terminate bypassing the need to go to an endpoint.
    context.environment.getProperty("import-locations")?.let {
      (context.getBean(ManualLocationImporter::class) as ManualLocationImporter).let {
        SpringApplication.exit(
          context,
          it.import()
        )
      }
    }

    context.environment.getProperty("import-serco-prices")?.let {
      (context.getBean(ManualPriceImporter::class) as ManualPriceImporter).let {
        SpringApplication.exit(
          context,
          it.import(Supplier.SERCO)
        )
      }
    }

    context.environment.getProperty("import-geoamey-prices")?.let {
      (context.getBean(ManualPriceImporter::class) as ManualPriceImporter).let {
        SpringApplication.exit(
          context,
          it.import(Supplier.GEOAMEY)
        )
      }
    }

    // This is a temporary solution to run an import of both supplier reports then terminate bypassing the need to go to an endpoint.
    context.environment.getProperty("import-supplier-reports")?.let { dates ->
      val from = LocalDate.parse(dates.split(",")[0].trim())
      val to = LocalDate.parse(dates.split(",")[1].trim())

      (context.getBean(ReportsImporter::class) as ReportsImporter).let { reportImporter ->
        SpringApplication.exit(context, reportImporter.import(from, to))
      }
    }
  }
}
