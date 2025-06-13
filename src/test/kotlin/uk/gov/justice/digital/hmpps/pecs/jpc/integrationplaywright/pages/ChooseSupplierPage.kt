package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

class ChooseSupplierPage(page: Page?) {

  private val url = "http://localhost:8080/choose-supplier"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
    page?.waitForLoadState()
  }

  fun gotToPage(supplier: Supplier) {
    page?.navigate("$url/${supplier.name.lowercase()}")
    page?.waitForLoadState()
  }

  fun isPageSuccessful() {
    val h1 = page?.locator("h1")
    assertThat(h1).hasText("Choose a supplier")
  }

  fun goToSercoDashboard() {
    page?.getByText("Serco")?.click()
  }

  fun goToGeoameyDashboard() {
    page?.getByText("GEOAmey")?.click()
  }
}
