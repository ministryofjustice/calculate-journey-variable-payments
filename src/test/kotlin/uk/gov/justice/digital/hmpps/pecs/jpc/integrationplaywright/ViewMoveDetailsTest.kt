package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.Dec2020MoveData.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.FindMovePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage

internal class ViewMoveDetailsTest : PlayWrightTest() {
  @Test
  fun `search for a move by the move reference identifier and view its details`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val findMovePage = FindMovePage(page)
    val move = standardMoveM4()
    loginPage.login()
    supplierPage.gotToPage()
    supplierPage.isPageSuccessful()
    supplierPage.goToGeoameyDashboard()
    dashboardPage.goToMoveBuyReferenceId()
    assert(findMovePage.findMoveByReferenceId(move.reference))
    findMovePage.assertPageShowsTheMove(move)
  }
}
