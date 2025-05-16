package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

class DashboardPage(page: Page?) {

  private val dashboardUrl = "http://localhost:8080/dashboard"
  private val page = page

  fun gotToPage() {
    page?.navigate(dashboardUrl)
  }

  fun isPageSuccessful(supplier: Supplier) {
    page?.waitForURL(dashboardUrl)
    assertThat(page!!).hasURL(dashboardUrl)
    val supplierLocator = page.getByText(supplier.name)
    assertThat(supplierLocator.first()).isVisible()
  }

  fun isDownloadAllMovesActive() {
    val download = page?.waitForDownload {
      page.locator("a.download-icon").click()
    }

    val downloadingHeading = page?.locator("a.govuk-heading-s:has-text('Downloading...')")
    assertThat(downloadingHeading).isVisible()

    val downloadAllHeading = page?.locator("a.govuk-heading-s:has-text('Download all moves')")
    assertThat(downloadAllHeading).isVisible(LocatorAssertions.IsVisibleOptions().setTimeout(20000.0))

    assert(download?.suggestedFilename()?.let { it.endsWith(".csv") || it.endsWith(".xlsx") } == true)
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
