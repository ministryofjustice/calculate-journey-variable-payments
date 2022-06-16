package uk.gov.justice.digital.hmpps.pecs.jpc

import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.GeoameyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.ReportLookup
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.aws.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveQueryRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MonitoringService
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ReportImporter
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.StandardReportReaderParser
import java.io.InputStreamReader
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.sql.DataSource

@TestConfiguration
class TestConfig {

  @Autowired
  private lateinit var resourceLoader: ResourceLoader

  @Bean
  fun moveQueryRepository(@Qualifier("dataSource") dataSource: DataSource) =
    MoveQueryRepository(JdbcTemplate(dataSource))

  @Bean
  fun journeyQueryRepository(@Qualifier("dataSource") dataSource: DataSource) =
    JourneyQueryRepository(JdbcTemplate(dataSource))

  @Bean
  fun timeSource() = TimeSource { LocalDateTime.now(Clock.fixed(Instant.now(), ZoneId.systemDefault())) }

  @Bean
  fun dataSource(): DataSource = DataSourceBuilder.create().url("jdbc:h2:mem:testdb;MODE=PostgreSQL").build()

  @Bean
  fun locationsResourceProvider(): Schedule34LocationsProvider {
    return Schedule34LocationsProvider { resourceLoader.getResource("classpath:/spreadsheets/locations.xlsx").inputStream }
  }

  @Bean
  fun sercoPricesResourceProvider() =
    SercoPricesProvider { resourceLoader.getResource("classpath:/spreadsheets/supplier_b_prices.xlsx").inputStream }

  @Bean
  fun geoameyPricesResourceProvider() =
    GeoameyPricesProvider { resourceLoader.getResource("classpath:/spreadsheets/supplier_a_prices.xlsx").inputStream }

  @Bean
  fun reportingResourceProvider() =
    ReportingProvider { resourceLoader.getResource("classpath:/reporting/$it").file.readText() }

  @Bean
  fun reportReaderParser() =
    StandardReportReaderParser { InputStreamReader(resourceLoader.getResource("classpath:/reporting/$it").inputStream) }

  @Bean
  fun reportImporter() = ReportImporter(reportingResourceProvider(), mock { MonitoringService() }, reportReaderParser())

  @Bean
  fun reportLookup() = ReportLookup { resourceLoader.getResource("classpath:/reporting/$it").exists() }
}
