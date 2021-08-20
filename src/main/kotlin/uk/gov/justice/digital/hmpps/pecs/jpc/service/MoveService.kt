package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.move.EventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovesSummary
import java.time.LocalDate

@Service
class MoveService(
  private val moveQueryRepository: MoveQueryRepository,
  private val moveRepository: MoveRepository,
  private val eventRepository: EventRepository
) {

  fun moveWithPersonJourneysAndEvents(moveId: String, supplier: Supplier): Move? {
    val maybeMove = moveQueryRepository.moveWithPersonAndJourneys(moveId, supplier)

    return maybeMove?.let {
      val moveEvents = eventRepository.findAllByEventableId(it.moveId)
      val journeyId2Events =
        eventRepository.findByEventableIdIn(it.journeys.map { j -> j.journeyId }).groupBy { e -> e.eventableId }

      val journeysWithEvents = it.journeys.map { journey ->
        journey.copy(events = journeyId2Events[journey.journeyId]?.sortedBy { e -> e.occurredAt } ?: listOf())
      }.sortedBy { journey -> journey.pickUpDateTime }

      it.copy(events = moveEvents, journeys = journeysWithEvents)
    }
  }

  fun moves(supplier: Supplier, startDate: LocalDate) =
    moveQueryRepository.movesInDateRange(supplier, startDate, endOfMonth(startDate))

  fun findMoveByReferenceAndSupplier(ref: String, supplier: Supplier) =
    // At time of writing the need for filtering null moves types is a result of poor supplier data.
    moveRepository.findByReferenceAndSupplier(ref, supplier).filter { it.moveType != null }

  fun movesForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate) =
    moveQueryRepository.movesForMoveTypeInDateRange(supplier, moveType, startDate, endOfMonth(startDate))

  fun summaryForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate): MovesTypeSummary {
    val endDate = endOfMonth(startDate)
    val moveCount = moveQueryRepository.moveCountInDateRange(supplier, startDate, endDate)
    val summary =
      moveQueryRepository.summariesInDateRange(supplier, startDate, endDate, moveCount).find { it.moveType == moveType }
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

  fun allSummaries() = MoveType.values().map { mt -> movesSummaries.find { it.moveType == mt } ?: MovesSummary(mt) }

  fun summary(): MovesSummary { // summary of summaries
    return with(movesSummaries) {
      MovesSummary(
        null,
        sumOf { it.percentage },
        sumOf { it.volume },
        sumOf { it.volumeUnpriced },
        sumOf { it.totalPriceInPence }
      )
    }
  }
}

fun endOfMonth(startDate: LocalDate): LocalDate = startDate.plusMonths(1).minusDays(1)
