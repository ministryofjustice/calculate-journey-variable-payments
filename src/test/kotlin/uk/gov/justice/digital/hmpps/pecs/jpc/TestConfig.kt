package uk.gov.justice.digital.hmpps.pecs.jpc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ResourceLoader
import uk.gov.justice.digital.hmpps.pecs.jpc.config.GeoamyPricesProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.Schedule34LocationsProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.config.SercoPricesProvider
import java.time.Clock

@TestConfiguration
class TestConfig {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    @Bean
    fun clock() = Clock.systemDefaultZone()

    @Bean
    fun locationsResourceProvider(): Schedule34LocationsProvider {
        return Schedule34LocationsProvider { resourceLoader.getResource(it).inputStream }
    }

    @Bean
    fun sercoPricesResourceProvider(): SercoPricesProvider {
        return SercoPricesProvider { resourceLoader.getResource(it).inputStream }
    }

    @Bean
    fun geoameyPricesResourceProvider(): GeoamyPricesProvider {
        return GeoamyPricesProvider { resourceLoader.getResource(it).inputStream }
    }
}