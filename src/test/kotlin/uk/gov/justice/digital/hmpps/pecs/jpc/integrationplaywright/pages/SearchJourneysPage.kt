package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

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
    page?.locator("input#from")?.fill(fromAgency)
    page?.waitForSelector("//ul[@id='from__listbox']/li", Page.WaitForSelectorOptions().setTimeout(1000.0))
    page?.locator("//ul[@id='from__listbox']/li[1]")?.click()
    page?.locator("input#to")?.fill(toAgency)
    page?.waitForSelector("//ul[@id='to__listbox']/li", Page.WaitForSelectorOptions().setTimeout(1000.0))
    page?.locator("//ul[@id='to__listbox']/li[1]")?.click()
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Find Journeys"))?.click()
    return page?.waitForSelector("h1")?.innerText()
      ?.startsWith("Manage Journey Price Catalogue") == true
  }
}
