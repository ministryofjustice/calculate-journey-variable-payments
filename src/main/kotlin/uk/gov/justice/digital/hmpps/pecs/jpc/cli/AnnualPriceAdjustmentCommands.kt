package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AnnualPriceAdjustmentsService
import java.math.BigDecimal

@ShellComponent
class AnnualPriceAdjustmentCommands(
  private val annualPriceAdjustmentsService: AnnualPriceAdjustmentsService
) {
  @ShellMethod("Performs an inflationary price adjustment for the given supplier, effective year and the supplied multiplier.")
  fun inflationary(supplier: Supplier, effectiveYear: Int, multiplier: BigDecimal, details: String, force: Boolean = false) {
    if (force)
      annualPriceAdjustmentsService.inflationary(supplier, effectiveYear, AdjustmentMultiplier(multiplier), null, details)
    else
      throw RuntimeException("Force is required for this operation to complete.")
  }

  @ShellMethod("Performs a volumetric price adjustment for the given supplier, effective year and the supplied multiplier.")
  fun volumetric(supplier: Supplier, effectiveYear: Int, multiplier: BigDecimal, details: String, force: Boolean = false) {
    if (force)
      annualPriceAdjustmentsService.volumetric(supplier, effectiveYear, AdjustmentMultiplier(multiplier), null, details)
    else
      throw RuntimeException("Force is required for this operation to complete.")
  }
}
