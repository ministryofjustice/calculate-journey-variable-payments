package uk.gov.justice.digital.hmpps.pecs.jpc.interceptors

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties(prefix = "server.servlet.session.cookie")
data class SessionCookieProperties(val maxAge: Duration, val httpOnly: Boolean, val secure: Boolean)
