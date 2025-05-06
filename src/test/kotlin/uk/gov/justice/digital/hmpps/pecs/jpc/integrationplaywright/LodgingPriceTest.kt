package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
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

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class LodgingPriceTest : PlayWrightTest() {

  @Test
  fun `add price exceptions and verify the move price matches the exception prices`() {

    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val selectMonth = SelectMonthPage(page)
    val searchJourneysPage = SearchJourneysPage(page)
    val journeysResultsPage = JourneysResultsPage(page)
    val updatePricePage = UpdatePricePage(page)
    val dashboardPage = DashboardPage(page)
    val movesByTypePage = MovesByTypePage(page)
    val manageJourneyPriceCataloguePage = ManageJourneyPriceCataloguePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToSercoDashboard()

    selectMonth.gotToPage()
    val selectedDate = selectMonth.goToMonth()

    manageJourneyPriceCataloguePage.gotToPage()
    manageJourneyPriceCataloguePage.goToFindJourneys()

    searchJourneysPage.findJourney("PRISON ONE L", "POLICE ONE L")
    journeysResultsPage.findJourneyAndUpdatePrice("PRISON ONE L", "POLICE ONE L")

    updatePricePage.addPriceExceptions(selectedDate, 3000.00)

    manageJourneyPriceCataloguePage.gotToPage()
    manageJourneyPriceCataloguePage.goToFindJourneys()

    searchJourneysPage.findJourney("POLICE ONE L", "POLICE TWO L")
    journeysResultsPage.findJourneyAndUpdatePrice("POLICE ONE L", "POLICE TWO L")

    updatePricePage.addPriceExceptions(selectedDate, 2.00)

    dashboardPage.gotToPage()

    dashboardPage.goToLongHaulMoves()
    assert(movesByTypePage.isPageSuccessful(MoveType.LONG_HAUL))

    val price = movesByTypePage.getPrice()
    assert(price != null && price.replace(",", "").equals("£3002.00"))
  }
}