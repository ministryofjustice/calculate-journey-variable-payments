package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page

class ChooseSupplierPage(page: Page?) {

  private val url = "http://localhost:8080/choose-supplier"
  private val page = page
  fun gotToPage() {
    page?.navigate(url)
  }

  fun isPageSuccessful(): Boolean {
    return page?.waitForSelector("h1")?.innerText().equals("Choose a supplier")
  }
  fun goToSercoDashboard() {
    page?.getByText("Serco")?.click()
  }
  fun goToGeoameyDashboard() {
    page?.getByText("GEOAmey")?.click()
  }
}
