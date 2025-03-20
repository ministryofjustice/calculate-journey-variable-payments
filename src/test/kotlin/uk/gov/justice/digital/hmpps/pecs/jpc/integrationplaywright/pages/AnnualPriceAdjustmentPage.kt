package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole

class AnnualPriceAdjustmentPage(page: Page?) {

  private val url = "http://localhost:8080/annual-price-adjustment"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
  }

  fun isPageSuccessful(): Boolean {
    return page?.waitForSelector("h1")?.innerText()?.startsWith("Manage Journey Price Catalogue") == true
  }

  fun applyBulkPriceAdjustment(inflationaryRate: Double?, volumetricRate: Double?, details: String) {
    if (inflationaryRate != null) {
      page?.getByLabel("inflationary-rate")?.fill(inflationaryRate.toString())
    }
    if (volumetricRate != null) {
      page?.getByLabel("volumetric-rate")?.fill(volumetricRate.toString())
    }
    page?.getByLabel("details")?.fill(details)
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Apply adjustment"))?.click()
  }

  fun showApplyBulkPriceAdjustmentTab() {
    page?.navigate("$url#annual-price-adjustment")
  }

  fun showPriceAdjustmentHistoryTab() {
    page?.navigate("$url#price-adjustment-history")
  }

  fun isPriceAdjustmentRecordPresent(message: String): Boolean {
    return page?.waitForSelector("td")?.innerText()?.equals(message) == true
  }
}
