package uk.gov.justice.digital.hmpps.pecs.jpc.service

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.price.PriceImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovePersister
import uk.gov.justice.digital.hmpps.pecs.jpc.move.PersonPersister
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
internal class ImportServiceIntegrationTest(
  @Autowired private val timeSource: TimeSource,
  @Autowired private val priceImporter: PriceImporter,
  @Autowired private val reportImporter: ReportImporter,
  @Autowired private val movePersister: MovePersister,
  @Autowired private val personPersister: PersonPersister,
  @Autowired private val auditService: AuditService
) {

  @MockBean
  lateinit var monitoringService: MonitoringService

  lateinit var service: ImportService

  @BeforeEach
  internal fun beforeEach() {
    service = ImportService(timeSource, priceImporter, reportImporter, movePersister, personPersister, auditService, monitoringService)
  }

  @Test
  internal fun `import should complete even when there are reports files with errors in them`() {
    service.importReportsOn(LocalDate.of(2020, 12, 1))

    verify(monitoringService).capture("moves: persisted 1 out of 2 for reporting date 2020-12-01.")
    verify(monitoringService).capture("people: persisted 3 out of 4 for reporting date 2020-12-01.")
    verifyNoMoreInteractions(monitoringService)
  }
}
