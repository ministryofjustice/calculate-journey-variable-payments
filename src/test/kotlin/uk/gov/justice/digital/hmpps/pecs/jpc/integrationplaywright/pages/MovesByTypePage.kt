package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

class MovesByTypePage(page: Page?) {

  private val url = "http://localhost:8080/moves-by-type"
  private val page = page
  private val logger = loggerFor<MovesByTypePage>()

  fun gotToPage(moveType: MoveType) {
    page?.navigate("$url/$moveType")
  }

  fun isPageSuccessful(moveType: MoveType): Boolean {
    page?.waitForURL("$url/$moveType")
    return page?.url().equals("$url/$moveType")
  }

  fun getPrice(): String? {
    val priceLocator = page?.locator("li:has(p:has-text('total price')) >> p.govuk-heading-l")
    val price = priceLocator?.textContent()?.trim()
    logger.info("the price is $price")
    return price
  }
  fun clickMoveByReference(moveRef: String) {
    page?.locator("a:has-text(\"${moveRef}\")")?.click()
  }
}

enum class MoveType {
  STANDARD,
  LONG_HAUL,
}
