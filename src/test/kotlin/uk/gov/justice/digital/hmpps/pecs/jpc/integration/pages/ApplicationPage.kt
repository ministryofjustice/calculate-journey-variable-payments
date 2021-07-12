package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebResponse
import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.FluentPage
import org.openqa.selenium.By
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.CustomHtmlUnitDriver

/**
 * Super class for all application pages under test.
 */
abstract class ApplicationPage : FluentPage() {

  /**
   * Helper method to assist with attachment downloads.
   */
  fun clickOnLinkText(anchorText: String): WebResponse {
    customDriver().page(url).getAnchorByText(anchorText).click<Page>()

    return customDriver().firstAttachmentResponse()
  }

  private fun customDriver() = super.getDriver() as CustomHtmlUnitDriver

  inline fun <reified T : ApplicationPage> isAtPage(): T {
    this.isAt()

    return this as T
  }

  fun assertBannerIsPresent(title: String, body: String) {
    assertTextIsPresent(title)
    assertTextIsPresent(body)
  }

  private fun assertTextIsPresent(text: String) {
    find(By.xpath("//p[normalize-space(text())='$text']")).firstOrNull().let { assertThat(it).isNotNull }
  }

  inline fun <reified T : ApplicationPage> isRowPresent(value: Any, vararg values: Any): T {
    val query = values.joinToString(" ") { "and contains(., '$it')" }

    val row = this.find(By.xpath("//tr[contains(.,'$value') $query]")).firstOrNull()

    assertThat(row).isNotNull

    return this as T
  }
}
