package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebResponse
import org.fluentlenium.core.FluentPage
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
}
