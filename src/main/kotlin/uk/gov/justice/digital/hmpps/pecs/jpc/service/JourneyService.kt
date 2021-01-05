package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

@Service
class JourneyService(private val journeyQueryRepository: JourneyQueryRepository) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun distinctJourneysExcludingPriced(supplier: Supplier, startDate: LocalDate) =
    journeyQueryRepository.distinctJourneysAndPriceInDateRange(supplier, startDate, endOfMonth(startDate))

  fun distinctJourneysIncludingPriced(supplier: Supplier, startDate: LocalDate) =
    journeyQueryRepository.distinctJourneysAndPriceInDateRange(supplier, startDate, endOfMonth(startDate), false)

  fun journeysSummary(supplier: Supplier, startDate: LocalDate) =
    journeyQueryRepository.journeysSummaryInDateRange(supplier, startDate, endOfMonth(startDate))

  fun prices(supplier: Supplier, fromSiteName: String?, toSiteName: String?, effectiveYear: Int) =
    journeyQueryRepository.prices(
      supplier,
      fromSiteName?.trim()?.toUpperCase(),
      toSiteName?.trim()?.toUpperCase(),
      effectiveYear
    )
}
