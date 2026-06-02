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
    // Wait for h1 to be visible (exists on both email verification and select service pages)
    val h1 = page?.locator("h1")
    h1?.waitFor()

    // If we're on the email verification page, click "Skip for now"
    val h1Text = h1?.textContent() ?: ""
    if (h1Text.contains("Verify your email", ignoreCase = true)) {
      page?.locator("a#cancel")?.click()
      page?.waitForLoadState()
    }
  }

  fun isLoginSuccessful() {
    val h1 = page?.locator("h1")
    assertThat(h1).containsText("Select service")
  }
}
