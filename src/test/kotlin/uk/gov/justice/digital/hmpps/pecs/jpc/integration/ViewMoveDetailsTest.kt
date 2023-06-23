package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.Ignore
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.FindMove
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails

internal class ViewMoveDetailsTest : IntegrationTest() {

  @Ignore
  fun `search for a move by the move reference identifier and view its details`() {
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
  }
}
