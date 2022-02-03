package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.HistoricMovesProcessingService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDate

class HistoricMovesCommandTest {

  private val historicMovesProcessingService: HistoricMovesProcessingService = mock()

  private val date: LocalDate = LocalDate.of(2020, 9, 30)

  private val commands: HistoricMovesCommand = HistoricMovesCommand(historicMovesProcessingService)

  @Test
  internal fun `given two dates the import service is called with the expected date range`() {
    commands.process(date, date.plusDays(1), Supplier.SERCO)

    verify(historicMovesProcessingService).process(DateRange(date, date.plusDays(1)), Supplier.SERCO)
  }
}
