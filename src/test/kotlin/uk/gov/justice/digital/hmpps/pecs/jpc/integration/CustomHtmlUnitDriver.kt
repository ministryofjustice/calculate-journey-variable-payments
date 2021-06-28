package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.WebResponse
import com.gargoylesoftware.htmlunit.attachment.Attachment
import com.gargoylesoftware.htmlunit.attachment.CollectingAttachmentHandler
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.openqa.selenium.htmlunit.HtmlUnitDriver

/**
 * Selenium (by design) does not expose download progress which makes it tricky for testing file downloads.
 *
 * This custom HtmlUnit driver implementation can download attachments by accessing them as [Attachment]s by using the AttachmentHandler interface.
 */
class CustomHtmlUnitDriver : HtmlUnitDriver() {

  lateinit var attachments: MutableList<Attachment>

  override fun modifyWebClient(client: WebClient): WebClient {
    attachments = mutableListOf()

    client.attachmentHandler = CollectingAttachmentHandler(attachments)

    return client
  }

  fun page(url: String): HtmlPage = webClient.getPage(url)

  fun firstAttachmentResponse(): WebResponse = attachments.first().page.webResponse
}
