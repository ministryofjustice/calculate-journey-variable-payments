package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDate

internal class ImportCommandsTest {

  private val importService: ImportService = mock()
  private val date: LocalDate = LocalDate.of(2020, 9, 30)

  private val commands: ImportCommands = ImportCommands(importService)

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
