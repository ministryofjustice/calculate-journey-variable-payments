package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

/**
 * Web client configuration for the Book a Secure Move API.
 */
@Configuration
@ConditionalOnWebApplication
class BasmWebClientConfiguration(@Value("\${BASM_API_BASE_URL}") val baseUri: String) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Autowired
  private lateinit var clientRegistrationRepository: ClientRegistrationRepository

  @Autowired
  private lateinit var oAuth2AuthorizedClientService: OAuth2AuthorizedClientService

  @Bean
  @ConditionalOnExpression("'\${BASM_API_BASE_URL}'.contains('localhost')")
  fun stubBasmWebClient(): WebClient? {
    logger.info("****Using stubbed BaSM web client with endpoint $baseUri****")

    return WebClient.builder().baseUrl(baseUri).build()
  }

  @Bean
  @ConditionalOnExpression("!'\${BASM_API_BASE_URL}'.contains('localhost')")
  fun basmWebClient(authorizedClientManager: OAuth2AuthorizedClientManager?): WebClient? {
    logger.info("****Using real BaSM web client with endpoint $baseUri****")

    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("basm")

    val exchangeStrategies = ExchangeStrategies.builder()
      .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
      .build()

    return WebClient.builder()
      .baseUrl(baseUri)
      .apply(oauth2Client.oauth2Configuration())
      .exchangeStrategies(exchangeStrategies)
      .build()
  }

  @Bean
  fun authorizedClientManager(): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}
