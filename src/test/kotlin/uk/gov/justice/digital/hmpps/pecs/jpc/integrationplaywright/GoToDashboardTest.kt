package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage

internal class GoToDashboardTest : PlayWrightTest() {

  @Test
  fun `User can go to SERCO dashboard by clicking the link on supplier page`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)

    loginPage.login()
    supplierPage.gotToPage()
    supplierPage.isPageSuccessful()
    supplierPage.goToSercoDashboard()
    dashboardPage.isPageSuccessful(Supplier.SERCO)
  }

  @Test
  fun `User can go to GEOAMEY dashboard by clicking the link on supplier page`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)

    loginPage.login()
    supplierPage.gotToPage()
    supplierPage.isPageSuccessful()
    supplierPage.goToGeoameyDashboard()
    dashboardPage.isPageSuccessful(Supplier.GEOAMEY)
  }

  @Test
  fun `User can go directly to GEOAMEY dashboard`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    loginPage.login()
    supplierPage.gotToPage(Supplier.GEOAMEY)
    dashboardPage.isPageSuccessful(Supplier.GEOAMEY)
  }

  @Test
  fun `User can go directly to SERCO dashboard`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    loginPage.login()
    supplierPage.gotToPage(Supplier.SERCO)
    dashboardPage.isPageSuccessful(Supplier.SERCO)
  }
}
