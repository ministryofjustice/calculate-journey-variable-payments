package uk.gov.justice.digital.hmpps.pecs.jpc.config

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportReaderParser
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Clock
import java.time.LocalDateTime

private val logger = loggerFor<ImporterConfiguration>()

@Configuration
class ImporterConfiguration {

  @Autowired
  private lateinit var priceRepository: PriceRepository

  @Autowired
  private lateinit var reportingProvider: ReportingProvider

  @Autowired
  private lateinit var reportReaderParser: ReportReaderParser

  @Autowired
  private lateinit var monitoringString: MonitoringService

  @Bean
  fun timeSource() = TimeSource { LocalDateTime.now(Clock.systemDefaultZone()) }

  @Bean
  fun supplierPrices() =
    SupplierPrices { supplier, year -> priceRepository.findBySupplierAndEffectiveYear(supplier, year) }

  @Bean
  fun reportImporter() =
    ReportImporter(reportingProvider, monitoringString, reportReaderParser)

  // This is now needed for Spring Boot as part of moving from 2.5.3 to 3.0.0 Thymeleaf Layout Dialect
  @Bean
  fun thymeleafLayoutDialect() = LayoutDialect()
}
