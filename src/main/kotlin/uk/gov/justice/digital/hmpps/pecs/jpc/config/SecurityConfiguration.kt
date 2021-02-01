package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.LogInAuditFilter
import uk.gov.justice.digital.hmpps.pecs.jpc.auditing.LogOutAuditHandler
import uk.gov.justice.digital.hmpps.pecs.jpc.service.AuditService
import kotlin.streams.toList

@EnableWebSecurity
@ConditionalOnWebApplication
class SecurityConfiguration<S : Session> : WebSecurityConfigurerAdapter() {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private lateinit var issuer: String

  @Value("\${HMPPS_AUTH_BASE_URI}")
  private lateinit var authLogoutSuccessUri: String

  @Autowired
  private lateinit var sessionRepository: FindByIndexNameSessionRepository<S>

  @Autowired
  private lateinit var timeSource: TimeSource

  @Autowired
  private lateinit var auditService: AuditService

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http {
      authorizeRequests {
        authorize("/health/**", permitAll)
        authorize("/info", permitAll)
        authorize(anyRequest, hasRole("PECS_JPC"))
      }
      sessionManagement {
        invalidSessionUrl = authLogoutSuccessUri
        sessionAuthenticationErrorUrl = authLogoutSuccessUri
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
        defaultSuccessUrl("/", true)
        failureUrl = authLogoutSuccessUri
      }
      logout {
        logoutSuccessUrl = authLogoutSuccessUri
        addLogoutHandler(LogOutAuditHandler(auditService))
      }
      addFilterAfter<OAuth2LoginAuthenticationFilter>(LogInAuditFilter(timeSource, auditService))
    }
  }

  @Bean
  fun clusteredConcurrentSessionRegistry(): SpringSessionBackedSessionRegistry<S> =
    SpringSessionBackedSessionRegistry(sessionRepository)

  @Bean
  fun oAuth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    val delegate = DefaultOAuth2UserService()

    return OAuth2UserService { userRequest ->
      val user = delegate.loadUser(userRequest)
      val jwt = JwtDecoders.fromIssuerLocation(issuer).decode(userRequest.accessToken.tokenValue)

      DefaultOAuth2User(
        jwt.getClaimAsStringList("authorities").stream().map { SimpleGrantedAuthority(it) }.toList(),
        user.attributes,
        "name"
      )
    }
  }

  private fun accessDeniedHandler(): AccessDeniedHandler {
    return AccessDeniedHandler { request, response, accessDeniedException ->
      val auth: Authentication? = SecurityContextHolder.getContext().authentication

      auth?.let { logger.warn("User: ${auth.name} attempted to access the protected URL: ${request.requestURI}") }

      logger.error(accessDeniedException.message)

      request.session.invalidate()

      response.sendRedirect(authLogoutSuccessUri)
    }
  }

  // Needed for Thymeleaf security extras.
  @Bean
  fun securityDialect(): SpringSecurityDialect? {
    return SpringSecurityDialect()
  }
}
