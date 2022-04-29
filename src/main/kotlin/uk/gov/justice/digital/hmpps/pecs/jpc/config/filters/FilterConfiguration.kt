package uk.gov.justice.digital.hmpps.pecs.jpc.config.filters

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FilterConfiguration {

  /**
   * All requests will be filtered through here to ensure a supplier has been chosen for current flow in the service.
   */
  @Bean
  fun chooseFilter(): FilterRegistrationBean<ChooseSupplierFilter> =
    FilterRegistrationBean<ChooseSupplierFilter>().apply {
      this.filter = ChooseSupplierFilter()
    }
}
