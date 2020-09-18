package uk.gov.justice.digital.hmpps.pecs.jpc.config

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnExpression("{'localstack'}.contains('\${aws.provider}')")
class ProviderConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun amazonS3(@Value("\${aws.endpoint.url}") endpoint: String,
                 @Value("\${aws.region}") region: String): AmazonS3 {
        logger.info("Using localstack AWS configuration.")

        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withPathStyleAccessEnabled(true)
                .build()
    }

    @Bean
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun locationsResourceProvider(client: AmazonS3): Schedule34LocationsProvider {
        logger.info("Using S3 resource provider.")

        return Schedule34LocationsProvider { client.getObject(GetObjectRequest("locations", it)).objectContent }
    }

    @Bean
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun sercoPricesResourceProvider(client: AmazonS3): SercoPricesProvider {
        logger.info("Using S3 resource provider.")

        return SercoPricesProvider { client.getObject(GetObjectRequest("serco", it)).objectContent }
    }

    @Bean
    @ConditionalOnProperty(name = ["aws.provider"], havingValue = "localstack")
    fun geoameyPricesResourceProvider(client: AmazonS3): GeoamyPricesProvider {
        logger.info("Using S3 resource provider.")

        return GeoamyPricesProvider { client.getObject(GetObjectRequest("geoamey", it)).objectContent }
    }
}