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
import uk.gov.justice.digital.hmpps.pecs.jpc.move.*
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
 class MoveServiceTest(){

    @MockBean
    lateinit var moveQueryRepository: MoveQueryRepository

    @MockBean
    lateinit var eventRepository: EventRepository

    @MockBean
    lateinit var moveRepository: MoveRepository

    @Test
    fun `move by move id`(){
        val service = MoveService(moveQueryRepository, moveRepository, eventRepository)
        val journey = journey()
        val move = move(journeys = listOf(journey))

        val moveEvent = event()

        whenever(moveQueryRepository.moveWithPersonAndJourneys(eq("M1"))).thenReturn(move)
        whenever(eventRepository.findAllByEventableId(eq("M1"))).thenReturn(listOf(moveEvent))

        val retrievedMpve = service.moveWithPersonJourneysAndEvents("M1")
        assertThat(retrievedMpve).isEqualTo(move)
        assertThat(retrievedMpve.events).containsExactly(moveEvent)

    }

    @Test
    fun `find move by move reference`(){
        val service = MoveService(moveQueryRepository, moveRepository, eventRepository)

        val move = move()
        whenever(moveRepository.findByReference(eq("REF1"))).thenReturn(Optional.of(move))

        val retrievedMpve = service.findMoveByReference("REF1")
        assertThat(retrievedMpve).isEqualTo(Optional.of(move))

    }
}