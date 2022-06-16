package uk.gov.justice.digital.hmpps.pecs.jpc.config.tasks

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.tasks.BackfillReportsTask
import uk.gov.justice.digital.hmpps.pecs.jpc.tasks.ImportReportsDataTask
import uk.gov.justice.digital.hmpps.pecs.jpc.tasks.PreviousDaysLocationMappingTask
import uk.gov.justice.digital.hmpps.pecs.jpc.tasks.ReprocessExistingMovesTask

/**
 * Config responsible for setting up the actual tasks to be scheduled.
 */
@ConditionalOnWebApplication
@Component
class TasksSchedulingConfiguration(
  val importReportsDataTask: ImportReportsDataTask,
  val previousDaysLocationMapping: PreviousDaysLocationMappingTask,
  val backfillReports: BackfillReportsTask,
  val reprocessExistingMovesTask: ReprocessExistingMovesTask
) {

  @Scheduled(cron = "\${CRON_IMPORT_REPORTS}")
  @SchedulerLock(name = "importReports")
  fun previousDaysReportsTask() {
    importReportsDataTask.execute()
  }

  @Scheduled(cron = "\${CRON_AUTOMATIC_LOCATION_MAPPING}")
  @SchedulerLock(name = "automaticLocationMapping")
  fun automaticLocationMapping() {
    previousDaysLocationMapping.execute()
  }

  @Scheduled(cron = "\${CRON_BACKFILL_REPORTS}")
  @SchedulerLock(name = "backfillReports")
  fun backfillReports() {
    backfillReports.execute()
  }

  @Scheduled(cron = "\${CRON_REPROCESS_EXISTING_MOVES}")
  @SchedulerLock(name = "reprocessExistingMoves")
  fun reprocessExistingMoves() {
    reprocessExistingMovesTask.execute()
  }
}
