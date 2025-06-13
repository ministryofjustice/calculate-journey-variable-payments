package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
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
    page?.waitForLoadState()
  }

  fun findJourney(fromAgency: String, toAgency: String): Boolean {
    page?.waitForSelector("input#from")
    page?.waitForSelector("input#to")
    page?.waitForSelector("button[id^='find-journeys']")
    page?.locator("input#from")?.fill(fromAgency)
    page?.locator("input#to")?.fill(toAgency)
    page?.locator("input#to")?.blur()
    page?.waitForSelector("button[id^='find-journeys']")?.click()
    return page?.waitForSelector("h1")?.innerText()
      ?.startsWith("Manage Journey Price Catalogue") == true
  }
}
