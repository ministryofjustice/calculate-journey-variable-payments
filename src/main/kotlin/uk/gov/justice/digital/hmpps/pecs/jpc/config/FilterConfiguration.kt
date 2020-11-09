package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.filter.ChooseSupplierFilter

@Configuration
class FilterRegistrationConfig {
    @Bean
    fun chooseFilter(): FilterRegistrationBean<ChooseSupplierFilter> {
        val registrationBean: FilterRegistrationBean<ChooseSupplierFilter> = FilterRegistrationBean<ChooseSupplierFilter>()
        registrationBean.filter = ChooseSupplierFilter()
        registrationBean.addUrlPatterns("/dashboard", "/select-month")
        return registrationBean
    }
}
