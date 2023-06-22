package uk.gov.justice.digital.hmpps.pecs.jpc.tasks

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import java.time.LocalDate

@Component
class ImportReportsDataTask(
  @Value("\${IMPORT_REPORTS_BACKDATE_ENABLED}") private val backDateEnabled: Boolean = false,
  private val service: ImportReportsService,
  private val timeSource: TimeSource,
  monitoringService: MonitoringService,
) : Task("Import reports data", monitoringService) {

  override fun performTask() {
    timeSource.yesterday().run {
      if (backDateEnabled) {
        val startDate = service.dateOfLastImport() ?: this

        if (startDate.isBefore(this)) {
          backdateReportsFrom(startDate)

          return
        }
      }

      service.importAllReportsOn(this)
    }
  }

  private fun backdateReportsFrom(from: LocalDate) {
    from.datesUntil(timeSource.date()).forEach { service.importAllReportsOn(it) }
  }
}
