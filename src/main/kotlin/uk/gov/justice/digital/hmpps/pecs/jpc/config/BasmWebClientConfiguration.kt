package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Web client configuration for the Book a Secure Move API.
 */
@Configuration
class BasmWebClientConfiguration(@Value("\${BASM_API_BASE_URL}") val baseUri: String) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Bean
  fun basmWebClient(): WebClient? {
    logger.info("****Using BaSM web client with base URI $baseUri****")

    return WebClient.builder().baseUrl(baseUri).build()
  }
}
