package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.pecs.jpc.config.*
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.ReportImporter
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@TestConfiguration
class TestConfig {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean
    fun clock(): Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    @Bean
    fun timeSource(): TimeSource {
        return TimeSource { LocalDateTime.now(Clock.fixed(Instant.now(), ZoneId.systemDefault())) }
    }

    @Bean
    fun locationsResourceProvider(): Schedule34LocationsProvider {
        return Schedule34LocationsProvider { resourceLoader.getResource("classpath:/spreadsheets/locations.xlsx").inputStream }
    }

    @Bean
    fun sercoPricesResourceProvider(): SercoPricesProvider {
        return SercoPricesProvider { resourceLoader.getResource("classpath:/spreadsheets/supplier_b_prices.xlsx").inputStream }
    }

    @Bean
    fun geoameyPricesResourceProvider(): GeoameyPricesProvider {
        return GeoameyPricesProvider { resourceLoader.getResource("classpath:/spreadsheets/supplier_a_prices.xlsx").inputStream }
    }

    @Bean
    fun reportingResourceProvider(): ReportingProvider {
        return ReportingProvider { resourceLoader.getResource("classpath:/reporting/$it").file.readText() }
    }

    @Bean
    fun reportImporter() = ReportImporter(reportingResourceProvider(), timeSource())

    @Bean
    fun jpcTemplateProvider(): JPCTemplateProvider {
        return JPCTemplateProvider { resourceLoader.getResource("classpath:/spreadsheets/JPC_template.xlsx").inputStream }
    }
}