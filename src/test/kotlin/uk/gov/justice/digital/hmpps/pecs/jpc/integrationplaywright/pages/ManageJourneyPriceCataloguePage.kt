package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page

class ManageJourneyPriceCataloguePage(page: Page?) {

  private val url = "http://localhost:8080/manage-journey-price-catalogue"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
  }

  fun isPageSuccessful(): Boolean {
    return page?.waitForSelector("h1")?.innerText()?.startsWith("Manage Journey Price Catalogue") == true
  }
  fun goToSearchJourneys() {
    page?.getByText("Find journeys")?.click()
  }
  fun goToAnnualPriceAdjustment() {
    page?.getByText("Apply bulk price adjustment")?.click()
  }
}
