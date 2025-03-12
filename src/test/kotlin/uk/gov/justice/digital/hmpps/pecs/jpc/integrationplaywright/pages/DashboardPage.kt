package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

class DashboardPage(page: Page?) {

  private val chooseSupplierUrl = "http://localhost:8080/choose-supplier"
  private val dashboardUrl = "http://localhost:8080/dashboard"
  private val page = page

  fun gotToPage(supplier: Supplier) {
    page?.navigate("$chooseSupplierUrl/${supplier.name.lowercase()}")
  }

  fun isPageSuccessful(supplier: Supplier): Boolean {
    page?.waitForURL(dashboardUrl)
    return page?.url().equals(dashboardUrl) && page?.getByText(supplier.name)?.first()?.isVisible == true
  }
}
