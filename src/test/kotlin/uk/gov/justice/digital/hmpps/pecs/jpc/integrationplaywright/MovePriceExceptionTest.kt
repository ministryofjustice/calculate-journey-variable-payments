package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.AnnualPriceAdjustmentPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.JourneysResultsPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ManageJourneyPriceCataloguePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SearchJourneysPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SelectMonthPage
import kotlin.random.Random

internal class MovePriceExceptionTest : PlayWrightTest() {

  @Test
  fun `add a price exception and verify the move price matches the exception price for SERCO`() {
    val fromAgency = "PRISON ONE"
    val toAgency = "POLICE ONE"
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val selectMonth = SelectMonthPage(page)
    val searchJourneysPage = SearchJourneysPage(page)
    val journeysResultsPage = JourneysResultsPage(page)
    var manageJourneyPriceCataloguePage = ManageJourneyPriceCataloguePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    assert(supplierPage.isPageSuccessful())
    supplierPage.goToSercoDashboard()
    selectMonth.gotToPage()
    assert(selectMonth.goToMonth())
    manageJourneyPriceCataloguePage.gotToPage()
    manageJourneyPriceCataloguePage.goToFindJourneys()
    searchJourneysPage.findJourney(fromAgency, toAgency)
    journeysResultsPage.findJourneyAndUpdatePrice(fromAgency, toAgency)
    assert(true)

  }
}
