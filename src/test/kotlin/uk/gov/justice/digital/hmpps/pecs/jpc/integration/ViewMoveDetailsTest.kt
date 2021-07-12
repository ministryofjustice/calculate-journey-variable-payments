package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.CHOOSE_SUPPLIER
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.DASHBOARD
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.FIND_MOVE
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.LOGIN
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MOVE_DETAILS_PAGE
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

internal class ViewMoveDetailsTest : IntegrationTest() {

  @Test
  fun `search for a move by the move reference identifier and view its details`() {
    goToPage(DASHBOARD)

    isAtPage(LOGIN).login()

    isAtPage(CHOOSE_SUPPLIER).choose(Supplier.GEOAMEY)

    isAtPage(DASHBOARD).navigateToFindMoveByReferenceId()

    isAtPage(FIND_MOVE).findMoveByReferenceId("STANDARDMOVE2")

    isAtPage(MOVE_DETAILS_PAGE).isAtPageForMoveID("M4")

    // TODO assert on details in the page
  }
}
