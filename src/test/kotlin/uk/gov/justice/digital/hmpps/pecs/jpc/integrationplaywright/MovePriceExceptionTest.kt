package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.JourneysResultsPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ManageJourneyPriceCataloguePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.MovesByTypePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SearchJourneysPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SelectMonthPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.UpdatePricePage

internal class MovePriceExceptionTest : PlayWrightTest() {

  @Test
  @Order(1)
  fun `add a price exception and verify the move price matches the exception price for SERCO`() {
    val fromAgency = "PRISON ONE"
    val toAgency = "POLICE ONE"
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val selectMonth = SelectMonthPage(page)
    val searchJourneysPage = SearchJourneysPage(page)
    val journeysResultsPage = JourneysResultsPage(page)
    val updatePricePage = UpdatePricePage(page)
    val dashboardPage = DashboardPage(page)
    val movesByTypePage = MovesByTypePage(page)
    var manageJourneyPriceCataloguePage = ManageJourneyPriceCataloguePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToSercoDashboard()
    selectMonth.gotToPage()
    val selectedDate = selectMonth.goToMonth()
    manageJourneyPriceCataloguePage.gotToPage()
    manageJourneyPriceCataloguePage.goToFindJourneys()
    searchJourneysPage.findJourney(fromAgency, toAgency)
    journeysResultsPage.findJourneyAndUpdatePrice(fromAgency, toAgency)
    updatePricePage.addPriceExceptions(selectedDate, 3000.00)
    dashboardPage.goToStandardMoves()
    assert(movesByTypePage.isPageSuccessful(MoveType.STANDARD))
    assert(movesByTypePage.getPrice().equals("£3,000.00"))
  }

  //@Test
  //@Order(2)
  // This test can be activated when all Selenium test will be replaced. The existing selenium test change the end price
  fun `remove a price exception and verify the move price is back to its original price`() {
    val fromAgency = "PRISON ONE"
    val toAgency = "POLICE ONE"
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val selectMonth = SelectMonthPage(page)
    val searchJourneysPage = SearchJourneysPage(page)
    val journeysResultsPage = JourneysResultsPage(page)
    val updatePricePage = UpdatePricePage(page)
    val dashboardPage = DashboardPage(page)
    val movesByTypePage = MovesByTypePage(page)
    var manageJourneyPriceCataloguePage = ManageJourneyPriceCataloguePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToSercoDashboard()
    selectMonth.gotToPage()
    selectMonth.goToMonth()
    manageJourneyPriceCataloguePage.gotToPage()
    manageJourneyPriceCataloguePage.goToFindJourneys()
    searchJourneysPage.findJourney(fromAgency, toAgency)
    journeysResultsPage.findJourneyAndUpdatePrice(fromAgency, toAgency)
    updatePricePage.goToPriceExceptions()
    updatePricePage.removeAllPriceExceptions()
    dashboardPage.goToStandardMoves()
    assert(movesByTypePage.isPageSuccessful(MoveType.STANDARD))
    assert(movesByTypePage.getPrice().equals("101.00"))
  }
}
