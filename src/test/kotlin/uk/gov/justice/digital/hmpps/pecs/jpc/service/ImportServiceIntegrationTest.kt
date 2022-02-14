package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.ProfileRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDate

/**
 * This test loads reporting data JSONL files from the 'test/resources/reporting' folder.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ImportServiceIntegrationTest(
  @Autowired private val moveRepository: MoveRepository,
  @Autowired private val journeyRepository: JourneyRepository,
  @Autowired private val peopleRepository: ProfileRepository,
  @Autowired private val profileRepository: ProfileRepository
) {

  @MockBean
  lateinit var monitoringService: MonitoringService

  @Autowired
  lateinit var importReportsService: ImportReportsService

  @Autowired
  lateinit var historicMovesProcessingService: HistoricMovesProcessingService

  @Test
  fun `given some report files contain errors the import should still complete successfully and not fail`() {
    importReportsService.importAllReportsOn(LocalDate.of(2020, 12, 1))

    verify(monitoringService).capture("people: persisted 3 and 1 errors for reporting feed date 2020-12-01.")
    verifyNoMoreInteractions(monitoringService)
  }

  @Test
  fun `given a booked move which is subsequently cancelled in time, a journey for pricing should be created so the supplier can be paid`() {
    val bookedMoveToBeCancelledIdentifier = "1dbd5d37-f728-4059-99a3-70e8bdf1d362"

    importReportsService.importAllReportsOn(LocalDate.of(2021, 5, 7))

    assertThat(moveRepository.findById(bookedMoveToBeCancelledIdentifier).get().status).isEqualTo(MoveStatus.booked)
    assertThat(journeyRepository.findAllByMoveId(bookedMoveToBeCancelledIdentifier)).isEmpty()

    importReportsService.importAllReportsOn(LocalDate.of(2021, 5, 26))

    assertThat(moveRepository.findById(bookedMoveToBeCancelledIdentifier).get().status).isEqualTo(MoveStatus.cancelled)
    assertThat(journeyRepository.findAllByMoveId(bookedMoveToBeCancelledIdentifier)).hasSize(1)
  }

  @Test
  fun `given GEOAmey moves imported for Dec 2020 when processed the moves and journeys should be unchanged`() {
    assertThat(moveRepository.findAll()).isEmpty()
    assertThat(journeyRepository.findAll()).isEmpty()

    listOf(
      date(2020, 12, 1),
      date(2020, 12, 2),
      date(2020, 12, 3),
      date(2020, 12, 4),
      date(2020, 12, 5),
      date(2020, 12, 6),
    ).forEach {
      importReportsService.importAllReportsOn(it)
    }

    val createdMoves = moveRepository.findAll()
    val createdJourneys = journeyRepository.findAll()

    assertThat(createdMoves).hasSize(9)
    assertThat(createdJourneys).hasSize(13)

    val importedDecemberMoves = geoMovesDec2020()
      .map { moveRepository.findByReferenceAndSupplier(it.moveRef, it.supplier).orElseThrow() to it.moveType }.toList()

    assertThat(importedDecemberMoves).hasSize(9)
    assertMovesHaveExpectedMoveTypeOrNull(importedDecemberMoves)

    val processedMoveCount = historicMovesProcessingService.process(
      DateRange(LocalDate.of(2020, 12, 1), LocalDate.of(2020, 12, 6)),
      Supplier.GEOAMEY
    )

    // Note out of the 9 moves one is never assigned a move type hence. The processor ignores these moves, so there are 8 and not 9 in this case.
    assertThat(processedMoveCount).isEqualTo(8)

    // Check moves have not changed
    assertThat(importedDecemberMoves.map { it.first }).hasSameElementsAs(createdMoves)

    // Check journeys have not changed
    assertThat(journeyRepository.findAll()).hasSameElementsAs(createdJourneys)
  }

  private fun date(year: Int, month: Int, day: Int) = LocalDate.of(year, month, day)

  @Test
  fun `given people and profiles reports files when we import then people and profiles are created`() {
    importReportsService.importAllReportsOn(LocalDate.of(2022, 1, 2))

    assertThat(profileRepository.findAll()).hasSize(3)
    assertThat(peopleRepository.findAll()).hasSize(3)
  }

  @Test
  fun `given complete moves with vehicle reg on journey and events when imported the registrations are applied accordingly`() {
    importReportsService.importAllReportsOn(date(2020, 12, 1))

    assertThat(journeyRepository.findById("J1").get().vehicleRegistration).isEqualTo("ABC")
    assertThat(journeyRepository.findById("J3").get().vehicleRegistration).isEqualTo("ABC, DEF")
  }

  private fun assertMovesHaveExpectedMoveTypeOrNull(moves: List<Pair<Move, MoveType?>>) {
    moves.forEach { move -> assertThat(move.first.moveType).isEqualTo(move.second) }
  }

  private fun geoMovesDec2020() = listOf(
    TestMove("STANDARDMOVE1", Supplier.GEOAMEY, MoveType.STANDARD),
    TestMove("STANDARDMOVE2", Supplier.GEOAMEY, MoveType.STANDARD),
    TestMove("REDIRMOVE", Supplier.GEOAMEY, MoveType.REDIRECTION),
    TestMove("LONGMOVE", Supplier.GEOAMEY, MoveType.LONG_HAUL),
    TestMove("LOCKMOVE", Supplier.GEOAMEY, MoveType.LOCKOUT),
    TestMove("MULTIMOVE", Supplier.GEOAMEY, MoveType.MULTI),
    TestMove("CANCELLED_BEFORE_3PM", Supplier.GEOAMEY, MoveType.CANCELLED),
    TestMove("CANCELLED_AFTER_3PM", Supplier.GEOAMEY, MoveType.CANCELLED),
    TestMove("CANCELLED_AFTER_3PM_NOT_BY_PMU", Supplier.GEOAMEY),
  )

  private data class TestMove(val moveRef: String, val supplier: Supplier, val moveType: MoveType? = null)
}
