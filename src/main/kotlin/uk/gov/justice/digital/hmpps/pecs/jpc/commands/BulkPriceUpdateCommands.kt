package uk.gov.justice.digital.hmpps.pecs.jpc.commands

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService

@ShellComponent
class BulkPriceUpdateCommands(
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService
) {
  @ShellMethod("Performs a price uplift for the given supplier, effective year and the supplied multiplier.")
  fun uplift(supplier: Supplier, effectiveYear: Int, multiplier: Double, force: Boolean = false) {
    if (force)
      annualPriceAdjustmentsService.uplift(supplier, effectiveYear, multiplier)
    else
      throw RuntimeException("Force is required for this operation to complete.")
  }
}
