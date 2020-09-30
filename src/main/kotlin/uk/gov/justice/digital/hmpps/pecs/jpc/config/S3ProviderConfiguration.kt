package uk.gov.justice.digital.hmpps.pecs.jpc.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@ConditionalOnExpression("{'s3'}.contains('\${resources.provider}')")
class S3ProviderConfiguration {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun jpcAmazonS3(@Value("\${resources.endpoint.url:}") endpoint: String,
                 @Value("\${AWS_DEFAULT_REGION:eu-west-2}") region: String,
                 @Value("\${JPC_AWS_ACCESS_KEY_ID:}") accessKey: String,
                 @Value("\${JPC_AWS_SECRET_ACCESS_KEY:}") secretKey: String): AmazonS3 {

        return amazonS3(endpoint, region, accessKey, secretKey)
    }

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun basmAmazonS3(@Value("\${resources.endpoint.url:}") endpoint: String,
                    @Value("\${AWS_DEFAULT_REGION:eu-west-2}") region: String,
                    @Value("\${BASM_AWS_ACCESS_KEY_ID:}") accessKey: String,
                    @Value("\${BASM_AWS_SECRET_ACCESS_KEY:}") secretKey: String): AmazonS3 {

        return amazonS3(endpoint, region, accessKey, secretKey)
    }

    private fun amazonS3 (endpoint: String, region: String, accessKey: String, secretKey: String) : AmazonS3{
        logger.info("Using AWS configuration.")
        val builder = if(endpoint.isNotEmpty()) {
            // Localstack
            logger.info("Using localstack")

            AmazonS3ClientBuilder.standard().withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, region))
        }
        else {
            // Real AWS S3
            logger.info("Using S3")

            val awsCreds = BasicAWSCredentials(accessKey, secretKey)
            AmazonS3ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(AWSStaticCredentialsProvider(awsCreds)
                    )
        }

        return builder.withPathStyleAccessEnabled(true).build()
    }

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun locationsResourceProvider(@Qualifier("jpcAmazonS3") client: AmazonS3, @Value("\${JPC_BUCKET_NAME}") bucketName: String): Schedule34LocationsProvider {
        logger.info("Using AWS S3 provider for Schedule 34 locations.")

        return Schedule34LocationsProvider {
            logger.info("Getting locations using bucket $bucketName")
            client.getObject(GetObjectRequest(bucketName, it)).objectContent
        }
    }

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun sercoPricesResourceProvider(@Qualifier("jpcAmazonS3") client: AmazonS3, @Value("\${JPC_BUCKET_NAME}") bucketName: String): SercoPricesProvider {
        logger.info("Using AWS S3 provider for Serco prices.")
        return SercoPricesProvider {
            logger.info("getting serco")
            client.getObject(GetObjectRequest(bucketName, it)).objectContent
        }
    }

    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun geoameyPricesResourceProvider(@Qualifier("jpcAmazonS3") client: AmazonS3, @Value("\${JPC_BUCKET_NAME}") bucketName: String): GeoamyPricesProvider {
        logger.info("Using AWS S3 provider for Geoamey prices.")
        return GeoamyPricesProvider {
            logger.info("getting geo")
            client.getObject(GetObjectRequest(bucketName, it)).objectContent
        }
    }


    @Bean
    @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
    fun reportingResourceProvider(@Qualifier("basmAmazonS3") client: AmazonS3, @Value("\${BASM_BUCKET_NAME}") bucketName: String): ReportingProvider {
        logger.info("Using AWS S3 resource provider for reporting.")
        return ReportingProvider {
            client.getObjectAsString(bucketName, it)
        }
    }
}