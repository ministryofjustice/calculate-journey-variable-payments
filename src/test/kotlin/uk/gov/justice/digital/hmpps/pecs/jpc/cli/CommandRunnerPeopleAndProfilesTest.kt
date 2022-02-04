package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import java.time.LocalDate

@SpringBootTest(args = ["--import-people-profiles", "--from=2022-01-13"], webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class CommandRunnerPeopleAndProfilesTest {

  @MockBean
  private lateinit var historicMovesCommand: HistoricMovesCommand

  @MockBean
  private lateinit var bulkPriceImportCommand: BulkPriceImportCommand

  @MockBean
  private lateinit var reportImportCommand: ReportImportCommand

  @Test
  fun `people and profiles command is invoked for date 2022-01-13`() {
    verify(reportImportCommand).importPeopleAndProfiles(LocalDate.of(2022, 1, 13))

    verifyNoInteractions(bulkPriceImportCommand)
    verifyNoInteractions(historicMovesCommand)
  }
}
