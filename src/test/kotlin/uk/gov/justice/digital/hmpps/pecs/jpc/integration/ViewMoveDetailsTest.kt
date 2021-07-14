package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.FindMove
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

internal class ViewMoveDetailsTest : IntegrationTest() {

  @Test
  fun `search for a move by the move reference identifier and view its details`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.GEOAMEY)

    isAtPage(Dashboard).navigateToFindMoveByReferenceId()

    isAtPage(FindMove).findBy(standardMoveM4())

    isAtPage(MoveDetails).isAtPageFor(standardMoveM4())
  }
}
