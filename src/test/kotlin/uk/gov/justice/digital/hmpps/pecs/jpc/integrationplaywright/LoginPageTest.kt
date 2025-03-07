package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage

internal class LoginPageTest {

  companion object {
    // Shared between all tests in this class.
    var playwright: Playwright? = null
    var browser: Browser? = null

    // New instance for each test method.
    var context: BrowserContext? = null
    var page: Page? = null

    @JvmStatic
    @BeforeAll
    fun launchBrowser() {
      playwright = Playwright.create()
      browser = playwright!!.chromium().launch(LaunchOptions().setHeadless(false))
    }

    @AfterAll
    @JvmStatic
    fun closeBrowser() {
      playwright!!.close()
    }
  }

  @BeforeEach
  fun createContextAndPage() {
    context = browser?.newContext()
    page = context?.newPage()
  }

  @AfterEach
  fun closeContext() {
    context?.close()
  }

  @Test
  fun `can load index`() {
    val loginPage = LoginPage(page)
    loginPage.login()
    assert(loginPage.isLoginSuccessful())
  }
}
