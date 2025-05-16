package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ManageJourneyPriceCataloguePage

internal class ManageJourneyPriceCatalogueTest : PlayWrightTest() {

  @Test
  fun `User can go to Manage Journey Price Catalogue for SERCO`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val manageJourneyPriceCatalogue = ManageJourneyPriceCataloguePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    supplierPage.isPageSuccessful()
    supplierPage.goToSercoDashboard()
    dashboardPage.isPageSuccessful(Supplier.SERCO)
    manageJourneyPriceCatalogue.gotToPage()
    manageJourneyPriceCatalogue.isPageSuccessful()
  }

  @Test
  fun `User can go to Manage Journey Price Catalogue for GEOAMEY`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val manageJourneyPriceCatalogue = ManageJourneyPriceCataloguePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    supplierPage.isPageSuccessful()
    supplierPage.goToGeoameyDashboard()
    dashboardPage.isPageSuccessful(Supplier.GEOAMEY)
    manageJourneyPriceCatalogue.gotToPage()
    manageJourneyPriceCatalogue.isPageSuccessful()
  }
}
