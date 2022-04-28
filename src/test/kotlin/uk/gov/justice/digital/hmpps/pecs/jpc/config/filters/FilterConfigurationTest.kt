package uk.gov.justice.digital.hmpps.pecs.jpc.config.filters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.AnnualPriceAdjustmentsController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.JourneyPriceCatalogueController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MaintainSupplierPricingController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManageJourneyPriceCatalogueController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.ManageSchedule34LocationsController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.MapFriendlyLocationController
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.SummaryPageController

class FilterConfigurationTest {

  @Test
  fun `choose supplier URLs filtered`() {
    assertThat(FilterConfiguration().chooseFilter().urlPatterns).containsExactlyInAnyOrder(
      *SummaryPageController.routes(),
      *MaintainSupplierPricingController.routes(),
      *ManageSchedule34LocationsController.routes(),
      *MapFriendlyLocationController.routes(),
      *ManageJourneyPriceCatalogueController.routes(),
      *AnnualPriceAdjustmentsController.routes(),
      *JourneyPriceCatalogueController.routes()
    )
  }
}
