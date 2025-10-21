package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole

class LoginPage(page: Page?) : BasicPage() {

  private val page = page
  private val rawEnv = System.getenv("APP_BASE_URL")
  private val cleaned = rawEnv
    ?.let { it.trim() }
    ?.let { it.replace(Regex("\\p{Cntrl}"), "") }
  private val url = cleaned?.ifBlank { null } ?: "http://localhost:8080"

  private fun debugUrl() {
    println("DEBUG APP_BASE_URL raw='" + (rawEnv ?: "null") + "'")
    println("DEBUG APP_BASE_URL cleaned='" + url + "' length=" + url.length)
    println(
      "DEBUG APP_BASE_URL codepoints=" +
        url.toCharArray().joinToString(",") { it.code.toString() }
    )
  }



  fun login() {
    debugUrl()
    require(url.startsWith("http://") || url.startsWith("https://")) {
      "Invalid URL scheme: $url"
    }
    println("Navigating to: '$url'")
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
