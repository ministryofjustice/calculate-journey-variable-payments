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
    lateinit var eventRepository: EventRepository

    @Test
    fun `move by move id`(){
        val service = MoveService(moveQueryRepository, eventRepository)
        val journey = journey()
        val move = move(journeys = mutableListOf(journey))

        val moveEvent = event()

        whenever(moveQueryRepository.move(eq("M1"))).thenReturn(move)
        whenever(eventRepository.findAllByEventableId(eq("M1"))).thenReturn(listOf(moveEvent))

        val retrievedMpve = service.move("M1")
        assertThat(retrievedMpve).isEqualTo(move)
        assertThat(retrievedMpve.events).containsExactly(moveEvent)

    }

    @Test
    fun `paginate`(){
        val service = MoveService(moveQueryRepository, eventRepository)
        val pageNo = 0
        val pageSize = 50
        val date = LocalDate.now()
        val pageable = PageRequest.of(pageNo, pageSize)

        val first50Moves = (1..50).map { move(moveId = "first50") }
        val second50Moves = (1..50).map { move(moveId = "second50") }

        whenever(moveQueryRepository.movesForMoveTypeInDateRange(eq(Supplier.SERCO), any(), any(), any(), eq(50), eq(0))).thenReturn(first50Moves)
        whenever(moveQueryRepository.movesForMoveTypeInDateRange(eq(Supplier.SERCO), any(), any(), any(), eq(50), eq(50))).thenReturn(second50Moves)
        whenever(moveQueryRepository.movesForMoveTypeInDateRange(eq(Supplier.SERCO), any(), any(), any(), eq(50), eq(100))).thenReturn(listOf(move(moveId = "lastOne")))
        whenever(moveQueryRepository.moveCountInDateRange(any(), any(), any())).thenReturn(101)

        val firstPage = service.paginatedMovesForMoveType(Supplier.SERCO, MoveType.STANDARD, date, pageable)

        assertThat(firstPage.totalElements).isEqualTo(101) // total number of moves is 101
        assertThat(firstPage.number).isEqualTo(0) // page 0
        assertThat(firstPage.pageable.pageSize).isEqualTo(50) // page size (number of moves per page) is 50
        assertThat(firstPage.numberOfElements).isEqualTo(50) // number of moves in this page is 50

        assertThat(firstPage.nextPageable().pageNumber).isEqualTo(1) // next page number is 1

        assertThat(firstPage.totalPages).isEqualTo(3) // 3 pages in total
        assertThat(firstPage.isFirst).isTrue // first page
        assertThat(firstPage.hasPrevious()).isFalse // no previous page

        assertThat(firstPage.last().moveId).isEqualTo("first50")
        val secondPage = service.paginatedMovesForMoveType(Supplier.SERCO, MoveType.STANDARD, date, firstPage.nextPageable())
        assertThat(secondPage.first().moveId).isEqualTo("second50")

        val lastPage = service.paginatedMovesForMoveType(Supplier.SERCO, MoveType.STANDARD, date, secondPage.nextPageable())
        assertThat(lastPage.numberOfElements).isEqualTo(1) // just one element
        assertThat(lastPage.first().moveId).isEqualTo("lastOne")









    }
}