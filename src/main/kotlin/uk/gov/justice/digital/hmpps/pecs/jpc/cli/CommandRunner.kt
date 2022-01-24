package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

/**
 * This enables commands to be executed within the context of a running application (but not in WEB mode). Once a
 * command has completed the application will terminate.
 */
private val logger = loggerFor<CommandRunner>()

@ConditionalOnNotWebApplication
@Component
class CommandRunner(
  private val bulkPriceImportCommand: BulkPriceImportCommand,
  private val reportImportCommand: ReportImportCommand
) : ApplicationRunner {

  override fun run(arguments: ApplicationArguments) {
    logger.info("running sample application runner {}", arguments.optionNames)

    when {
      arguments.contains("price-import") -> runPriceImport(arguments.getSupplier(), arguments.getYear(),)
      arguments.contains("report-import") -> runReportImport(arguments.getDate("from"), arguments.getDate("to"))
      else -> logger.info("no commands executed")
    }
  }

  private fun runPriceImport(supplier: Supplier?, year: Int?) {
    if (supplier == null || year == null) throw RuntimeException("missing supplier and/or year - unable to run the price import")

    bulkPriceImportCommand.bulkImportPricesFor(supplier, year)
  }

  private fun runReportImport(from: LocalDate?, to: LocalDate?) {
    if (from == null || to == null) throw RuntimeException("missing from and/or to - unable to run the report import")

    reportImportCommand.importReports(from, to)
  }

  private fun ApplicationArguments.contains(command: String) = this.optionNames.contains(command)

  private fun ApplicationArguments.getSupplier() = this.get("supplier")?.let { Supplier.valueOf(it) }

  private fun ApplicationArguments.getYear() = this.get("year")?.toIntOrNull()

  private fun ApplicationArguments.getDate(attribute: String) = this.get(attribute)?.let { LocalDate.parse(it) }

  private fun ApplicationArguments.get(attribute: String) = this.getOptionValues(attribute).firstOrNull()?.trim()?.uppercase()
}
