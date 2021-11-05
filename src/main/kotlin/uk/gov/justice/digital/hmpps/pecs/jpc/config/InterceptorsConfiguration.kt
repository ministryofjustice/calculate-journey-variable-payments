package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.pecs.jpc.interceptors.SessionCookieInterceptor
import uk.gov.justice.digital.hmpps.pecs.jpc.interceptors.SessionCookieProperties

@Configuration
class InterceptorsConfiguration : WebMvcConfigurer {

  @Autowired
  private lateinit var sessionCookieProperties: SessionCookieProperties

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(SessionCookieInterceptor(sessionCookieProperties))
  }
}
