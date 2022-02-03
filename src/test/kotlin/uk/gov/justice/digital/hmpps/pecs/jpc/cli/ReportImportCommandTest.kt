package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDate

internal class ReportImportCommandTest {

  private val importService: ImportService = mock()

  private val date: LocalDate = LocalDate.of(2020, 9, 30)

  private val commands: ReportImportCommand = ReportImportCommand(importService)

  @Test
  internal fun `given two dates the import service is called with the expected date range`() {
    commands.importReports(date, date.plusDays(1))

    verify(importService).importReportsOn(DateRange(date, date.plusDays(1)))
  }
}
