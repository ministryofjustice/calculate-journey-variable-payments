package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.pecs.jpc.output.POISpreadsheetProtection
import uk.gov.justice.digital.hmpps.pecs.jpc.output.SpreadsheetProtection
import java.time.Clock
import java.time.LocalDateTime

@Configuration
class ImporterConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean
    fun jcpTemplateProvider(@Value("\${export-files.template}") templateFileLocation: String): JCPTemplateProvider {
        return JCPTemplateProvider { resourceLoader.getResource(templateFileLocation).inputStream }
    }

    @Bean
    fun timeSource() = TimeSource { LocalDateTime.now(Clock.systemDefaultZone()) }

    @Bean
    fun spreadsheetProtection(@Value("\${SPREADSHEET_PASSWORD:}") value: String?): SpreadsheetProtection {
        if (value.isNullOrBlank()) {
            logger.warn("Spreadsheet password protection is disabled.")

            return SpreadsheetProtection { it }
        }

        logger.info("Spreadsheet password protection is enabled.")

        return POISpreadsheetProtection(value)
    }
}
