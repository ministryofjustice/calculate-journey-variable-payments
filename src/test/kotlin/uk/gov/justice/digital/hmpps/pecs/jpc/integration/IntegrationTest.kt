package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.adapter.junit.jupiter.FluentTest
import org.openqa.selenium.WebDriver

/**
 * Super class for all integration test implementations.
 */
abstract class IntegrationTest : FluentTest() {

  private val driver: CustomHtmlUnitDriver = CustomHtmlUnitDriver()

  override fun newWebDriver(): WebDriver {
    return driver
  }
}
