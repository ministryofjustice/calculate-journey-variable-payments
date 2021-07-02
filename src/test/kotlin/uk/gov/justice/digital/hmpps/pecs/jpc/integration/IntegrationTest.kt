package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.adapter.junit.jupiter.FluentTest
import org.fluentlenium.core.annotation.Page
import org.openqa.selenium.WebDriver
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.LoginPage

/**
 * Super class for all integration test implementations.
 */
internal abstract class IntegrationTest : FluentTest() {

  private val driver: CustomHtmlUnitDriver = CustomHtmlUnitDriver()

  @Page
  protected lateinit var loginPage: LoginPage

  override fun newWebDriver(): WebDriver {
    return driver
  }
}
