package uk.gov.justice.digital.hmpps.pecs.jpc.config.interceptors

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class InterceptorsConfiguration : WebMvcConfigurer {

  @Autowired
  private lateinit var sessionCookieProperties: SessionCookieProperties

  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(SessionCookieInterceptor(sessionCookieProperties))
  }
}
