package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.boot.ApplicationArguments
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import java.time.LocalDate
import java.time.LocalDateTime

class DailyReportsImporterRunnerTest {

  private val importReportsService: ImportReportsService = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2022, 6, 15, 0, 0) }
  private val arguments: ApplicationArguments = mock()

  @Test
  fun `move data is backdated when backdating enabled`() {
    whenever(importReportsService.dateOfLastImport()).thenReturn(LocalDate.of(2022, 6, 12))

    DailyReportsImporterRunner(true, importReportsService, timeSource).run(arguments)

    verify(importReportsService).dateOfLastImport()
    verify(importReportsService).importAllReportsOn(LocalDate.of(2022, 6, 12))
    verify(importReportsService).importAllReportsOn(LocalDate.of(2022, 6, 13))
    verify(importReportsService).importAllReportsOn(LocalDate.of(2022, 6, 14))
    verifyNoMoreInteractions(importReportsService)
  }

  @Test
  fun `move data is imported for yesterday only when backdating is disabled`() {
    DailyReportsImporterRunner(false, importReportsService, timeSource).run(arguments)

    verify(importReportsService, never()).dateOfLastImport()
    verify(importReportsService).importAllReportsOn(timeSource.yesterday())
  }
}
