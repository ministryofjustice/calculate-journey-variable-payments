package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService
import java.time.LocalDate

/**
 * When the application is started in non-web mode and the 'daily-reports' profile is used this runner will execute and
 * import the daily reports for the previous day. Upon completion the application will exit.
 */
@ConditionalOnNotWebApplication
@Component
@Profile("daily-reports")
class DailyReportsImporterRunner(
  @Value("\${IMPORT_REPORTS_BACKDATE_ENABLED}") private val backDateEnabled: Boolean = false,
  private val service: ImportReportsService,
  private val timeSource: TimeSource,
) : ApplicationRunner {

  override fun run(arguments: ApplicationArguments) {
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
