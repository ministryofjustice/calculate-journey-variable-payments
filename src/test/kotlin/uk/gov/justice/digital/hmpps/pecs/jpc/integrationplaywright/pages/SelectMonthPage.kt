package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.LocalDate

class SelectMonthPage(page: Page?) {

  private val logger = loggerFor<SelectMonthPage>()
  private val url = "http://localhost:8080/select-month"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
    page?.waitForLoadState()
  }

  fun goToMonth(
    date: LocalDate = LocalDate.now().minusMonths(2),
  ): LocalDate {
    page?.locator("input#month-year")?.fill("${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}")
    page?.locator("input#month-year")?.blur()
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Go to month"))?.click()
    val h1 = page?.locator("h1")
    assertThat(h1).hasText("${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}")
    return date
  }
}
