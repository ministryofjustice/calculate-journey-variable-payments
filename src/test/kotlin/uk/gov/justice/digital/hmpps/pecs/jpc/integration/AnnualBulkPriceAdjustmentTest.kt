package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.AnnualPriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue

internal class AnnualBulkPriceAdjustmentTest : IntegrationTest() {

  @Test
  fun `apply annual price adjustment`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).applyAdjustment(1.123456789012345, "Indexation rate of 1.123456789012345.")

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).showPriceAdjustmentHistoryTab().isPriceHistoryRowPresent(1.123456789012345, "Indexation rate of 1.123456789012345.")
  }
}
