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
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate

@SpringBootTest(
  args = ["--process-historic-moves", "--supplier=serco", "--from=2021-01-01", "--to=2021-01-01"],
  webEnvironment = WebEnvironment.NONE,
)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class CommandRunnerHistoricMovesCommandTest {

  @MockitoBean
  private lateinit var bulkPriceImportCommand: BulkPriceImportCommand

  @MockitoBean
  private lateinit var reportImportCommand: ReportImportCommand

  @MockitoBean
  private lateinit var historicMovesCommand: HistoricMovesCommand

  @Test
  fun `price importer command is invoked for supplier Serco with year 2021`() {
    verify(historicMovesCommand).process(
      LocalDate.of(2021, 1, 1),
      LocalDate.of(2021, 1, 1),
      Supplier.SERCO,
    )

    verifyNoInteractions(bulkPriceImportCommand)
    verifyNoInteractions(reportImportCommand)
  }
}
