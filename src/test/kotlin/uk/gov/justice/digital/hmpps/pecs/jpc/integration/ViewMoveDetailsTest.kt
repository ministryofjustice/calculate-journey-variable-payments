package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.FindMove
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails

internal class ViewMoveDetailsTest : IntegrationTest() {

  @Test
  fun `search for a move by the move reference identifier and view its details`() {
    loginAndGotoDashboardFor(Supplier.GEOAMEY)

    isAtPage(Dashboard).navigateToFindMoveByReferenceId()

    isAtPage(FindMove).findBy(standardMoveM4())

    isAtPage(MoveDetails).isAtPageFor(standardMoveM4())
  }
}
