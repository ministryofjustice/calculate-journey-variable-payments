package uk.gov.justice.digital.hmpps.pecs.jpc.config.interceptors

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "server.servlet.session.cookie")
data class SessionCookieProperties(val maxAge: Duration, val httpOnly: Boolean, val secure: Boolean)
