package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import java.time.LocalDate

@SpringBootTest(args = ["--report-import", "--from=2022-01-13", "--to=2022-01-14"], webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class CommandRunnerImporterCommandsTest {

  @MockitoBean
  private lateinit var historicMovesCommand: HistoricMovesCommand

  @MockitoBean
  private lateinit var bulkPriceImportCommand: BulkPriceImportCommand

  @MockitoBean
  private lateinit var reportImportCommand: ReportImportCommand

  @Test
  fun `report importer command is invoked for dates 2022-01-13 and 2022-01-14`() {
    verify(reportImportCommand).importReports(LocalDate.of(2022, 1, 13), LocalDate.of(2022, 1, 14))

    verifyNoInteractions(bulkPriceImportCommand)
    verifyNoInteractions(historicMovesCommand)
  }
}
