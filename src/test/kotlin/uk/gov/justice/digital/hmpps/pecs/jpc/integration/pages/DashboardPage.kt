package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import com.gargoylesoftware.htmlunit.WebResponse
import org.fluentlenium.core.annotation.PageUrl

@PageUrl("http://localhost:8080/dashboard")
class DashboardPage : ApplicationPage() {

  fun downloadAllMoves(): WebResponse = clickOnLinkText("Download all moves")
}
