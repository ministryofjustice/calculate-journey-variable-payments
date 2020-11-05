package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.move.*
import java.time.LocalDate

@Service
class DashboardService(private val moveQueryRepository: MoveQueryRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun movesForMonth(supplier: Supplier, startDate: LocalDate) = moveQueryRepository.allMoves(supplier, startDate, endOfMonth(startDate))

    fun uniqueJourneysForMonth(supplier: Supplier, startDate: LocalDate) = moveQueryRepository.uniqueJourneys(supplier, startDate, endOfMonth(startDate))

    fun summariesForMonth(supplier: Supplier, startDate: LocalDate): MovesCountAndSummaries {
        val endDate = endOfMonth(startDate)
        val moveCount = moveQueryRepository.movesCount(supplier, startDate, endDate)
        val summaries = moveQueryRepository.summaries(supplier, startDate, endDate, moveCount)
        return MovesCountAndSummaries(moveCount, summaries)
    }
}

data class MovesCountAndSummaries(val count: Int, val summaries: List<Summary>) {

    fun allSummaries() = MoveType.values().map { mt -> summaries.find { it.moveType == mt} ?: Summary(mt) }

    fun summary() : Summary { // summary of summaries
        return with(summaries) {
            Summary(null,
                sumByDouble { it.percentage },
                sumBy { it.volume },
                sumBy { it.volumeUnpriced },
                sumBy { it.totalPriceInPence }
            )
        }
    }
}

fun endOfMonth(startDate: LocalDate): LocalDate = startDate.plusMonths(1).minusDays(1)

