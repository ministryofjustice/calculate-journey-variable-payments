package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.defaultSupplierSerco
import uk.gov.justice.digital.hmpps.pecs.jpc.move.EventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.move.eventE1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1
import java.util.Optional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class MoveServiceTest {

  @MockBean
  lateinit var moveQueryRepository: MoveQueryRepository

  @MockBean
  lateinit var eventRepository: EventRepository

  @MockBean
  lateinit var moveRepository: MoveRepository

  lateinit var service: MoveService

  @Test
  fun `find move by move id`() {
    val service = MoveService(moveQueryRepository, moveRepository, eventRepository)
    val journey = journeyJ1()
    val move = moveM1(journeys = listOf(journey))

    val moveEvent = eventE1()

    whenever(moveQueryRepository.moveWithPersonAndJourneys(eq("M1"), eq(defaultSupplierSerco))).thenReturn(move)
    whenever(eventRepository.findAllByEventableId(eq("M1"))).thenReturn(listOf(moveEvent))

    val retrievedMove = service.moveWithPersonJourneysAndEvents("M1", defaultSupplierSerco)
    assertThat(retrievedMove).isEqualTo(move)
    assertThat(retrievedMove?.events).containsExactly(moveEvent)
  }

  @Test
  fun `journeys on the move are ordered by pickup datetime`() {
    val service = MoveService(moveQueryRepository, moveRepository, eventRepository)

    val journey1 = journeyJ1().copy(state = JourneyState.cancelled, dropOffDateTime = null)

    val journey2 = journey1.copy(
      journeyId = "J2",
      fromNomisAgencyId = "WYI",
      toNomisAgencyId = "BOG",
      state = JourneyState.completed,
      pickUpDateTime = journey1.pickUpDateTime?.plusMinutes(1)
    )

    val moveWithJourneysOutOfOrder = moveM1(journeys = listOf(journey2, journey1))

    whenever(moveQueryRepository.moveWithPersonAndJourneys(eq("M1"), eq(defaultSupplierSerco))).thenReturn(moveWithJourneysOutOfOrder)
    whenever(eventRepository.findAllByEventableId(eq("M1"))).thenReturn(listOf(eventE1()))

    assertThat(service.moveWithPersonJourneysAndEvents("M1", defaultSupplierSerco)?.journeys).containsExactly(journey1, journey2)
  }

  @Test
  fun `find move by move reference`() {
    val service = MoveService(moveQueryRepository, moveRepository, eventRepository)

    val move = moveM1()
    whenever(moveRepository.findByReferenceAndSupplier(eq("REF1"), eq(defaultSupplierSerco))).thenReturn(
      Optional.of(
        move
      )
    )

    val retrievedMpve = service.findMoveByReferenceAndSupplier("REF1", defaultSupplierSerco)
    assertThat(retrievedMpve).isEqualTo(Optional.of(move))
  }
}
