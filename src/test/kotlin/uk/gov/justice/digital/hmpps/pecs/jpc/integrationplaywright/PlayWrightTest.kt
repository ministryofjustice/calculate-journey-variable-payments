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
import java.nio.file.Paths

internal abstract class PlayWrightTest {

  companion object {

    var playwright: Playwright? = null
    var browser: Browser? = null

    var context: BrowserContext? = null
    var page: Page? = null

    @JvmStatic
    @BeforeAll
    fun launchBrowser() {
      val headlessProp = System.getProperty("playwright.headless")
      val headless = headlessProp?.toBooleanStrictOrNull() ?: true
      val args = arrayOf("--no-sandbox", "--disable-dev-shm-usage")

      playwright = Playwright.create()
      browser = playwright!!.chromium().launch(
        LaunchOptions()
          .setHeadless(headless)
          .setArgs(args.toMutableList()),
      )
    }

    @AfterAll
    @JvmStatic
    fun closeBrowser() {
      playwright?.close()
    }
  }

  @BeforeEach
  fun createContextAndPage() {
    context = browser?.newContext(
      Browser.NewContextOptions()
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
