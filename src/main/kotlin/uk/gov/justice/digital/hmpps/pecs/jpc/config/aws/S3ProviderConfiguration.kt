package uk.gov.justice.digital.hmpps.pecs.jpc.config.aws

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.ObfuscatingStreamingReportParser
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.StandardStreamingReportParser
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.StreamingReportParser
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.InputStream
import java.io.InputStreamReader

private val logger = loggerFor<S3ProviderConfiguration>()

/**
 * All AWS S3 resources for pulling in locations, prices and reports are configured in here.
 */
@Configuration
@ConditionalOnExpression("{'s3'}.contains('\${resources.provider}')")
class S3ProviderConfiguration {

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun jpcAmazonS3(
    @Value("\${resources.endpoint.url:}") endpoint: String,
    @Value("\${AWS_DEFAULT_REGION:eu-west-2}") region: String,
  ): AmazonS3 {
    return amazonS3(endpoint, region)
  }

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun basmAmazonS3(
    @Value("\${resources.endpoint.url:}") endpoint: String,
    @Value("\${AWS_DEFAULT_REGION:eu-west-2}") region: String,
  ): AmazonS3 {
    return amazonS3(endpoint, region)
  }

  private fun amazonS3(endpoint: String, region: String): AmazonS3 {
    logger.info("Using AWS configuration.")
    val builder = if (endpoint.isNotEmpty()) {
      // Localstack
      logger.info("****Using localstack with endpoint: $endpoint*****")

      AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, region))
    } else {
      // Real AWS S3
      logger.info("****Using Real S3 with IRSA****")

      AmazonS3ClientBuilder.standard()
        .withRegion(region)
        .withCredentials(
          WebIdentityTokenCredentialsProvider(),
        )
    }

    return builder.withPathStyleAccessEnabled(true).build()
  }

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun locationsResourceProvider(
    @Qualifier("jpcAmazonS3") client: AmazonS3,
    @Value("\${JPC_BUCKET_NAME}") bucketName: String,
    @Value("\${import-files.locations}") locationsFile: String,
  ): Schedule34LocationsProvider {
    logger.info("Using AWS S3 provider for Schedule 34 locations file: $locationsFile")

    return Schedule34LocationsProvider {
      logger.info("Getting locations using bucket $bucketName")
      client.getObject(GetObjectRequest(bucketName, locationsFile)).objectContent
    }
  }

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun sercoPricesResourceProvider(
    @Qualifier("jpcAmazonS3") client: AmazonS3,
    @Value("\${JPC_BUCKET_NAME}") bucketName: String,
    @Value("\${import-files.serco-prices}") sercoPricesFile: String,
  ): SercoPricesProvider {
    logger.info("Using AWS S3 provider for Serco prices file: $sercoPricesFile")
    return SercoPricesProvider {
      logger.info("getting SERCO")
      client.getObject(GetObjectRequest(bucketName, sercoPricesFile)).objectContent
    }
  }

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun geoameyPricesResourceProvider(
    @Qualifier("jpcAmazonS3") client: AmazonS3,
    @Value("\${JPC_BUCKET_NAME}") bucketName: String,
    @Value("\${import-files.geo-prices}") geoPricesFile: String,
  ): GeoameyPricesProvider {
    logger.info("Using AWS S3 provider for Geoamey prices file: $geoPricesFile")
    return GeoameyPricesProvider {
      logger.info("getting geo")
      client.getObject(GetObjectRequest(bucketName, geoPricesFile)).objectContent
    }
  }

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun reportingResourceProvider(
    @Qualifier("basmAmazonS3") client: AmazonS3,
    @Value("\${BASM_BUCKET_NAME}") bucketName: String,
  ): ReportingProvider {
    logger.info("Using AWS S3 resource provider for move.")
    return ReportingProvider {
      logger.debug("Using bucket $bucketName")
      client.getObjectAsString(bucketName, it)
    }
  }

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun reportLookup(
    @Qualifier("basmAmazonS3") client: AmazonS3,
    @Value("\${BASM_BUCKET_NAME}") bucketName: String,
  ): ReportLookup {
    return ReportLookup { client.doesObjectExist(bucketName, it) }
  }

  @Bean
  @ConditionalOnProperty(name = ["resources.provider"], havingValue = "s3")
  fun streamingReportParser(
    @Qualifier("basmAmazonS3") client: AmazonS3,
    @Value("\${BASM_BUCKET_NAME}") bucketName: String,
    @Value("\${SENTRY_ENVIRONMENT:}") env: String,
  ): StreamingReportParser {
    return if (env.trim().uppercase() == "LOCAL") {
      logger.warn("Running parser in PII obfuscation mode")
      ObfuscatingStreamingReportParser {
        InputStreamReader(
          client.getObject(
            GetObjectRequest(
              bucketName,
              it,
            ),
          ).objectContent,
        )
      }
    } else {
      logger.info("Using AWS S3 for streaming report parser.")
      StandardStreamingReportParser {
        InputStreamReader(
          client.getObject(
            GetObjectRequest(
              bucketName,
              it,
            ),
          ).objectContent,
        )
      }
    }
  }
}

/**
 * Responsible for providing the Geoamey prices Excel spreadsheet via an [InputStream].
 */
fun interface GeoameyPricesProvider {
  /**
   * The caller is responsible for closing the [InputStream].
   */
  fun get(): InputStream
}

/**
 * Responsible for providing the Serco prices Excel spreadsheet via an [InputStream].
 */
fun interface SercoPricesProvider {
  /**
   * The caller is responsible for closing the [InputStream].
   */
  fun get(): InputStream
}

fun interface ReportingProvider {
  fun get(resourceName: String): String
}

/**
 * Responsible for providing the Schedule 34 locations Excel spreadsheet via an [InputStream].
 */

fun interface Schedule34LocationsProvider {
  /**
   * The caller is responsible for closing the [InputStream].
   */
  fun get(): InputStream
}

/**
 * Responsible for determining whether a particular report exists. For example, we can check it exists prior to trying
 * to read/download the file.
 */
fun interface ReportLookup {
  fun doesReportExist(filename: String): Boolean
}
