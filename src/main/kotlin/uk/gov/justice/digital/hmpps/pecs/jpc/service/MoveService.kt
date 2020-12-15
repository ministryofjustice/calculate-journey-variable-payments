package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.move.*
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

@Service
class MoveService(private val moveQueryRepository: MoveQueryRepository, private val eventRepository: EventRepository) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun moveWithPersonJourneysAndEvents(moveId: String) : Move {
        val move = moveQueryRepository.moveWithPersonAndJourneys(moveId)
        val moveEvents = eventRepository.findAllByEventableId(move.moveId)
        val journeyId2Events = eventRepository.findByEventableIdIn(move.journeys.map { it.journeyId }).groupBy { it.eventableId }

        val journeysWithEvents = move.journeys.map {
            it.copy(events = journeyId2Events[it.journeyId] ?: listOf())
        }

        return move.copy(events = moveEvents, journeys = journeysWithEvents)
    }


    fun moves(supplier: Supplier, startDate: LocalDate) = moveQueryRepository.movesInDateRange(supplier, startDate, endOfMonth(startDate))

    fun movesForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate) =
            moveQueryRepository.movesForMoveTypeInDateRange(supplier, moveType, startDate, endOfMonth(startDate))

    fun summaryForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate): MovesTypeSummary {
        val endDate = endOfMonth(startDate)
        val moveCount = moveQueryRepository.moveCountInDateRange(supplier, startDate, endDate)
        val summary = moveQueryRepository.summariesInDateRange(supplier, startDate, endDate, moveCount).find { it.moveType == moveType }
        return MovesTypeSummary(moveCount, summary ?: MovesSummary(moveType))
    }

    fun moveTypeSummaries(supplier: Supplier, startDate: LocalDate): MoveTypeSummaries {
        val endDate = endOfMonth(startDate)
        val moveCount = moveQueryRepository.moveCountInDateRange(supplier, startDate, endDate)
        val summaries = moveQueryRepository.summariesInDateRange(supplier, startDate, endDate, moveCount)
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

