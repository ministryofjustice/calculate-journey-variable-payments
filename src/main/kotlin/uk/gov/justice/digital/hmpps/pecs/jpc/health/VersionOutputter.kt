package uk.gov.justice.digital.hmpps.pecs.jpc.health

import com.microsoft.applicationinsights.extensibility.ContextInitializer
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<VersionOutputter>()

@Configuration
class VersionOutputter(buildProperties: BuildProperties) {

  private val version = buildProperties.version

  @EventListener(ApplicationReadyEvent::class)
  fun logVersionOnStartup() {
    logger.info("Version {} started", version)
  }

  @Bean
  fun versionContextInitializer() = ContextInitializer { it.component.setVersion(version) }
}
