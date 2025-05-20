package uk.gov.justice.digital.hmpps.pecs.jpc.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.session.Session
import org.springframework.session.config.SessionRepositoryCustomizer
import org.springframework.session.jdbc.JdbcIndexedSessionRepository
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.LogInAuditHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.LogOutAuditHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<SecurityConfiguration<*>>()

/**
 * All OAuth2 and user session management is configured here.
 */
@EnableWebSecurity
@ConditionalOnWebApplication
@EnableMethodSecurity
@Configuration
class SecurityConfiguration<S : Session> {

  @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private lateinit var issuer: String

  @Value("\${HMPPS_AUTH_BASE_URI}")
  private lateinit var authLogoutSuccessUri: String

  @Autowired
  private lateinit var auditService: AuditService

  @Bean
  fun jdbcHttpSessionCustomizer(): PostgreSqlJdbcHttpSessionCustomizer = PostgreSqlJdbcHttpSessionCustomizer()

  class PostgreSqlJdbcHttpSessionCustomizer : SessionRepositoryCustomizer<JdbcIndexedSessionRepository> {
    override fun customize(sessionRepository: JdbcIndexedSessionRepository) {
      sessionRepository.setCreateSessionAttributeQuery(CREATE_SESSION_ATTRIBUTE_QUERY)
    }

    companion object {
      private val CREATE_SESSION_ATTRIBUTE_QUERY = """
        INSERT INTO SPRING_SESSION_ATTRIBUTES (SESSION_PRIMARY_ID, ATTRIBUTE_NAME, ATTRIBUTE_BYTES)
        VALUES (?, ?, ?)
        ON CONFLICT (SESSION_PRIMARY_ID, ATTRIBUTE_NAME)
        DO UPDATE SET ATTRIBUTE_BYTES = EXCLUDED.ATTRIBUTE_BYTES
        
      """.trimIndent()
    }
  }

  @Bean
  @Throws(Exception::class)
  fun filterChain(http: HttpSecurity): SecurityFilterChain? = http
    .authorizeHttpRequests { auth ->
      auth
        .requestMatchers("/health/**", "/info").permitAll()
        .anyRequest().hasRole("PECS_JPC")
    }
    .sessionManagement {
      it.invalidSessionUrl(ssoLogoutUri())
        .sessionAuthenticationErrorUrl(ssoLogoutUri())
        .sessionConcurrency { concurrency ->
          concurrency.maximumSessions(1)
        }
    }
    .exceptionHandling {
      it.accessDeniedHandler(accessDeniedHandler())
    }
    .oauth2Login {
      it.userInfoEndpoint { oAuth2UserService() }.failureUrl(ssoLogoutUri()).successHandler(logInHandler())
    }
    .logout {
      it.logoutSuccessHandler(logOutHandler())
        .logoutSuccessUrl(ssoLogoutUri())
    }
    .build()

  @Bean
  fun logInHandler() = LogInAuditHandler(auditService)

  @Bean
  fun logOutHandler() = LogOutAuditHandler(auditService, ssoLogoutUri())

  @Bean
  fun oAuth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> = OAuth2UserService { userRequest ->

    val jwt = (JwtDecoders.fromIssuerLocation(issuer) as JwtDecoder).decode(userRequest.accessToken.tokenValue)
    val userAttributes = jwt.claims
    DefaultOAuth2User(
      jwt.getClaimAsStringList("authorities").stream().map { SimpleGrantedAuthority(it) }.toList(),
      userAttributes,
      "name",
    )
  }

  private fun accessDeniedHandler(): AccessDeniedHandler = AccessDeniedHandler { request, response, accessDeniedException ->
    val auth: Authentication? = SecurityContextHolder.getContext().authentication

    auth?.let { logger.warn("User: ${auth.name} attempted to access the protected URL: ${request.requestURI}") }

    logger.error(accessDeniedException.message)

    request.session.invalidate()

    response.sendRedirect(ssoLogoutUri())
  }

  private fun ssoLogoutUri() = authLogoutSuccessUri.plus("/auth/sign-out")

  @Bean
  fun securityDialectForThymeleafSecurityExtras(): SpringSecurityDialect? = SpringSecurityDialect()
}
