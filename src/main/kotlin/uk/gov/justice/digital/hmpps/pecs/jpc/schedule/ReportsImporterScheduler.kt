package uk.gov.justice.digital.hmpps.pecs.jpc.schedule

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.ReportsImporter

@ConditionalOnWebApplication
@Component
class ReportsImporterScheduler(private val reportsImporter: ReportsImporter, private val timeSource: TimeSource) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Scheduled(cron = "\${CRON_IMPORT_REPORTS}")
  @SchedulerLock(name = "importReports")
  fun importPreviousDaysReports() {
    val to = timeSource.date().minusDays(1)
    val from = to.minusDays(1)

    logger.info("Importing previous days reporting data: from $from to $to.")

    reportsImporter.import(from, to)
  }
}
