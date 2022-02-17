package uk.gov.justice.digital.hmpps.pecs.jpc.service.moves

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDateTime

class HistoricMovesProcessingServiceTest {

  private val move: Move = mock()

  private val moveQueryRepository: MoveQueryRepository = mock()

  private val movePersister: MovePersister = mock()

  private val timeSource = TimeSource { LocalDateTime.now() }

  private val service = HistoricMovesProcessingService(moveQueryRepository, movePersister, timeSource)

  @Test
  fun `given an invalid start date when process historic moves is called an exception is thrown`() {
    val invalidDteRange = DateRange(timeSource.date(), timeSource.date())

    assertThatThrownBy {
      service.process(invalidDteRange, Supplier.SERCO)
    }.isInstanceOf(RuntimeException::class.java).hasMessage("Date range must be in the past")

    verifyNoInteractions(moveQueryRepository)
    verifyNoInteractions(movePersister)
  }

  @Test
  fun `given an invalid end date when process historic moves is called an exception is thrown`() {
    val invalidDteRange = DateRange(timeSource.yesterday(), timeSource.date())

    assertThatThrownBy {
      service.process(invalidDteRange, Supplier.SERCO)
    }.isInstanceOf(RuntimeException::class.java).hasMessage("Date range must be in the past")

    verifyNoInteractions(moveQueryRepository)
    verifyNoInteractions(movePersister)
  }

  @Test
  fun `given a valid date range when process historic moves is called the expected invocations are made`() {
    val dateRange = DateRange(timeSource.yesterday(), timeSource.yesterday())

    whenever(moveQueryRepository.movesInDateRange(Supplier.SERCO, dateRange.start, dateRange.endInclusive)).thenReturn(
      mapOf(MoveType.STANDARD to listOf(move))
    )

    service.process(DateRange(dateRange.start, dateRange.endInclusive), Supplier.SERCO)

    verify(moveQueryRepository).movesInDateRange(Supplier.SERCO, dateRange.start, dateRange.endInclusive)
    verify(movePersister).persist(listOf(move))
  }
}
