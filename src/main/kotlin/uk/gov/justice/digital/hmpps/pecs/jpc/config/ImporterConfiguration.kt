package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.io.File
import java.time.Clock

@Configuration
class ImporterConfiguration {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean
    fun clock() = Clock.systemDefaultZone()

    @Bean
    @Qualifier(value ="spreadsheet-template")
    fun file(@Value("\${export-files.template}") location: String) : File = resourceLoader.getResource(location).file
}
