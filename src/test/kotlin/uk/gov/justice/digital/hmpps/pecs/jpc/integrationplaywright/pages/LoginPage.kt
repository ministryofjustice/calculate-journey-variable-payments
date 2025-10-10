package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole

class LoginPage(page: Page?) : BasicPage() {

  private val page = page
  private val url = System.getenv("APP_BASE_URL") ?: "http://localhost:8080"

  fun login() {
    page?.navigate(url)
    page?.waitForLoadState()
    page?.querySelector("#sign-out")?.click()
    page?.getByLabel("username")?.fill(getProperty("jpc.web.user"))
    page?.getByLabel("password")?.fill(getProperty("jpc.web.password"))
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sign In"))?.click()
  }

  fun isLoginSuccessful() {
    val h1 = page?.locator("h1")
    assertThat(h1).containsText("Choose a supplier")
  }
}
