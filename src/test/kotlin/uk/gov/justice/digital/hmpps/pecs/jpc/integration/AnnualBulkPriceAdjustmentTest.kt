package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.support.ui.FluentWait
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.AnnualPriceAdjustment
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.io.File
import java.lang.Error
import java.time.Duration
import java.time.LocalDate

internal class AnnualBulkPriceAdjustmentTest : IntegrationTest() {

  private val logger = loggerFor<AnnualBulkPriceAdjustmentTest>()
  private val volumetric = "Volumetric"
  private val inflationary = "Inflationary"

  // This will break in September, fix test to get current CY or otherwise fix up time providers in tests.
  private val year = effectiveYearForDate(LocalDate.now())

  @Test
  fun `apply annual price adjustments`() {
    try {
      logger.info("Logging in and going to Dashboard")
      loginAndGotoDashboardFor(Supplier.SERCO)

      logger.info("Doing inflationary Adjustment")
      doInflationaryAdjustment()

      logger.info("Going back to Dashboard")
      goToPage(Dashboard)

      logger.info("Doing Volumetric adjustment")
      doVolumetricAdjustment()
    } catch (e: Error) {
      val scrFile: File = (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
      FileUtils.copyFile(
        scrFile,
        File(imageLocation + "apply-annual-price-adjustments.jpg"),
      )
      throw e
    }
  }

  private fun doInflationaryAdjustment() {
    logger.info("Navigating to Manage Journey Price")
    isAtPage(Dashboard).navigateToManageJourneyPrice()

    logger.info("Navigating to Apply Bulk Price Adjustment")
    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    // This is currently less than 1 to be determined as inflationary
    val rate = 0.123456789012345
    logger.info("Applying Inflationary rate of $rate")
    isAtPage(AnnualPriceAdjustment).applyAdjustment(rate, "Inflationary rate of $rate.")

    waitThreeSeconds()

    logger.info("Navigating to Apply Bulk Price Adjustment")
    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    logger.info("Checking for price history row on Price history tab")
    isAtPage(AnnualPriceAdjustment).showPriceAdjustmentHistoryTab()
      .isPriceHistoryRowPresent(inflationary, rate, year, "Inflationary rate of $rate.")
  }

  private fun doVolumetricAdjustment() {
    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).applyAdjustments(0.5, 1.0, "Inflationary and volumetric rates 0.5 and 1.0.")

    waitThreeSeconds()

    isAtPage(ManageJourneyPriceCatalogue).navigateToApplyBulkPriceAdjustment()

    isAtPage(AnnualPriceAdjustment).showPriceAdjustmentHistoryTab()
      .isPriceHistoryRowPresent(inflationary, 0.5, year, "Inflationary and volumetric rates 0.5 and 1.0.")
      .isPriceHistoryRowPresent(volumetric, 1.0, year, "Inflationary and volumetric rates 0.5 and 1.0.")
  }

  private fun waitThreeSeconds() {
    var timer = 0
    FluentWait(this).withTimeout(Duration.ofSeconds(6)).pollingEvery(Duration.ofSeconds(1)).until {
      timer += 1
      timer == 3
    }
  }
}
