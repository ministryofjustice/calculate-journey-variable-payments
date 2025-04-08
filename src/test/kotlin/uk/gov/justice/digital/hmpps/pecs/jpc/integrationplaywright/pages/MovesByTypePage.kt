package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page

class MovesByTypePage(page: Page?) {

  private val url = "http://localhost:8080/moves-by-type"
  private val page = page

  fun gotToPage(moveType: MoveType) {
    page?.navigate("$url/$moveType")
  }

  fun isPageSuccessful(moveType: MoveType): Boolean {
    page?.waitForURL("$url/$moveType")
    return page?.url().equals("$url/$moveType")
  }

  fun getPrice(): String? {
    val priceLocator = page?.locator("li:has(p:has-text('total price')) >> p.govuk-heading-l")
    return priceLocator?.textContent()?.trim()
  }
}

enum class MoveType {
  STANDARD,
}
