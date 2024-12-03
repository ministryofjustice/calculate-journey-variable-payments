package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.FindMove
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails
import java.io.File

internal class ViewMoveDetailsTest : IntegrationTest() {

  @Test
  fun `search for a move by the move reference identifier and view its details`() {
    try {
      loginAndGotoDashboardFor(Supplier.GEOAMEY)

      isAtPage(Dashboard).navigateToFindMoveByReferenceId()
      val findMove = isAtPage(FindMove)

      wait.until {
        findMove.searchReady()
      }

      findMove.findBy(standardMoveM4())

      val details = isAtPage(MoveDetails)
      wait.until { details.atPage(standardMoveM4().moveId) }
      details.isAtPageFor(standardMoveM4())
    } catch (e: Throwable) {
      val scrFile: File = (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
      FileUtils.copyFile(
        scrFile,
        File(imageLocation + "search-for-a-move-by-the-move-reference-identifier-and-view-its-details.jpg"),
      )
      throw e
    }
  }
}
