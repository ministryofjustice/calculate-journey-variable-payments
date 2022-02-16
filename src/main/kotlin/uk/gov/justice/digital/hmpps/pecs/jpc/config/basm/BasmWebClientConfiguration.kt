package uk.gov.justice.digital.hmpps.pecs.jpc.config.basm

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

/**
 * Web client configuration for communication with the Book a Secure Move API e.g. automatic mapping NOMIS locations.
 */
private val logger = loggerFor<BasmWebClientConfiguration>()

@Configuration
class BasmWebClientConfiguration(@Value("\${BASM_API_BASE_URL}") val baseUri: String) {

  @Bean
  fun basmWebClient(): WebClient? {
    logger.info("****Using BaSM web client with base URI $baseUri****")

    return WebClient.builder().baseUrl(baseUri).build()
  }
}
