package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.AnnualPriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue

internal class AnnualBulkPriceAdjustmentTest : IntegrationTest() {

  @Test
  fun `apply annual price adjustment`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.GEOAMEY)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).applyAdjustment(1.6, "Indexation rate of 1.6.")

    isAtPage(ManageJourneyPriceCatalogue)
  }
}
