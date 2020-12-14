package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.move.*
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
 class MoveServiceTest(){

    @MockBean
    lateinit var moveQueryRepository: MoveQueryRepository

    @MockBean
    lateinit var journeyRepository: JourneyRepository

    @MockBean
    lateinit var eventRepository: EventRepository

    @Test
    fun `move by move id`(){
        val service = MoveService(moveQueryRepository, journeyRepository, eventRepository)
        val journey = journey()
        val move = move(journeys = mutableSetOf(journey))

        val moveEvent = event()

        whenever(moveQueryRepository.move(eq("M1"))).thenReturn(move)
        whenever(eventRepository.findAllByEventableId(eq("M1"))).thenReturn(listOf(moveEvent))

        val retrievedMpve = service.moveWithJourneysAndEvents("M1")
        assertThat(retrievedMpve).isEqualTo(move)
        assertThat(retrievedMpve.events).containsExactly(moveEvent)

    }
}