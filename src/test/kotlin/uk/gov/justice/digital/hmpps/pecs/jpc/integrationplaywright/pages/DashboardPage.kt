package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

class DashboardPage(page: Page?) {

  private val dashboardUrl = "http://localhost:8080/dashboard"
  private val page = page

  fun gotToPage() {
    page?.navigate(dashboardUrl)
  }

  fun isPageSuccessful(supplier: Supplier): Boolean {
    page?.waitForURL(dashboardUrl)
    return page?.url().equals(dashboardUrl) && page?.getByText(supplier.name)?.first()?.isVisible == true
  }

  fun isDownloadAllMovesActive(): Boolean {
    val download = page?.waitForDownload { page.locator("a.download-icon").click() }
    return download?.suggestedFilename()?.endsWith(".xlsx") == true
  }

  fun goToStandardMoves() {
    gotToPage()
    page?.waitForURL(dashboardUrl)
    page?.locator("a:has-text(\"Standard\")")?.click()
  }

  fun goToMoveBuyReferenceId() {
    gotToPage()
    page?.waitForURL(dashboardUrl)
    page?.locator("a:has-text(\"Find by move reference ID\")")?.click()
  }

  fun goToLongHaulMoves() {
    gotToPage()
    page?.waitForURL(dashboardUrl)
    page?.locator("a:has-text(\"Long haul\")")?.click()
  }

  fun navigateToMovesBy(moveTypeLabel: String) {
    page?.locator("a:has-text(\"${moveTypeLabel}\")")?.click()
  }
}
