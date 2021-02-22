package uk.gov.justice.digital.hmpps.pecs.jpc.schedule

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService
import java.time.LocalDateTime

class ReportsImporterSchedulerTest {

  private val importService: ImportService = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 11, 30, 12, 0) }

  @Test
  fun `previous days reports data import invoked with correct dates`() {
    ReportsImporterScheduler(importService, timeSource).importPreviousDaysReports()

    verify(importService).importReportsOn(timeSource.date().minusDays(1))
  }
}
