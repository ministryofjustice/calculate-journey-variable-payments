package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.adapter.junit.jupiter.FluentTest
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.FindBy
import org.openqa.selenium.support.ui.FluentWait
import org.openqa.selenium.support.ui.Wait
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ApplicationPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.LoginPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration
import java.util.logging.Level

/**
 * Super class for all integration test implementations.
 */
internal abstract class IntegrationTest(useCustomDriver: Boolean = false) : FluentTest() {

  private val logger = loggerFor<IntegrationTest>()
  protected val imageLocation = "build/reports/tests/testIntegration/"

  @FindBy(id = "sign-out")
  private lateinit var logoutButton: FluentWebElement

  // The custom driver is for testing the spreadsheet download functionality only!
  private val testDriver: WebDriver = getTestDriver(useCustomDriver)

  private fun getTestDriver(useCustomDriver: Boolean): WebDriver {
    return if (useCustomDriver) {
      CustomHtmlUnitDriver()
    } else {
      val driver = ChromeDriver(
        ChromeOptions().apply {
          addArguments(
            "--window-size=1300,2000",
            "--ignore-certificate-errors",
          )
        },
      )

      driver.setLogLevel(Level.SEVERE)
      return driver
    }
  }

  protected val wait: Wait<WebDriver> =
    FluentWait(testDriver).withTimeout(Duration.ofSeconds(60)).pollingEvery(Duration.ofSeconds(4))

  override fun newWebDriver(): WebDriver = testDriver

  fun goToPage(page: Pages<*>) {
    goTo(newInstance(page.page))
  }

  fun loginAndGotoDashboardFor(supplier: Supplier) {
    goToPage(Dashboard)
    logUrl()
    logger.info("Waiting until we're at the expected page")
    logUrl()
    val login: LoginPage = isAtPage(Login)
    logUrl()
    logger.info("Waiting for login button")
    wait.until {
      logUrl()
      login.loginReady()
    }
    logUrl()
    logger.info("Checking we're at the expected page")
    isAtPage(Login).login()

    logger.info("Logging in")

    // login.login()

    logger.info("Checking we're at the choose supplier page")
    logUrl()
    val cs: ChooseSupplierPage = isAtPage(Pages.ChooseSupplier)

    logger.info("Choosing supplier {}", supplier)
    cs.choose(supplier)
    logUrl()
  }

  fun logout() {
    logoutButton.click()
  }

  fun <T : ApplicationPage> isAtPage(page: Pages<T>): T = newInstance(page.page)

  private fun logUrl() {
    logger.info(testDriver.currentUrl)
  }
}
