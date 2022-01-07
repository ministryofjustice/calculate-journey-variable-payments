package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.AnnualPriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue

internal class AnnualBulkPriceAdjustmentTest : IntegrationTest() {

  @Test
  fun `apply annual price adjustments`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    doInflationaryAdjustment()

    goToPage(Dashboard)

    doVolumetricAdjustment()
  }

  private fun doInflationaryAdjustment() {
    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).applyAdjustment(1.123456789012345, "Inflationary rate of 1.123456789012345.")

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).showPriceAdjustmentHistoryTab().isPriceHistoryRowPresent(1.123456789012345, "Inflationary rate of 1.123456789012345.")
  }

  private fun doVolumetricAdjustment() {
    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).applyAdjustments(1.5, 1.0, "Inflationary and volumetric rates 1.5 and 1.0.")

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).showPriceAdjustmentHistoryTab()
      .isPriceHistoryRowPresent(1.5, "Inflationary and volumetric rates 1.5 and 1.0.")
      .isPriceHistoryRowPresent(1.0, "Inflationary and volumetric rates 1.5 and 1.0.")
  }
}
