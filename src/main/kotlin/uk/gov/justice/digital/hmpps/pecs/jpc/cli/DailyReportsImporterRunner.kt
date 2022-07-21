package uk.gov.justice.digital.hmpps.pecs.jpc.cli

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ImportReportsService

/**
 * When the application is started in non-web mode and the 'daily-reports' profile is used this runner will execute and
 * import the daily reports for the previous day. Upon completion the application will exit.
 */
@ConditionalOnNotWebApplication
@Component
@Profile("daily-reports")
class DailyReportsImporterRunner(
  private val service: ImportReportsService,
  private val timeSource: TimeSource,
) : ApplicationRunner {

  override fun run(arguments: ApplicationArguments) {
    timeSource.yesterday().run {
      service.importAllReportsOn(this)
    }
  }
}
