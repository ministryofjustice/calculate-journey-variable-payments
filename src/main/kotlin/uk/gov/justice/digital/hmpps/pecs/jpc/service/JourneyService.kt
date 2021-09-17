package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyWithPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneysSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate

@Service
class JourneyService(private val journeyQueryRepository: JourneyQueryRepository) {

  private val logger = LoggerFactory.getLogger(javaClass)

  /**
   * Returned un-priced journeys are ordered by their from and to locations (pick up and drop off).
   */
  fun distinctJourneysExcludingPriced(supplier: Supplier, startDate: LocalDate): List<JourneyWithPrice> {
    logger.info("Fetching distinct journeys (excluding priced).")

    return journeyQueryRepository.distinctJourneysAndPriceInDateRange(supplier, startDate, endOfMonth(startDate))
      .sortedBy { it.pickUpAndDropOff() }
      .also { logger.info("Retrieved ${it.size} distinct journeys (excluding priced).") }
  }

  private fun JourneyWithPrice.pickUpAndDropOff() = pickUp() + "-" + dropOff()

  private fun JourneyWithPrice.pickUp() = fromSiteName ?: fromNomisAgencyId

  private fun JourneyWithPrice.dropOff() = toSiteName ?: toNomisAgencyId

  fun distinctJourneysIncludingPriced(supplier: Supplier, startDate: LocalDate): List<JourneyWithPrice> {
    logger.info("Fetching distinct journeys (including priced).")

    return journeyQueryRepository.distinctJourneysAndPriceInDateRange(supplier, startDate, endOfMonth(startDate), false)
      .also {
        logger.info("Retrieved ${it.size} distinct journeys (including priced).")
      }
  }

  fun journeysSummary(supplier: Supplier, startDate: LocalDate): JourneysSummary {
    logger.info("Fetching journey summary for $supplier on start date $startDate")

    return journeyQueryRepository.journeysSummaryInDateRange(supplier, startDate, endOfMonth(startDate)).also {
      logger.info("Retrieved journey summary for $supplier on start date $startDate")
    }
  }

  fun prices(
    supplier: Supplier,
    fromSiteName: String?,
    toSiteName: String?,
    effectiveYear: Int
  ): List<JourneyWithPrice> {
    logger.info("Fetching prices for $supplier, from site '${fromSiteName.orEmpty()}', to site name '${toSiteName.orEmpty()}' for effective year $effectiveYear")

    return journeyQueryRepository.prices(
      supplier,
      fromSiteName?.trim()?.uppercase(),
      toSiteName?.trim()?.uppercase(),
      effectiveYear
    ).also {
      logger.info("Retrieved ${it.size} prices for $supplier, from site '${fromSiteName.orEmpty()}', to site name '${toSiteName.orEmpty()}' for effective year $effectiveYear")
    }
  }
}
