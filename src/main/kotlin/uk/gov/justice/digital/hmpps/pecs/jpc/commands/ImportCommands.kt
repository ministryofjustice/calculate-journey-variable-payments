package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ManualLocationImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ManualPriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ReportsImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * These commands are a temporary measure/solution to allow manually running the import of locations, prices and reporting data.
 */
@ShellComponent
class ImportCommands(
  @Autowired private val locationImporter: ManualLocationImporter,
  @Autowired private val manualPriceImporter: ManualPriceImporter,
  @Autowired private val reportsImporter: ReportsImporter
) {

  @ShellMethod("Imports schedule 34 locations from S3. This command deletes all existing prices.")
  fun importLocations() {
    locationImporter.import()
  }

  @ShellMethod("Imports prices for the given supplier from S3. This command deletes all existing prices for the given supplier.")
  fun importPrices(supplier: Supplier) {
    manualPriceImporter.import(supplier)
  }

  /**
   * Due to potential the large volumes of data each day is imported as an individual import to reduce the memory footprint.
   */
  @ShellMethod("Imports reports for both suppliers for the given dates. Date params are the in ISO date format e.g. YYYY-MM-DD.")
  fun importReports(from: LocalDate, to: LocalDate) {
    if (to.isBefore(from)) throw RuntimeException("To date must be equal to or greater than from date.")

    for (i in 0..ChronoUnit.DAYS.between(from, to)) {
      reportsImporter.import(from.plusDays(i), from.plusDays(i))
    }
  }
}
