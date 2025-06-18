package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.Assert.fail
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

class AnnualPriceAdjustmentPage(page: Page?) {

  private val logger = loggerFor<AnnualPriceAdjustmentPage>()
  private val url = "http://localhost:8080/annual-price-adjustment"
  private val page = page

  fun isPageSuccessful() {
    page?.waitForURL(url)
    val h1 = page?.locator("h1:has-text(\"Manage Journey Price Catalogue\")")
    assertThat(h1).containsText("Manage Journey Price Catalogue")
  }

  fun applyBulkPriceAdjustment(inflationaryRate: String, volumetricRate: String?, details: String): Boolean {
    this.isPageSuccessful()
    page?.getByRole(AriaRole.TEXTBOX)?.first()?.getAttribute("name")
    page?.locator("input#inflationary-rate")?.fill(inflationaryRate)
    if (volumetricRate != null) {
      page?.locator("input#volumetric-rate")?.fill(volumetricRate)
    }
    page?.locator("textarea#details")?.fill(details)
    page?.locator("textarea#details")?.blur()
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Apply adjustment"))?.click()
    page?.waitForLoadState()
    val formSubmitted = !page?.url().equals(url)
    if (!formSubmitted) {
      page?.locator("p.govuk-error-message")?.all()?.forEach { element ->
        logger.info("Validation Error: $element")
      }
    }
    return formSubmitted
  }

  fun showPriceAdjustmentHistoryTab() {
    page?.navigate(url)
    page?.waitForLoadState()
    page?.locator("a#tab_price-adjustment-history")?.click()
  }

  fun isPriceAdjustmentRecordsPresent(message: String) {
    page?.waitForLoadState()
    val rows = page?.locator("//tr[td[contains(text(), '$message')]]")
    val rowCount = rows?.count() ?: 0

    assertThat(rows).hasCount(rowCount)
    if (rowCount !in 1..2) {
      fail("Expected 1 or 2 rows but found $rowCount rows for message: $message")
    }

    when (rowCount) {
      1 -> {
        logger.info("Scenario when only Inflationary record is added: $message")
        val rateCell = rows?.nth(0)?.locator("td:nth-child(3)")
        logger.info(rateCell?.innerText())
        assertThat(rateCell).containsText("Inflationary price adjustment by rate of")
      }

      2 -> {
        logger.info("Scenario when both Inflationary and Volumetric records is added: $message")
        val inflationaryRateCell = rows?.nth(1)?.locator("td:nth-child(3)")
        val volumetricRateCell = rows?.nth(0)?.locator("td:nth-child(3)")

        logger.info(inflationaryRateCell?.innerText())
        logger.info(volumetricRateCell?.innerText())

        assertThat(inflationaryRateCell).containsText("Inflationary price adjustment by rate of")
        assertThat(volumetricRateCell).containsText("Volumetric price adjustment by rate")
      }
    }
  }
}
