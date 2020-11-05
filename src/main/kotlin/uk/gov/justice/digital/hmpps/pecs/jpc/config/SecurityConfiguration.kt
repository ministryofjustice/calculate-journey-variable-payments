package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.servlet.invoke

// TODO for now this is tied to run for localstack profile only!!
@EnableWebSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http {
            authorizeRequests {
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
