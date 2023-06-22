package uk.gov.justice.digital.hmpps.pecs.jpc.config.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration<S : Session> {

  @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private lateinit var issuer: String

  @Value("\${HMPPS_AUTH_BASE_URI}")
  private lateinit var authLogoutSuccessUri: String

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private lateinit var sessionRepository: FindByIndexNameSessionRepository<S>

  @Autowired
  private lateinit var auditService: AuditService

  @Bean
  @Throws(Exception::class)
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      authorizeRequests {
        authorize("/health/**", permitAll)
        authorize("/info", permitAll)
        authorize(anyRequest, hasRole("PECS_JPC"))
      }
      sessionManagement {
        invalidSessionUrl = ssoLogoutUri()
        sessionAuthenticationErrorUrl = ssoLogoutUri()
        sessionConcurrency {
          sessionRegistry = clusteredConcurrentSessionRegistry()
          maximumSessions = 1
        }
      }
      exceptionHandling {
        accessDeniedHandler = accessDeniedHandler()
      }
      oauth2Login {
        userInfoEndpoint { userService = oAuth2UserService() }
        failureUrl = ssoLogoutUri()
        authenticationSuccessHandler = logInHandler()
      }
      logout {
        logoutSuccessUrl = ssoLogoutUri()
        logoutSuccessHandler = logOutHandler()
      }
    }

    return http.build()
  }

  @Bean
  fun logInHandler() = LogInAuditHandler(auditService)

  @Bean
  fun logOutHandler() = LogOutAuditHandler(auditService, ssoLogoutUri())

  @Bean
  fun clusteredConcurrentSessionRegistry(): SpringSessionBackedSessionRegistry<S> =
    SpringSessionBackedSessionRegistry(sessionRepository)

  @Bean
  fun oAuth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    val delegate = DefaultOAuth2UserService()

    return OAuth2UserService { userRequest ->
      val user = delegate.loadUser(userRequest)
      val jwt = (JwtDecoders.fromIssuerLocation(issuer) as JwtDecoder).decode(userRequest.accessToken.tokenValue)

      DefaultOAuth2User(
        jwt.getClaimAsStringList("authorities").stream().map { SimpleGrantedAuthority(it) }.toList(),
        user.attributes,
        "name",
      )
    }
  }

  private fun accessDeniedHandler(): AccessDeniedHandler {
    return AccessDeniedHandler { request, response, accessDeniedException ->
      val auth: Authentication? = SecurityContextHolder.getContext().authentication

      auth?.let { logger.warn("User: ${auth.name} attempted to access the protected URL: ${request.requestURI}") }

      logger.error(accessDeniedException.message)

      request.session.invalidate()

      response.sendRedirect(ssoLogoutUri())
    }
  }

  private fun ssoLogoutUri() = authLogoutSuccessUri.plus("/sign-out")

  @Bean
  fun securityDialectForThymeleafSecurityExtras(): SpringSecurityDialect? {
    return SpringSecurityDialect()
  }
}
