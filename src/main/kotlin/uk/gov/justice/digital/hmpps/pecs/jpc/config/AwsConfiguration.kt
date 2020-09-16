package uk.gov.justice.digital.hmpps.pecs.jpc.config

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnExpression("{'localstack'}.contains('\${aws.provider}')")
class AwsConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun amazonS3(@Value("\${aws.endpoint.url}") endpoint: String,
                 @Value("\${aws.region}") region: String): AmazonS3 {
        logger.info("Using localstack AWS configuration.")

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(EndpointConfiguration(endpoint, region))
                .withPathStyleAccessEnabled(true)
                .build()
    }

    @Bean
    @Qualifier("locations")
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun locationsResourceProvider(provider: AmazonS3): ResourceProvider {
        logger.info("Using S3 resource provider.")

        return S3ResourceProvider(provider, "locations")
    }

    @Bean
    @Qualifier("serco")
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun sercoPricesResourceProvider(provider: AmazonS3): ResourceProvider {
        logger.info("Using S3 resource provider.")

        return S3ResourceProvider(provider, "serco")
    }

    @Bean
    @Qualifier("geoamey")
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun geoameyPricesResourceProvider(provider: AmazonS3): ResourceProvider {
        logger.info("Using S3 resource provider.")

        return S3ResourceProvider(provider, "geoamey")
    }
}
