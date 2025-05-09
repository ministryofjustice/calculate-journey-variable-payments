package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate
import java.time.Month

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

  fun isAtPricePageForJourney(fromAgencyId: String, toAgencyId: String): Boolean {
    return page?.url()?.contains("$url/$fromAgencyId-$toAgencyId") ?: false
  }

  fun updatePriceForJourney(fromAgencyId: String, toAgencyId: String, amount: Money) {
    page?.locator("#price")?.fill(amount.toString())
    page?.locator("#confirm-save-price")?.click()
  }

  fun goToPriceExceptions() {
    page?.locator("a#tab_price-exceptions")?.click()
  }

  fun addPriceExceptions(date: LocalDate, price: Double) {
    goToPriceExceptions()
    removeAllPriceExceptions()
    page?.locator("#exception-month")?.selectOption(date.month.name)
    page?.locator("input#exception-price")?.fill(String.format("%.2f", price))
    page?.waitForSelector("button[id^='confirm-save-exception']")?.click()
  }

  fun removeAllPriceExceptions() {
    val removeButton = page?.locator("button[id^='remove-exception-']")?.first()
    if (removeButton?.isVisible == true) {
      removeButton?.click()
      page?.waitForLoadState()
      removeAllPriceExceptions()
    }
  }

  fun assertTextIsPresent(text: String): Boolean {
    return page?.content()?.contains(text) ?: false
  }

  fun assertTextIsNotPresent(text: String): Boolean {
    return !(page?.content()?.contains(text) ?: true)
  }

  fun removePriceException(month: Month) {
    page?.locator("#remove-exception-${month.name}")?.click()
  }
}
