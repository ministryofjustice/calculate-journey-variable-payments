package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

class SelectMonthPage(page: Page?) {

  private val logger = loggerFor<SelectMonthPage>()
  private val url = "http://localhost:8080/select-month"
  private val page = page

  fun isPageSuccessful(): Boolean {
    page?.waitForURL(url)
    return page?.waitForSelector("h1")?.innerText()?.equals("Jump to month") == true
  }

  fun gotToPage() {
    page?.navigate(url)
  }

  fun goToMonth(
    date: LocalDate = LocalDate.now().minusMonths(2),
  ): LocalDate {
    page?.locator("input#month-year")?.fill("${date.month.name} ${date.year}")
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Go to month"))?.click()
    assert(page?.waitForSelector("h1")?.innerText()?.equals("${date.month.name} ${date.year}", ignoreCase = true) == true)
    return date
  }
}
