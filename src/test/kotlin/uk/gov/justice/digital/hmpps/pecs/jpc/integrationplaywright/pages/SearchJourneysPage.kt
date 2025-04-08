package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.options.AriaRole
import com.microsoft.playwright.options.WaitForSelectorState
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

class SearchJourneysPage(page: Page?) {

  private val logger = loggerFor<SearchJourneysPage>()
  private val url = "http://localhost:8080/search-journeys"
  private val page = page

  fun isPageSuccessful(): Boolean {
    page?.waitForURL(url)
    return page?.waitForSelector("h2")?.innerText()?.equals("Find Journeys") == true
  }

  fun gotToPage() {
    page?.navigate(url)
  }

  fun findJourney(fromAgency: String, toAgency: String): Boolean {
    page?.waitForLoadState()
    page?.locator("input#from")?.fill(fromAgency)
    try {
      page?.waitForSelector("#from__listbox li", Page.WaitForSelectorOptions().setTimeout(2000.0))
    } catch (e: PlaywrightException) {
      return false
    }?.click()
    page?.locator("input#to")?.fill(toAgency)
    try {
      page?.waitForSelector("#to__listbox li", Page.WaitForSelectorOptions().setTimeout(2000.0))
    } catch (e: PlaywrightException) {
      return false
    }?.click()
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Find Journeys"))?.click()
    return page?.waitForSelector("h1")?.innerText()
      ?.startsWith("Manage Journey Price Catalogue") == true
  }

}
