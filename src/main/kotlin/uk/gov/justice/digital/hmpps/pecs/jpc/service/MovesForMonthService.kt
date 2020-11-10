package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovesPage
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate


@Service
class MovesForMonthService(private val moveQueryRepository: MoveQueryRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun moves(supplier: Supplier, startDate: LocalDate) = moveQueryRepository.allForSupplierInDateRange(supplier, startDate, endOfMonth(startDate))

    fun movesForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate) =
            moveQueryRepository.allForSupplierAndMoveTypeInDateRange(supplier, moveType, startDate, endOfMonth(startDate))

    fun paginatedMovesForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate, pageable: Pageable): MovesPage {
        val moves = moveQueryRepository.allForSupplierAndMoveTypeInDateRange(supplier, moveType, startDate, endOfMonth(startDate), pageable.pageSize, pageable.offset)
        val totalCount = moveQueryRepository.countForSupplierInDateRange(supplier, startDate, endOfMonth(startDate))
        return MovesPage(moves, pageable, totalCount.toLong())
    }

    fun summaryForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate): MovesTypeSummary {
        val endDate = endOfMonth(startDate)
        val moveCount = moveQueryRepository.countForSupplierInDateRange(supplier, startDate, endDate)
        val summary = moveQueryRepository.summariesForSupplierInDateRange(supplier, startDate, endDate, moveCount).find { it.moveType == moveType }
        return MovesTypeSummary(moveCount, summary ?: MovesSummary(moveType))
    }

    fun uniqueJourneys(supplier: Supplier, startDate: LocalDate) = moveQueryRepository.uniqueJourneysForSupplierInDateRange(supplier, startDate, endOfMonth(startDate))

    fun journeysSummary(supplier: Supplier, startDate: LocalDate) = moveQueryRepository.journeysSummaryForSupplierInDateRange(supplier, startDate, endOfMonth(startDate))

    fun moveTypeSummaries(supplier: Supplier, startDate: LocalDate): MoveTypeSummaries {
        val endDate = endOfMonth(startDate)
        val moveCount = moveQueryRepository.countForSupplierInDateRange(supplier, startDate, endDate)
        val summaries = moveQueryRepository.summariesForSupplierInDateRange(supplier, startDate, endDate, moveCount)
        return MoveTypeSummaries(moveCount, summaries)
    }
}

data class MovesTypeSummary(val count: Int, val movesSummary: MovesSummary)

data class MoveTypeSummaries(val count: Int, val movesSummaries: List<MovesSummary>) {

    fun allSummaries() = MoveType.values().map { mt -> movesSummaries.find { it.moveType == mt} ?: MovesSummary(mt) }

    fun summary() : MovesSummary { // summary of summaries
        return with(movesSummaries) {
            MovesSummary(null,
                    sumByDouble { it.percentage },
                    sumBy { it.volume },
                    sumBy { it.volumeUnpriced },
                    sumBy { it.totalPriceInPence }
            )
        }
    }
}

fun endOfMonth(startDate: LocalDate): LocalDate = startDate.plusMonths(1).minusDays(1)

