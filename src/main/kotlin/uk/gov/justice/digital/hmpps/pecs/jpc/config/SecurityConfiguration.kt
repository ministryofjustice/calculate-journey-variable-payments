package uk.gov.justice.digital.hmpps.pecs.jpc.config

import com.nimbusds.oauth2.sdk.util.JSONArrayUtils
import net.minidev.json.JSONArray
import org.slf4j.LoggerFactory
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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.web.access.AccessDeniedHandler
import org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect
import kotlin.streams.toList

@EnableWebSecurity
@ConditionalOnWebApplication
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
  private lateinit var issuer: String

  @Value("\${HMPPS_AUTH_BASE_URI}")
  private lateinit var authLogoutSuccesUri: String

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http {
      authorizeRequests {
        authorize("/health/**", permitAll)
        authorize("/info", permitAll)
        authorize(anyRequest, hasRole("PECS_JPC"))
      }
      exceptionHandling {
        accessDeniedHandler = accessDeniedHandler()
        // TODO consider redirecting to access denied page?
      }
      oauth2Login {
        userInfoEndpoint { userService = oAuth2UserService() }
        defaultSuccessUrl("/", true)
      }
      logout {
        logoutSuccessUrl = authLogoutSuccesUri
      }
    }
  }

  @Bean
  fun oAuth2UserService(): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    // This is not an ideal solution and should be revisited.  Spring was not playing nicely with JWT.
    val delegate = DefaultOAuth2UserService()

    return OAuth2UserService { userRequest ->
      val user = delegate.loadUser(userRequest)
      val jwt = JwtDecoders.fromIssuerLocation(issuer).decode(userRequest.accessToken.tokenValue)
      val authorities = JSONArrayUtils.toStringList(jwt.claims["authorities"] as JSONArray?)

      DefaultOAuth2User(authorities.stream().map { SimpleGrantedAuthority(it) }.toList(), user.attributes, "name")
    }
  }

  private fun accessDeniedHandler(): AccessDeniedHandler {
    return AccessDeniedHandler { request, _, accessDeniedException ->
      val auth: Authentication? = SecurityContextHolder.getContext().authentication

      if (auth != null) {
        logger.warn("User: ${auth.name} attempted to access the protected URL: ${request.requestURI}")
      }

      logger.error(accessDeniedException.message)
    }
  }

  // Needed for Thymeleaf security extras.
  @Bean
  fun securityDialect(): SpringSecurityDialect? {
    return SpringSecurityDialect()
  }
}
