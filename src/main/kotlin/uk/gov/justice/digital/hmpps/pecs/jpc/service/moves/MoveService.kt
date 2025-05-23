package uk.gov.justice.digital.hmpps.pecs.jpc.service.moves

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate
import java.time.Month

@Service
class MoveService(
  private val moveQueryRepository: MoveQueryRepository,
  private val moveRepository: MoveRepository,
  private val eventRepository: EventRepository,
) {
  private val logger = loggerFor<MoveService>()

  fun moveWithPersonJourneysAndEvents(moveId: String, supplier: Supplier, inMonth: Month): Move? {
    val maybeMove = moveQueryRepository.moveWithPersonAndJourneys(moveId, supplier, inMonth)

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

  fun moves(supplier: Supplier, startDate: LocalDate) = moveQueryRepository.movesWithMoveTypeInDateRange(supplier, startDate, endOfMonth(startDate))

  fun findMoveByReferenceAndSupplier(ref: String, supplier: Supplier) = moveRepository.findByReferenceAndSupplier(ref, supplier)?.takeIf { it.moveType != null }

  fun movesForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate) = moveQueryRepository.movesForMoveTypeInDateRange(supplier, moveType, startDate, endOfMonth(startDate))

  fun summaryForMoveType(supplier: Supplier, moveType: MoveType, startDate: LocalDate): MovesTypeSummary {
    val endDate = endOfMonth(startDate)
    val moveCount = moveQueryRepository.moveCountInDateRange(supplier, startDate, endDate)
    val summary =
      moveQueryRepository.summariesInDateRange(supplier, startDate, endDate, moveCount).find { it.moveType == moveType }
    return MovesTypeSummary(moveCount, summary ?: MovesSummary(moveType))
  }

  fun moveTypeSummaries(supplier: Supplier, startDate: LocalDate): MoveTypeSummaries {
    val endDate = endOfMonth(startDate)
    logger.info("SQL data: GET - moveQueryRepository.moveCountInDateRange for supplier: $supplier, startDate: $startDate, endDate: $endDate ")
    val moveCount = moveQueryRepository.moveCountInDateRange(supplier, startDate, endDate)
    logger.info("SQL data: COMPLETED - moveQueryRepository.moveCountInDateRange")
    logger.info("SQL data: GET - moveQueryRepository.summariesInDateRange")
    val summaries = moveQueryRepository.summariesInDateRange(supplier, startDate, endDate, moveCount)
    logger.info("SQL data: COMPLETED - moveQueryRepository.summariesInDateRange for supplier: $supplier, startDate: $startDate, endDate: $endDate ")
    return MoveTypeSummaries(moveCount, summaries)
  }

  /**
   * Provides moves present in the payment service but not included as part of the pricing/billing due missing key
   * information for pricing purposes e.g. move type or drop off date time. This will enable the end users to better
   * reconcile invoices from the supplier. Prior to this they were not readily available.
   */
  fun candidateReconciliations(supplier: Supplier, startDate: LocalDate) = moveRepository.findCompletedCandidateReconcilableMoves(supplier, startDate.year, startDate.month.value)
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
        sumOf { it.totalPriceInPence },
      )
    }
  }
}

fun endOfMonth(startDate: LocalDate): LocalDate = startDate.plusMonths(1).minusDays(1)
