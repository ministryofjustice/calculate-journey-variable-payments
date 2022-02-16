package uk.gov.justice.digital.hmpps.pecs.jpc.config

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.ReportReaderParser
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

@Configuration
class ReportImporterConfiguration {

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

fun interface SupplierPrices {
  fun get(supplier: Supplier, effectiveYear: Int): Stream<Price>
}

/**
 * To be used for providing date and or time in the applications. Enables control of time in the code (and unit tests).
 */
fun interface TimeSource {
  fun dateTime(): LocalDateTime

  fun date(): LocalDate = dateTime().toLocalDate()

  fun yesterday(): LocalDate = date().minusDays(1)
}
