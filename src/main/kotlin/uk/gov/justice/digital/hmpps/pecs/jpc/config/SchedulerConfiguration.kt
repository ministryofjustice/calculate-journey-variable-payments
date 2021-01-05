package uk.gov.justice.digital.hmpps.pecs.jpc.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

@Configuration
@ConditionalOnWebApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtLeastFor = "PT1M", defaultLockAtMostFor = "PT30M")
class SchedulerConfiguration {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Bean
  fun lockProvider(dataSource: DataSource): LockProvider {
    logger.info("Configuring scheduler lock provider.")

    return JdbcTemplateLockProvider(dataSource)
  }
}
