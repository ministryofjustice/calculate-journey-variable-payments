package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.move.*
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

    fun distinctPricedJourneys(supplier: Supplier, fromSiteName: String?, toSiteName: String?) =
            journeyQueryRepository.distinctPricedJourneys(supplier, fromSiteName?.trim()?.toUpperCase(), toSiteName?.trim()?.toUpperCase())
}
