package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ObfuscatingReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import java.time.Clock
import java.time.LocalDateTime

@Configuration
class ImporterConfiguration {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Autowired
  private lateinit var resourceLoader: ResourceLoader

  @Autowired
  private lateinit var priceRepository: PriceRepository

  @Autowired
  private lateinit var reportingProvider: ReportingProvider

  @Autowired
  private lateinit var monitoringString: MonitoringService

  @Bean
  fun jpcTemplateProvider(@Value("\${export-files.template}") templateFileLocation: String): JPCTemplateProvider {
    return JPCTemplateProvider { resourceLoader.getResource(templateFileLocation).inputStream }
  }

  @Bean
  fun timeSource() = TimeSource { LocalDateTime.now(Clock.systemDefaultZone()) }

  @Bean
  fun supplierPrices() =
    SupplierPrices { supplier, year -> priceRepository.findBySupplierAndEffectiveYear(supplier, year) }

  @Bean
  fun reportImporter(@Value("\${SENTRY_ENVIRONMENT:}") env: String): ReportImporter {
    return if (env.trim().uppercase() == "LOCAL") {
      logger.warn("Running importer in PII obfuscation mode")
      ObfuscatingReportImporter(reportingProvider, monitoringString)
    } else {
      logger.warn("Running importer in PII mode")
      ReportImporter(reportingProvider, monitoringString)
    }
  }
}
