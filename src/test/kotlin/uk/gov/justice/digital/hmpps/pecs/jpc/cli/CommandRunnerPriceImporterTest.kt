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

@SpringBootTest(args = ["--price-import", "--supplier=serco", "--year=2021", "--action=ERROR"], webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
class CommandRunnerPriceImporterTest {

  @MockitoBean
  private lateinit var historicMovesCommand: HistoricMovesCommand

  @MockitoBean
  private lateinit var bulkPriceImportCommand: BulkPriceImportCommand

  @MockitoBean
  private lateinit var reportImportCommand: ReportImportCommand

  @Test
  fun `price importer command is invoked for supplier Serco with year 2021`() {
    verify(bulkPriceImportCommand).bulkImportPricesFor(Supplier.SERCO, 2021)

    verifyNoInteractions(reportImportCommand)
    verifyNoInteractions(historicMovesCommand)
  }
}
