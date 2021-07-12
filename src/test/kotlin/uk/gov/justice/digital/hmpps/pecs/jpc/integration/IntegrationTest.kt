package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.adapter.junit.jupiter.FluentTest
import org.fluentlenium.core.annotation.Page
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.LoginPage

/**
 * Super class for all integration test implementations.
 */
internal abstract class IntegrationTest(useCustomDriver: Boolean = false) : FluentTest() {

  // The custom driver is for testing the spreadsheet download functionality only!
  private val testDriver: WebDriver = if (useCustomDriver) CustomHtmlUnitDriver() else ChromeDriver()

  @Page
  protected lateinit var loginPage: LoginPage

  override fun newWebDriver(): WebDriver {
    return testDriver
  }
}
