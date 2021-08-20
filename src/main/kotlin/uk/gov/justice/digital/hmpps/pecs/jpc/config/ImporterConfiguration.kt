package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ObfuscatingReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportImporter
import java.time.Clock
import java.time.LocalDateTime

@Configuration
class ImporterConfiguration {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Autowired
  private lateinit var priceRepository: PriceRepository

  @Autowired
  private lateinit var reportingProvider: ReportingProvider

  @Autowired
  private lateinit var monitoringString: MonitoringService

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
