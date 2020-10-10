package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.time.Clock

@Configuration
class ImporterConfiguration {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean
    fun clock() = Clock.systemDefaultZone()

    @Bean
    fun jcpTemplateProvider(@Value("\${export-files.template}") templateFileLocation: String): JCPTemplateProvider {
        return JCPTemplateProvider { resourceLoader.getResource(templateFileLocation).inputStream }
    }
}
