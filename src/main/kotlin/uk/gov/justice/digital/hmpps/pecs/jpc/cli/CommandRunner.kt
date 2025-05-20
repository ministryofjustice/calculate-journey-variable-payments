package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

/**
 * This enables commands to be executed within the context of a running application (but not in WEB mode). Once a
 * command has completed the application will terminate.
 */
private val logger = loggerFor<CommandRunner>()

@ConditionalOnNotWebApplication
@Profile("!daily-reports")
@Component
class CommandRunner(
  private val bulkPriceImportCommand: BulkPriceImportCommand,
  private val reportImportCommand: ReportImportCommand,
  private val historicMovesCommand: HistoricMovesCommand,
  private val priceAdjustmentCommand: PriceAdjustmentCommand,
) : ApplicationRunner {

  override fun run(arguments: ApplicationArguments) {
    logger.info("running sample application runner {}", arguments.optionNames)

    when {
      arguments.contains("price-import") -> bulkPriceImportCommand.bulkImportPricesFor(
        arguments.getSupplier(),
        arguments.getYear(),
        arguments.getAction(),
      )
      arguments.contains("price-adjust") -> priceAdjustmentCommand.adjustPricesFor(
        arguments.getSupplier(),
        arguments.getYear(),
        arguments.getMultiplier("inflationary") ?: throw RuntimeException("Missing inflation multiplier argument"),
        arguments.getMultiplier("volumetric"),
        arguments.get("details") ?: "Price adjusted via terminal",

      )
      arguments.contains("report-import") -> reportImportCommand.importReports(
        arguments.getDate("from"),
        arguments.getDate("to"),
      )
      arguments.contains("process-historic-moves") -> historicMovesCommand.process(
        arguments.getDate("from"),
        arguments.getDate("to"),
        arguments.getSupplier(),
      )
      else -> logger.info("no commands executed")
    }
  }

  private fun ApplicationArguments.contains(command: String) = this.optionNames.contains(command)

  private fun ApplicationArguments.getSupplier() = this.get("supplier")?.let { Supplier.valueOf(it) } ?: throw RuntimeException("Missing supplier argument")

  private fun ApplicationArguments.getAction() = this.get("action")?.let { PriceImporter.Action.valueOf(it) } ?: PriceImporter.Action.ERROR

  private fun ApplicationArguments.getYear() = this.get("year")?.toIntOrNull() ?: throw RuntimeException("Missing year argument")

  private fun ApplicationArguments.getMultiplier(attribute: String) = this.get(attribute)?.let { AdjustmentMultiplier.valueOf(it) }

  private fun ApplicationArguments.getDate(attribute: String) = this.get(attribute)?.let { LocalDate.parse(it) } ?: throw RuntimeException("Missing date argument $attribute")

  private fun ApplicationArguments.get(attribute: String) = this.getOptionValues(attribute).firstOrNull()?.trim()?.uppercase()
}
