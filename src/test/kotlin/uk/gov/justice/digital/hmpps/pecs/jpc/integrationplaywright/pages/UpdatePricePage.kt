package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

class UpdatePricePage(page: Page?) {

  private val logger = loggerFor<UpdatePricePage>()
  private val url = "http://localhost:8080/update-price"
  private val page = page

  fun isPageSuccessful(): Boolean {
    page?.waitForURL(url)
    return page?.waitForSelector("h1")?.innerText()?.equals("Manage Journey Price Catalogue") == true
  }

  fun gotToPage(fromAgencyId: String, toAgencyId: String) {
    page?.navigate("$url/$fromAgencyId-$toAgencyId")
  }

  fun goToPriceExceptions() {
    page?.locator("a#tab_price-exceptions")?.click()
  }

  fun addPriceExceptions(date: LocalDate, price: Double) {
    goToPriceExceptions()
    removeAllPriceExceptions()
    page?.locator("#exception-month")?.selectOption(date.month.name)
    page?.locator("input#exception-price")?.fill(String.format("%.2f", price))
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Add exception"))?.click()
  }

  fun removeAllPriceExceptions() {
    val removeButton = page?.locator("button[id^='remove-exception-']")?.first()
    if (removeButton?.isVisible == true) {
      removeButton?.click()
      page?.waitForLoadState()
      removeAllPriceExceptions()
    }
  }
}
