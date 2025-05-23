package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage

internal class DashboardTest : PlayWrightTest() {

  @Test
  fun `GEOAMEY user can download all moves from Dashboard`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    loginPage.login()
    supplierPage.gotToPage(Supplier.GEOAMEY)
    dashboardPage.isPageSuccessful(Supplier.GEOAMEY)
    dashboardPage.isDownloadAllMovesActive()
  }

  @Test
  fun `SERCO user can download all moves from Dashboard`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    loginPage.login()
    supplierPage.gotToPage(Supplier.SERCO)
    dashboardPage.isPageSuccessful(Supplier.SERCO)
    dashboardPage.isDownloadAllMovesActive()
  }
}
