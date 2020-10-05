package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.ReportingProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.ReportingImporter
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@TestConfiguration
class TestConfig {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean
    fun clock(): Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

    @Bean
    fun locationsResourceProvider(@Value("\${import-files.locations}") locationsFile: String): Schedule34LocationsProvider {
        return Schedule34LocationsProvider { resourceLoader.getResource(locationsFile).inputStream }
    }

    @Bean
    fun sercoPricesResourceProvider(@Value("\${import-files.serco-prices}") sercoPricesFile: String): SercoPricesProvider {
        return SercoPricesProvider { resourceLoader.getResource(sercoPricesFile).inputStream }
    }

    @Bean
    fun geoameyPricesResourceProvider(@Value("\${import-files.geo-prices}") geoPricesFile: String): GeoamyPricesProvider {
        return GeoamyPricesProvider { resourceLoader.getResource(geoPricesFile).inputStream }
    }

    @Bean
    fun reportingResourceProvider(): ReportingProvider {
        return ReportingProvider { resourceLoader.getResource("classpath:/reporting/$it").file.readText() }
    }

    @Bean
    fun reportImporter() = ReportingImporter(reportingResourceProvider(), clock())
}