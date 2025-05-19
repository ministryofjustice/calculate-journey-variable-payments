package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole

class LoginPage(page: Page?) {

  private val url = "http://localhost:9090/auth/sign-in"
  private val page = page
  fun login(username: String = "JPC_USER", password: String = "password123456") {
    page?.navigate(url)
    page?.getByLabel("username")?.fill(username)
    page?.getByLabel("password")?.fill(password)
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sign In"))?.click()
  }

  fun isLoginSuccessful() {
    val h1 = page?.locator("h1")
    assertThat(h1).containsText("Select service")
  }
}
