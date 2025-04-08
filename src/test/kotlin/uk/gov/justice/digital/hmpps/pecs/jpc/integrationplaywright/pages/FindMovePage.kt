package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

class FindMovePage(page: Page?) {

  private val url = "http://localhost:8080/find-move"
  private val page = page

  fun gotToPage() {
    page?.navigate(url)
  }

  fun isPageSuccessful(): Boolean {
    page?.waitForURL(url)
    return page?.url().equals(url)
  }

  fun findMoveByReferenceId(referenceId: String): Boolean {
    page?.waitForLoadState()
    page?.locator("input#reference")?.fill(referenceId)
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Find Move"))?.click()
    return false
  }
}
