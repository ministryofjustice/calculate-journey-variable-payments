package uk.gov.justice.digital.hmpps.pecs.jpc.config.filters

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.AnnualPriceAdjustmentsController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.HtmlController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MaintainSupplierPricingController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManageJourneyPriceCatalogueController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManageSchedule34LocationsController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MapFriendlyLocationController

@Configuration
class FilterConfiguration {

  /**
   * All requests will be filtered through here with the configured URL patterns to ensure a supplier has been chosen
   * for current flow in the service.
   */
  @Bean
  fun chooseFilter(): FilterRegistrationBean<ChooseSupplierFilter> =
    FilterRegistrationBean<ChooseSupplierFilter>().apply {
      this.filter = ChooseSupplierFilter()
      this.addUrlPatterns(
        *HtmlController.routes(),
        *MaintainSupplierPricingController.routes(),
        *ManageSchedule34LocationsController.routes(),
        *MapFriendlyLocationController.routes(),
        *ManageJourneyPriceCatalogueController.routes(),
        *AnnualPriceAdjustmentsController.routes()
      )
    }
}