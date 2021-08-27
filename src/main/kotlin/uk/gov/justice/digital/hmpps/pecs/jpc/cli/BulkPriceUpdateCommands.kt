package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService

@ShellComponent
class BulkPriceUpdateCommands(
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService
) {
  @ShellMethod("Performs a bulk price adjustment for the given supplier, effective year and the supplied multiplier.")
  fun bulkPriceAdjustment(supplier: Supplier, effectiveYear: Int, multiplier: Double, force: Boolean = false) {
    if (force)
      annualPriceAdjustmentsService.adjust(supplier, effectiveYear, multiplier, null)
    else
      throw RuntimeException("Force is required for this operation to complete.")
  }
}
