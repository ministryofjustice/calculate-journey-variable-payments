package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
open class ImporterConfiguration {

    @Bean
    open fun clock() = Clock.systemDefaultZone()
}