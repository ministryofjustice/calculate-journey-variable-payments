package uk.gov.justice.digital.hmpps.pecs.jpc.controller

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.function.client.WebClient
import java.util.*
import kotlin.jvm.Throws


@Controller
class HtmlController: WebSecurityConfigurerAdapter() {

    @RequestMapping("/")
    fun test(model: ModelMap): String {
        model["title"] = "Calculate Journey Variable Payments"
        return "index"
    }

    @GetMapping("/user")
    fun user(@AuthenticationPrincipal principal: OAuth2User): Map<String?, Any?>? {
        return Collections.singletonMap("name", principal.getAttribute("name"))
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login()
    }

    @Bean
    fun webClient(clientRegistrationRepository: ClientRegistrationRepository?,
                  authorizedClientRepository: OAuth2AuthorizedClientRepository?): WebClient? {
        val oauth2 = ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                authorizedClientRepository)
        oauth2.setDefaultOAuth2AuthorizedClient(true)
        return WebClient.builder().apply(oauth2.oauth2Configuration()).build()
    }

}