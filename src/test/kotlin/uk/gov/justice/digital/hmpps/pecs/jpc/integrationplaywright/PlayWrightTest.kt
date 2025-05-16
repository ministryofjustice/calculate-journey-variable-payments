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
import org.springframework.beans.factory.annotation.Value
import java.nio.file.Paths

internal abstract class PlayWrightTest {

  companion object {
    // Shared between all tests in this class.
    @Value("\${playwright.test.headless:false}")
    private val headless: Boolean = false

    var playwright: Playwright? = null
    var browser: Browser? = null

    // New instance for each test method.
    var context: BrowserContext? = null
    var page: Page? = null

    @JvmStatic
    @BeforeAll
    fun launchBrowser() {
      playwright = Playwright.create()
      browser = playwright!!.chromium().launch(LaunchOptions().setHeadless(headless))
    }

    @AfterAll
    @JvmStatic
    fun closeBrowser() {
      playwright!!.close()
    }
  }

  @BeforeEach
  fun createContextAndPage() {
    context = browser?.newContext(
      Browser.NewContextOptions()
        .setRecordVideoSize(1920,1080)
        .setRecordVideoDir(Paths.get("build/reports/tests/testPlayWrightIntegration/videos/${this.javaClass.canonicalName}/")),
    )
    page = context?.newPage()
  }

  @AfterEach
  fun closeContext() {
    context?.close()
    page?.close()
  }
}
