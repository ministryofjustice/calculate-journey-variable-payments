package uk.gov.justice.digital.hmpps.pecs.jpc.schedule

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ReportsImporter
import java.time.LocalDateTime

class ReportsImporterSchedulerTest {

  private val reportsImporter: ReportsImporter = mock()
  private val timeSource: TimeSource = TimeSource { LocalDateTime.of(2020, 11, 30, 12, 0) }

  @Test
  fun `previous days reports data import invoked with correct dates`() {
    ReportsImporterScheduler(reportsImporter, timeSource).importPreviousDaysReports()

    verify(reportsImporter).import(timeSource.date().minusDays(1), timeSource.date().minusDays(1))
  }
}
