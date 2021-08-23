package uk.gov.justice.digital.hmpps.pecs.jpc.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MaintainSupplierPricingController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManagePriceCatalogueController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManageSchedule34LocationsController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MapFriendlyLocationController
import uk.gov.justice.digital.hmpps.pecs.jpc.filter.ChooseSupplierFilter

@Configuration
class FilterConfiguration {
  @Bean
  fun chooseFilter(): FilterRegistrationBean<ChooseSupplierFilter> =
    FilterRegistrationBean<ChooseSupplierFilter>().apply {
      this.filter = ChooseSupplierFilter()
      this.addUrlPatterns(
        *HtmlController.routes(),
        *MaintainSupplierPricingController.routes(),
        *ManageSchedule34LocationsController.routes(),
        *MapFriendlyLocationController.routes(),
        *ManagePriceCatalogueController.routes()
      )
    }
}
