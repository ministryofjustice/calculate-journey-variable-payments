package uk.gov.justice.digital.hmpps.pecs.jpc.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.JourneyRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.PersonPersister
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportImporter
import java.time.LocalDate

/**
 * This test loads reporting data JSONL files from the 'test/resources/reporting' folder.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class ImportServiceIntegrationTest(
  @Autowired private val timeSource: TimeSource,
  @Autowired private val priceImporter: PriceImporter,
  @Autowired private val reportImporter: ReportImporter,
  @Autowired private val movePersister: MovePersister,
  @Autowired private val personPersister: PersonPersister,
  @Autowired private val auditService: AuditService,
  @Autowired private val moveRepository: MoveRepository,
  @Autowired private val journeyRepository: JourneyRepository
) {

  @MockBean
  lateinit var monitoringService: MonitoringService

  lateinit var service: ImportService

  @BeforeEach
  internal fun beforeEach() {
    service = ImportService(
      timeSource,
      priceImporter,
      reportImporter,
      movePersister,
      personPersister,
      auditService,
      monitoringService
    )
  }

  @Test
  internal fun `given some report files contain errors the import should still complete successfully and not fail`() {
    service.importReportsOn(LocalDate.of(2020, 12, 1))

    verify(monitoringService).capture("moves: persisted 1 out of 2 for reporting feed date 2020-12-01.")
    verify(monitoringService).capture("people: persisted 3 out of 4 for reporting feed date 2020-12-01.")
    verifyNoMoreInteractions(monitoringService)
  }

  @Test
  internal fun `given a booked move which is subsequently cancelled in time, a journey for pricing should be created so the supplier can be paid`() {
    val bookedMoveToBeCancelledIdentifier = "1dbd5d37-f728-4059-99a3-70e8bdf1d362"

    service.importReportsOn(LocalDate.of(2021, 5, 7))

    assertThat(moveRepository.findById(bookedMoveToBeCancelledIdentifier).get().status).isEqualTo(MoveStatus.booked)
    assertThat(journeyRepository.findAllByMoveId(bookedMoveToBeCancelledIdentifier)).isEmpty()

    service.importReportsOn(LocalDate.of(2021, 5, 26))

    assertThat(moveRepository.findById(bookedMoveToBeCancelledIdentifier).get().status).isEqualTo(MoveStatus.cancelled)
    assertThat(journeyRepository.findAllByMoveId(bookedMoveToBeCancelledIdentifier)).hasSize(1)
  }
}
