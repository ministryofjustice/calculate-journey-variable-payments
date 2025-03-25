package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

class AnnualPriceAdjustmentPage(page: Page?) {

  private val logger = loggerFor<AnnualPriceAdjustmentPage>()
  private val url = "http://localhost:8080/annual-price-adjustment"
  private val page = page

  fun isPageSuccessful(): Boolean {
    page?.waitForURL(url)
    return page?.waitForSelector("h1")?.innerText()?.startsWith("Manage Journey Price Catalogue") == true
  }

  fun applyBulkPriceAdjustment(inflationaryRate: String, volumetricRate: String?, details: String): Boolean {
    assert(this.isPageSuccessful())
    page?.getByRole(AriaRole.TEXTBOX)?.first()?.getAttribute("name")
    page?.locator("input#inflationary-rate")?.fill(inflationaryRate)
    if (volumetricRate != null) {
      page?.locator("input#volumetric-rate")?.fill(volumetricRate)
    }
    page?.locator("textarea#details")?.fill(details)
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

  fun showApplyBulkPriceAdjustmentTab() {
    page?.navigate("$url#annual-price-adjustment")
  }

  fun showPriceAdjustmentHistoryTab() {
    page?.navigate("$url#price-adjustment-history")
    page?.waitForURL("$url#price-adjustment-history")
  }

  fun isPriceAdjustmentRecordsPresent(message: String): Boolean {
    val rows = page?.locator("//tr[td[contains(text(), '$message')]]")

    when (rows?.count()) {
      0 -> {
        logger.info("Record not found")
        false
      }

      1 -> {
        logger.info("Scenario when only Inflationary record is added")
        val rateText = rows.nth(0).locator("td:nth-child(3)").innerText()
        logger.info(rateText)
        return rateText.contains("Inflationary price adjustment by rate of")
      }

      2 -> {
        logger.info(" Scenario when both Inflationary and Volumetric records is added")
        val inflationaryRateText = rows.nth(1).locator("td:nth-child(3)").innerText()
        val volumetricRateText = rows.nth(0).locator("td:nth-child(3)").innerText()
        logger.info(inflationaryRateText)
        logger.info(volumetricRateText)
        return inflationaryRateText.contains("Inflationary price adjustment by rate of") &&
          volumetricRateText.contains("Volumetric price adjustment by rate")
      }

      else -> {
        logger.info("To many records created")
        false
      }
    }
    return false
  }
}
