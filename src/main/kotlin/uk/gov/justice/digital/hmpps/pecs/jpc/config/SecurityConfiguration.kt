package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke

@EnableWebSecurity
@ConditionalOnWebApplication
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http {
            authorizeRequests {
                authorize("/health/**", permitAll)
                authorize("/info", permitAll)
                authorize(anyRequest, authenticated)
            }
            exceptionHandling {
            }
            oauth2Login {
                defaultSuccessUrl("/", true)
            }
        }
    }
}
