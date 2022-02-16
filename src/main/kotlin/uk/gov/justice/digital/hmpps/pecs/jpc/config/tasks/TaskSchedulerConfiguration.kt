package uk.gov.justice.digital.hmpps.pecs.jpc.config.tasks

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import javax.sql.DataSource

private val logger = loggerFor<TaskSchedulerConfiguration>()

/**
 * Config responsible for setting up the actual scheduler i.e. the thing responsible for running tasks in a distributed
 * environment. In our case we're using Shedlock which makes sure only one task is ever run in a distributed environment.
 */
@Configuration
@ConditionalOnWebApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtLeastFor = "PT1M", defaultLockAtMostFor = "PT30M")
class TaskSchedulerConfiguration {

  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider {
    logger.info("Configuring scheduler lock provider.")

    return JdbcTemplateLockProvider(dataSource)
  }
}
