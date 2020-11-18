package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.DASHBOARD_URL
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.JOURNEYS_URL
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.MOVES_BY_TYPE_URL
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController.Companion.SELECT_MONTH_URL
import uk.gov.justice.digital.hmpps.pecs.jpc.filter.ChooseSupplierFilter

@Configuration
class FilterRegistrationConfig {

    @Autowired lateinit var timeSource: TimeSource

    @Bean
    fun chooseFilter(): FilterRegistrationBean<ChooseSupplierFilter> {
        val registrationBean: FilterRegistrationBean<ChooseSupplierFilter> = FilterRegistrationBean<ChooseSupplierFilter>()
        registrationBean.filter = ChooseSupplierFilter(timeSource)
        registrationBean.addUrlPatterns(DASHBOARD_URL, SELECT_MONTH_URL, JOURNEYS_URL, MOVES_BY_TYPE_URL)
        return registrationBean
    }
}
