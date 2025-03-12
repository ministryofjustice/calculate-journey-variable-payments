package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

class ChooseSupplierPage(page: Page?) {

  private val url = "http://localhost:8080/choose-supplier"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
  }

  fun gotToPage(supplier: Supplier) {
    page?.navigate("$url/${supplier.name.lowercase()}")
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
