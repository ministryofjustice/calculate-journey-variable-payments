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
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.ReportingImporter

@Configuration
@ConditionalOnExpression("{'s3'}.contains('\${resources.provider}')")
class S3ProviderConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun amazonS3(@Value("\${resources.endpoint.url:}") endpoint: String,
                 @Value("\${AWS_DEFAULT_REGION:eu-west-2}") region: String): AmazonS3 {
        logger.info("Using AWS configuration.")

        val builder = if(endpoint.isNotEmpty())
            // Localstack
            AmazonS3ClientBuilder.standard().withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, region))
        else
            // Real AWS S3
            AmazonS3ClientBuilder.standard().withRegion(region)

        return builder.withPathStyleAccessEnabled(true).build()
    }

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun locationsResourceProvider(client: AmazonS3): Schedule34LocationsProvider {
        logger.info("Using AWS S3 provider for Schedule 34 locations.")

        return Schedule34LocationsProvider { client.getObject(GetObjectRequest("locations", it)).objectContent }
    }

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun sercoPricesResourceProvider(client: AmazonS3): SercoPricesProvider {
        logger.info("Using AWS S3 provider for Serco prices.")

        return SercoPricesProvider { client.getObject(GetObjectRequest("serco", it)).objectContent }
    }

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun geoameyPricesResourceProvider(client: AmazonS3): GeoamyPricesProvider {
        logger.info("Using AWS S3 provider for Geoamey prices.")

        return GeoamyPricesProvider { client.getObject(GetObjectRequest("geoamey", it)).objectContent }
    }


    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun reportingResourceProvider(client: AmazonS3, @Value("\${S3_REPORTING_BUCKET:jpc}") reportingBucketName: String = "jpc"): ReportingProvider {
        logger.info("Using AWS S3 resource provider for reporting.")
        return ReportingProvider {
            client.getObjectAsString(reportingBucketName, it)
        }
    }
}