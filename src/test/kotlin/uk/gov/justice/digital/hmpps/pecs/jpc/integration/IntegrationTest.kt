package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.adapter.junit.jupiter.FluentTest
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ApplicationPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login

/**
 * Super class for all integration test implementations.
 */
internal abstract class IntegrationTest(useCustomDriver: Boolean = false) : FluentTest() {

  @FindBy(id = "sign-out")
  private lateinit var logoutButton: FluentWebElement

  // The custom driver is for testing the spreadsheet download functionality only!
  private val testDriver: WebDriver = if (useCustomDriver) CustomHtmlUnitDriver() else ChromeDriver(ChromeOptions().apply { addArguments("--headless") })

  override fun newWebDriver(): WebDriver = testDriver

  fun goToPage(page: Pages<*>) {
    goTo(newInstance(page.page))
  }

  fun loginAndGotoDashboardFor(supplier: Supplier) {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(supplier)
  }

  fun logout() {
    logoutButton.click()
  }

  fun <T : ApplicationPage> isAtPage(page: Pages<T>): T = newInstance(page.page).also { it.isAt() }
}
