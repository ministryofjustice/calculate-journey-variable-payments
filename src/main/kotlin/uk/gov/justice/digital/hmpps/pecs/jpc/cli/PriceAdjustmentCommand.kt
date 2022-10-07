package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.AdjustmentMultiplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.EffectiveYear
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing.AnnualPriceAdjustmentsService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<PriceAdjustmentCommand>()

@ConditionalOnNotWebApplication
@Component
class PriceAdjustmentCommand(private val adjustmentsService: AnnualPriceAdjustmentsService, private val effectiveYear: EffectiveYear) {

  fun adjustPricesFor(
    supplier: Supplier,
    year: Int,
    inflationary: AdjustmentMultiplier,
    volumetric: AdjustmentMultiplier? = null,
    details: String
  ) {
    if (!effectiveYear.canAddOrUpdatePrices(year))
      logger.warn(
        "Price being adjusted before previous effective year '${effectiveYear.previous()}'. Subsequent years must be adjusted accordingly."
      )

    logger.info("Starting adjustment of prices for $supplier for effective year $year.")

    when (supplier) {
      Supplier.UNKNOWN -> throw RuntimeException("UNKNOWN is not a valid supplier")
      else -> adjustmentsService.adjust(
        supplier,
        year,
        inflationary,
        volumetric,
        null,
        details,
        true
      )
    }

    logger.info("Finished adjustment of prices for $supplier for effective year $year.")
  }
}
