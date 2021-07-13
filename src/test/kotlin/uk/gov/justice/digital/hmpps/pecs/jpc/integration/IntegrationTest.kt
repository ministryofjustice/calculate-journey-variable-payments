package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.adapter.junit.jupiter.FluentTest
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ApplicationPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages

/**
 * Super class for all integration test implementations.
 */
internal abstract class IntegrationTest(useCustomDriver: Boolean = false) : FluentTest() {

  // The custom driver is for testing the spreadsheet download functionality only!
  private val testDriver: WebDriver = if (useCustomDriver) CustomHtmlUnitDriver() else ChromeDriver()

  override fun newWebDriver(): WebDriver = testDriver

  fun goToPage(page: Pages<*>) {
    goTo(newInstance(page.page))
  }

  fun <T : ApplicationPage> isAtPage(page: Pages<T>): T = newInstance(page.page).also { it.isAt() }
}
