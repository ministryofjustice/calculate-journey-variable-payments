package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader
import java.io.InputStream

@Configuration
@ConditionalOnMissingBean(AwsConfiguration::class)
class DevConfiguration {

    @Autowired
    private lateinit var resourceLoader: ResourceLoader

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @Qualifier("locations")
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "test")
    fun locationsResourceProvider() : ResourceProvider {
        logger.info("Using anonymous resource provider for locations.")

        return resourceProvider()
    }

    @Bean
    @Qualifier("serco")
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "test")
    fun sercoPricesResourceProvider(): ResourceProvider {
        logger.info("Using anonymous resource provider for serco.")

        return resourceProvider()
    }

    @Bean
    @Qualifier("geoamey")
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "test")
    fun geoameyPricesResourceProvider(): ResourceProvider {
        logger.info("Using anonymous resource provider for geoamey.")

        return resourceProvider()
    }

    private fun resourceProvider() : ResourceProvider = object: ResourceProvider {
        override fun get(resourceName: String): InputStream {
            return resourceLoader.getResource(resourceName).inputStream
        }
    }
}
