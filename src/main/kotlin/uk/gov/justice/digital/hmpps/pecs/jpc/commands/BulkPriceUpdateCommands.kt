package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.BulkPricesService

@ShellComponent
class BulkPriceUpdateCommands(
  private val bulkPricesService: BulkPricesService,
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @ShellMethod("Adds prices for the next effective year based on the current effective years prices and the supplied multiplier.")
  fun addNextYearsPrices(supplier: Supplier, multiplier: Double) {
    logger.info("Starting bulk price update for $supplier with multiplier $multiplier")

    bulkPricesService.addNextYearsPrices(supplier, multiplier)

    logger.info("Finished bulk price update for $supplier with multiplier $multiplier")
  }

  @ShellMethod("Performs a price uplift for the given supplier, effective year and the supplied multiplier.")
  fun uplift(supplier: Supplier, effectiveYear: Int, multiplier: Double, force: Boolean = false) {
    if (force)
      annualPriceAdjustmentsService.uplift(supplier, effectiveYear, multiplier)
    else
      throw RuntimeException("Force is required for this operation to complete.")
  }
}
