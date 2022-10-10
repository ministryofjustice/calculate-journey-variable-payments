package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import org.openqa.selenium.support.ui.FluentWait
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.AnnualPriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration

internal class AnnualBulkPriceAdjustmentTest : IntegrationTest() {

  private val logger = loggerFor <AnnualBulkPriceAdjustmentTest>()
  @Test
  fun `apply annual price adjustments`() {

    logger.info("Logging in and going to Dashboard")
    loginAndGotoDashboardFor(Supplier.SERCO)

    logger.info("Doing inflationary Adjustment")
    doInflationaryAdjustment()

    logger.info("Going back to Dashboard")
    goToPage(Dashboard)

    logger.info("Doing Volumetric adjustment")
    doVolumetricAdjustment()
  }

  private fun doInflationaryAdjustment() {
    logger.info("Navigating to Manage Journey Price")
    isAtPage(Dashboard).navigateToManageJourneyPrice()

    logger.info("Navigating to Apply Bulk Price Adjustment")
    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    val rate = 1.123456789012345
    logger.info("Applying Inflationary rate of $rate")
    isAtPage(AnnualPriceAdjustment).applyAdjustment(rate, "Inflationary rate of $rate.")

    waitFiveSeconds()

    logger.info("Navigating to Apply Bulk Price Adjustment")
    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    logger.info("Checking for price history row on Price history tab")
    isAtPage(AnnualPriceAdjustment).showPriceAdjustmentHistoryTab().isPriceHistoryRowPresent(rate, "Inflationary rate of $rate.")
  }

  private fun doVolumetricAdjustment() {
    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).applyAdjustments(1.5, 1.0, "Inflationary and volumetric rates 1.5 and 1.0.")

    waitFiveSeconds()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).showPriceAdjustmentHistoryTab()
      .isPriceHistoryRowPresent(1.5, "Inflationary and volumetric rates 1.5 and 1.0.")
      .isPriceHistoryRowPresent(1.0, "Inflationary and volumetric rates 1.5 and 1.0.")
  }

  private fun waitFiveSeconds() {
    var timer = 0
    FluentWait(this).withTimeout(Duration.ofSeconds(6)).pollingEvery(Duration.ofSeconds(1)).until {
      timer += 1
      timer == 5
    }
  }
}
