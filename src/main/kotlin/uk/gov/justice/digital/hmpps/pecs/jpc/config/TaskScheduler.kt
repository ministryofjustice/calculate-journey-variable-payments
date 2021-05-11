package uk.gov.justice.digital.hmpps.pecs.jpc.config

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.tasks.PreviousDaysLocationMappingTask
import uk.gov.justice.digital.hmpps.pecs.jpc.tasks.PreviousDaysReportsTask

@ConditionalOnWebApplication
@Component
class TaskScheduler(
  val previousDaysReportsTask: PreviousDaysReportsTask,
  val previousDaysLocationMappingTask: PreviousDaysLocationMappingTask
) {

  @Scheduled(cron = "\${CRON_IMPORT_REPORTS}")
  @SchedulerLock(name = "importReports")
  fun previousDaysReportsTask() {
    previousDaysReportsTask.execute()
  }

  @Scheduled(cron = "\${CRON_AUTOMATIC_LOCATION_MAPPING}")
  @SchedulerLock(name = "automaticLocationMapping")
  fun automaticLocationMapping() {
    previousDaysLocationMappingTask.execute()
  }
}
