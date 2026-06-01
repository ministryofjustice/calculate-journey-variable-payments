package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole

class LoginPage(page: Page?) : BasicPage() {

  private val url = "http://localhost:9090/auth/sign-in"
  private val page = page

  fun login() {
    page?.navigate(url)
    page?.waitForLoadState()
    page?.getByLabel("username")?.fill(getProperty("jpc.web.user"))
    page?.getByLabel("password")?.fill(getProperty("jpc.web.password"))
    page?.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Sign In"))?.click()
    page?.waitForLoadState()

    // Handle extended email verification page if it appears
    skipExtendedEmailVerificationIfPresent()
  }

  private fun skipExtendedEmailVerificationIfPresent() {
    // Check if the "Skip for now" link exists on the extended email verification page
    // Use count() which doesn't wait/timeout - returns 0 immediately if not found
    val skipLink = page?.locator("a#cancel")
    if ((skipLink?.count() ?: 0) > 0) {
      skipLink?.click()
      page?.waitForLoadState()
    }
  }

  fun isLoginSuccessful() {
    val h1 = page?.locator("h1")
    assertThat(h1).containsText("Select service")
  }
}
