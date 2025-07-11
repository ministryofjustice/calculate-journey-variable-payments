package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

class JourneysResultsPage(page: Page?) {

  private val logger = loggerFor<JourneysResultsPage>()
  private val url = "http://localhost:8080/journeys-results"
  private val page = page

  fun gotToPage(fromAgency: String, toAgency: String) {
    page?.navigate("$url?pickup-up=$fromAgency&drop-off=$toAgency")
    page?.waitForLoadState()
  }

  fun findJourneyAndUpdatePrice(fromAgency: String): Boolean {
    page?.waitForSelector("h1")?.innerText()?.startsWith("Manage Journey Price Catalogue")
    val rows = page?.locator("//tr[td/a/span[contains(text(), '$fromAgency')]]")

    when (rows?.count()) {
      0 -> {
        logger.info("Record not found for: $fromAgency")
        return false
      }

      1 -> {
        logger.info("Scenario when journey is found for: $fromAgency")
        rows.nth(0).locator("td:nth-child(5)").click()
        page?.waitForSelector("h1")?.innerText()?.contains("Manage Journey Price Catalogue")
        return true
      }

      else -> {
        logger.info("To many records found for: $fromAgency")
        return false
      }
    }
    return false
  }
}
