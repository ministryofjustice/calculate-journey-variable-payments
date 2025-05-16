package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

class ManageJourneyPriceCataloguePage(page: Page?) {

  private val url = "http://localhost:8080/manage-journey-price-catalogue"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
  }

  fun isPageSuccessful() {
    val h1 = page?.locator("h1")
    assertThat(h1).containsText("Manage Journey Price Catalogue")
  }
  fun goToFindJourneys() {
    page?.getByText("Find journeys")?.click()
    page?.waitForLoadState()
  }
  fun goToAnnualPriceAdjustment() {
    page?.getByText("Apply bulk price adjustment")?.click()
    page?.waitForLoadState()
  }
}
