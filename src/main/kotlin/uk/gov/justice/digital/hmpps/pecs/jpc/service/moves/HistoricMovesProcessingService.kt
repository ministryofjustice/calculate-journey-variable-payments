package uk.gov.justice.digital.hmpps.pecs.jpc.service.moves

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.temporal.ChronoUnit

private val logger = loggerFor<HistoricMovesProcessingService>()

/**
 * The intent of this service is to provide a means to process existing Moves if adjustments and/or corrections need to
 * be made to Moves that have already taken place. This avoids the need to rerun the report import process which is very
 * time and resource hungry when it spans a large period of time.
 *
 * An example of a correction would be cancelled Moves that don't have a (fake) journey which are needed for pricing
 * purposes.
 */
@Service
class HistoricMovesProcessingService(
  private val moveQueryRepository: MoveQueryRepository,
  private val movePersister: MovePersister,
  private val timeSource: TimeSource
) {

  /**
   * This will go through and only process historic moves that have been assigned a move type.
   *
   * Note: if a move does not have a move type and needs to be processed as well this will need to be changed.
   */
  fun process(dateRange: DateRange, supplier: Supplier): Int {
    if (dateRange.notInPast()) throw RuntimeException("Date range must be in the past")

    val start = timeSource.dateTime()

    return moveQueryRepository.movesInDateRange(
      supplier,
      dateRange.start,
      dateRange.endInclusive
    ).values.flatten()
      .also { existingMoves ->
        logger.info("Processing ${existingMoves.size} historic moves in range ${dateRange.start} to ${dateRange.endInclusive}")
      }
      .let(this::processExisting)
      .also {
        logger.info("Processed $it historic moves in range ${dateRange.start} to ${dateRange.endInclusive}")
        logger.info("Time taken in minutes ${start.until(timeSource.dateTime(), ChronoUnit.MINUTES)}")
      }
  }

  private fun DateRange.notInPast() = this.start.isAfter(timeSource.yesterday()) || this.endInclusive.isAfter(timeSource.yesterday())

  private fun processExisting(moves: List<Move>) = movePersister.persist(moves)
}
