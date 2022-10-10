package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebResponse
import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.FluentPage
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.FluentWait
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.CustomHtmlUnitDriver
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor
import java.time.Duration

/**
 * Super class for all application pages under test.
 */
abstract class ApplicationPage : FluentPage() {

  val logger = loggerFor <ApplicationPage>()
  /**
   * Helper method to assist with attachment downloads.
   */
  fun clickOnLinkText(anchorText: String): WebResponse {
    logger.info("Clicking on anchor text [$anchorText]")
    customDriver().page(url).getAnchorByText(anchorText).click<Page>()

    return customDriver().firstAttachmentResponse()
  }

  private fun customDriver() = super.getDriver() as CustomHtmlUnitDriver

  fun assertBannerIsPresent(title: String, body: String) {
    assertTextIsPresent(title)
    assertTextIsPresent(body)
  }

  private fun assertTextIsPresent(text: String) {
    find(By.xpath("//p[normalize-space(text())='$text']")).firstOrNull().let { assertThat(it).isNotNull }
  }

  internal inline fun <reified T : ApplicationPage> assertTextIsNotPresent(text: String): T {
    assertThat(super.pageSource()).doesNotContainIgnoringCase(text)

    return this as T
  }

  inline fun <reified T : ApplicationPage> isRowPresent(value: Any, vararg values: Any): T {
    val query = values.joinToString(" ") { "and contains(., '$it')" }
    logger.info("Looking for row with xpath:")
    logger.info(query)
    FluentWait(this).withTimeout(Duration.ofSeconds(30)).pollingEvery(Duration.ofSeconds(2)).until {
      find(By.xpath("//tr[contains(.,'$value') $query]")).firstOrNull() != null
      //*[@id="price-adjustment-history"]/table/tbody/tr/td[5]
    }

    return this as T
  }

  inline fun <reified T : ApplicationPage> isRowPresentAtIndex(tableId: String, value: Any, vararg values: Any, index: Int): T {
    val query = values.joinToString(" ") { "and contains(., '$it')" }

    val row = this.find(By.xpath("//table[@id = '$tableId']//tr[$index][contains(.,'$value') $query]")).firstOrNull()

    assertThat(row).isNotNull

    return this as T
  }
}
