package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.ProfileRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.HistoricMovesProcessingService
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
  @Autowired private val profileRepository: ProfileRepository,
) {

  @MockBean
  lateinit var monitoringService: MonitoringService

  @Autowired
  lateinit var importReportsService: ImportReportsService

  @Autowired
  lateinit var historicMovesProcessingService: HistoricMovesProcessingService

  @Test
  fun `given a date when no files exist the import should fail`() {
    assertThatThrownBy { importReportsService.importAllReportsOn(LocalDate.of(1900, 1, 1)) }
      .hasMessageContaining("The service is missing data which may affect pricing due to missing file(s): 1900/01/01/1900-01-01-moves.jsonl, 1900/01/01/1900-01-01-events.jsonl, 1900/01/01/1900-01-01-journeys.jsonl, 1900/01/01/1900-01-01-profiles.jsonl, 1900/01/01/1900-01-01-people.jsonl")

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file(s): 1900/01/01/1900-01-01-moves.jsonl, 1900/01/01/1900-01-01-events.jsonl, 1900/01/01/1900-01-01-journeys.jsonl, 1900/01/01/1900-01-01-profiles.jsonl, 1900/01/01/1900-01-01-people.jsonl")
  }

  @Test
  fun `given a date when profile file is missing the import should fail`() {
    assertThatThrownBy { importReportsService.importAllReportsOn(LocalDate.of(2022, 6, 1)) }
      .hasMessageContaining("The service is missing data which may affect pricing due to missing file(s): 2022/06/01/2022-06-01-profiles.jsonl")

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file(s): 2022/06/01/2022-06-01-profiles.jsonl")
  }

  @Test
  fun `given a date when people file is missing the import should fail`() {
    assertThatThrownBy { importReportsService.importAllReportsOn(LocalDate.of(2022, 6, 2)) }
      .hasMessageContaining("The service is missing data which may affect pricing due to missing file(s): 2022/06/02/2022-06-02-people.jsonl")

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file(s): 2022/06/02/2022-06-02-people.jsonl")
  }

  @Test
  fun `given a date when events file is missing the import should fail`() {
    assertThatThrownBy { importReportsService.importAllReportsOn(LocalDate.of(2022, 6, 3)) }
      .hasMessageContaining("The service is missing data which may affect pricing due to missing file(s): 2022/06/03/2022-06-03-events.jsonl")

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file(s): 2022/06/03/2022-06-03-events.jsonl")
  }

  @Test
  fun `given a date when journeys file is missing the import should fail`() {
    assertThatThrownBy { importReportsService.importAllReportsOn(LocalDate.of(2022, 6, 4)) }
      .hasMessageContaining("The service is missing data which may affect pricing due to missing file(s): 2022/06/04/2022-06-04-journeys.jsonl")

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file(s): 2022/06/04/2022-06-04-journeys.jsonl")
  }

  @Test
  fun `given a date when moves file is missing the import should fail`() {
    assertThatThrownBy { importReportsService.importAllReportsOn(LocalDate.of(2022, 6, 5)) }
      .hasMessageContaining("The service is missing data which may affect pricing due to missing file(s): 2022/06/05/2022-06-05-moves.jsonl")

    verify(monitoringService).capture("The service is missing data which may affect pricing due to missing file(s): 2022/06/05/2022-06-05-moves.jsonl")
  }

  @Test
  fun `given some report files contain errors the import should still complete successfully and not fail`() {
    importReportsService.importAllReportsOn(LocalDate.of(2020, 12, 1))

    verify(monitoringService).capture("people: persisted 4 and 1 errors for reporting feed date 2020-12-01.")
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
      .map { moveRepository.findByReferenceAndSupplier(it.moveRef, it.supplier)!! to it.moveType }.toList()

    assertThat(importedDecemberMoves).hasSize(9)
    assertMovesHaveExpectedMoveTypeOrNull(importedDecemberMoves)

    val processedMoveCount = historicMovesProcessingService.process(
      DateRange(LocalDate.of(2020, 12, 1), LocalDate.of(2020, 12, 6)),
      Supplier.GEOAMEY,
    )

    assertThat(processedMoveCount).isEqualTo(9)

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

    assertThat(journeyRepository.findById("J1").get().vehicleRegistrations()).isEqualTo("ABC")
    assertThat(journeyRepository.findById("J3").get().vehicleRegistrations()).isEqualTo("ABC, DEF")
  }

  @Test
  fun `given a redirect move missing journey when imported it is marked as a multi move type`() {
    importReportsService.importAllReportsOn(date(2022, 2, 23))

    assertThat(moveRepository.findById("M1").get().moveType).isEqualTo(MoveType.MULTI)
  }

  @Test
  fun `given a completed move with a redirect event before the move start when imported it is marked as a standard move type`() {
    importReportsService.importAllReportsOn(date(2022, 2, 23))

    assertThat(moveRepository.findById("M2").get().moveType).isEqualTo(MoveType.STANDARD)
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
