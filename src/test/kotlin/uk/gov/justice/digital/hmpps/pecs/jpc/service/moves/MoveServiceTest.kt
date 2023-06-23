package uk.gov.justice.digital.hmpps.pecs.jpc.service.moves

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.eventE1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyEventJE1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.defaultSupplierSerco
import java.time.LocalDate
import java.time.Month

internal class MoveServiceTest {

  private val moveQueryRepository: MoveQueryRepository = mock()

  private val eventRepository: EventRepository = mock()

  private val moveRepository: MoveRepository = mock()

  private val service: MoveService = MoveService(moveQueryRepository, moveRepository, eventRepository)

  @Test
  fun `find move by move id`() {
    val journey = journeyJ1()
    val move = moveM1(journeys = listOf(journey))

    val moveEvent = eventE1()

    whenever(
      moveQueryRepository.moveWithPersonAndJourneys(
        "M1",
        defaultSupplierSerco,
        Month.SEPTEMBER,
      ),
    ).thenReturn(move)
    whenever(eventRepository.findAllByEventableId("M1")).thenReturn(listOf(moveEvent))

    val retrievedMove = service.moveWithPersonJourneysAndEvents("M1", defaultSupplierSerco, Month.SEPTEMBER)
    assertThat(retrievedMove).isEqualTo(move)
    assertThat(retrievedMove?.events).containsExactly(moveEvent)
  }

  @Test
  fun `journeys on the move are ordered by pickup datetime`() {
    val journey1 = journeyJ1().copy(state = JourneyState.Cancelled, dropOffDateTime = null)

    val journey2 = journey1.copy(
      journeyId = "J2",
      fromNomisAgencyId = "WYI",
      toNomisAgencyId = "BOG",
      state = JourneyState.completed,
      pickUpDateTime = journey1.pickUpDateTime?.plusMinutes(1),
    )

    val moveWithJourneysOutOfOrder = moveM1(journeys = listOf(journey2, journey1))

    whenever(moveQueryRepository.moveWithPersonAndJourneys("M1", defaultSupplierSerco, Month.SEPTEMBER)).thenReturn(
      moveWithJourneysOutOfOrder,
    )
    whenever(eventRepository.findAllByEventableId("M1")).thenReturn(listOf(eventE1()))

    assertThat(
      service.moveWithPersonJourneysAndEvents(
        "M1",
        defaultSupplierSerco,
        Month.SEPTEMBER,
      )?.journeys,
    ).containsExactly(
      journey1,
      journey2,
    )
  }

  @Test
  fun `journey events on the journey are ordered correctly`() {
    val journeyStartEvent = journeyEventJE1()
    val journeyCompleteEvent =
      journeyEventJE1(eventType = EventType.JOURNEY_COMPLETE).copy(occurredAt = journeyEventJE1().occurredAt.plusHours(1))

    val journey = journeyJ1().copy(
      state = JourneyState.Cancelled,
      dropOffDateTime = null,
      events = listOf(journeyCompleteEvent, journeyStartEvent),
    )

    val moveWithJourneyEventsOutOfOrder = moveM1(journeys = listOf(journey))

    whenever(moveQueryRepository.moveWithPersonAndJourneys("M1", defaultSupplierSerco, Month.SEPTEMBER)).thenReturn(
      moveWithJourneyEventsOutOfOrder,
    )
    whenever(eventRepository.findAllByEventableId("M1")).thenReturn(listOf(eventE1()))
    whenever((eventRepository.findByEventableIdIn(listOf(journey.journeyId)))).thenReturn(
      listOf(
        journeyCompleteEvent,
        journeyStartEvent,
      ),
    )

    assertThat(
      service.moveWithPersonJourneysAndEvents(
        "M1",
        defaultSupplierSerco,
        Month.SEPTEMBER,
      )!!.journeys.first().events,
    ).containsExactly(journeyStartEvent, journeyCompleteEvent)
  }

  @Test
  fun `find move by move reference`() {
    val move = moveM1()

    whenever(moveRepository.findByReferenceAndSupplier("REF1", defaultSupplierSerco)).thenReturn(move)

    assertThat(service.findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)).isEqualTo(move)
  }

  @Test
  fun `find move by move reference not found when move has no move type`() {
    val move = moveM1().copy(moveType = null)

    whenever(moveRepository.findByReferenceAndSupplier("REF1", defaultSupplierSerco)).thenReturn(move)

    assertThat(service.findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)).isNull()
  }

  @Test
  fun `find reconciliations moves for Serco`() {
    service.candidateReconciliations(Supplier.SERCO, LocalDate.of(2021, 1, 1))
    verify(moveRepository).findCompletedCandidateReconcilableMoves(Supplier.SERCO, 2021, 1)

    service.candidateReconciliations(Supplier.SERCO, LocalDate.of(2022, 2, 1))
    verify(moveRepository).findCompletedCandidateReconcilableMoves(Supplier.SERCO, 2022, 2)
  }

  @Test
  fun `find reconciliations moves for GEOAmey`() {
    service.candidateReconciliations(Supplier.GEOAMEY, LocalDate.of(2021, 5, 10))
    verify(moveRepository).findCompletedCandidateReconcilableMoves(Supplier.GEOAMEY, 2021, 5)

    service.candidateReconciliations(Supplier.GEOAMEY, LocalDate.of(2022, 3, 30))
    verify(moveRepository).findCompletedCandidateReconcilableMoves(Supplier.GEOAMEY, 2022, 3)
  }
}
