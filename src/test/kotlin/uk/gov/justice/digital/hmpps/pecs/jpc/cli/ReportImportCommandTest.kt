package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDate

internal class ReportImportCommandTest {

  private val importService: ImportService = mock()
  private val date: LocalDate = LocalDate.of(2020, 9, 30)

  private val commands: ReportImportCommand = ReportImportCommand(importService)

  @Test
  internal fun `import one days reporting data`() {
    commands.importReports(date, date)

    verify(importService).importReportsOn(date)
    verify(importService, times(1)).importReportsOn(any())
  }

  @Test
  internal fun `import two days reporting data`() {
    commands.importReports(date, date.plusDays(1))

    verify(importService).importReportsOn(date)
    verify(importService).importReportsOn(LocalDate.of(2020, 10, 1))
    verify(importService, times(2)).importReportsOn(any())
  }

  @Test
  internal fun `import spanning entire leap year of reporting data`() {
    commands.importReports(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31))

    verify(importService, times(366)).importReportsOn(any())
  }
}
