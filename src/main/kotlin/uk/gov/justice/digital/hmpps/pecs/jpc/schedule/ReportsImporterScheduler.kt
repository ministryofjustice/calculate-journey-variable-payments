package uk.gov.justice.digital.hmpps.pecs.jpc.schedule

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.service.ImportService

@ConditionalOnWebApplication
@Component
class ReportsImporterScheduler(
  @Autowired private val importService: ImportService,
  private val timeSource: TimeSource
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Scheduled(cron = "\${CRON_IMPORT_REPORTS}")
  @SchedulerLock(name = "importReports")
  fun importPreviousDaysReports() {
    val yesterday = timeSource.date().minusDays(1)

    logger.info("Importing previous days reporting data: $yesterday.")

    importService.importReports(yesterday, yesterday)
  }
}
