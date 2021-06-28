package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.adapter.junit.jupiter.FluentTest
import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.LoginPage
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

class GotoDashboardTest : FluentTest() {

  @Page
  private lateinit var dashboardPage: DashboardPage

  @Page
  private lateinit var loginPage: LoginPage

  @Page
  private lateinit var chooseSupplierPage: ChooseSupplierPage

  @Test
  fun `User can login, go to the dashboard after choosing supplier`() {
    goTo(dashboardPage)
    loginPage.isAt()
    loginPage.login()
    chooseSupplierPage.isAt()
    chooseSupplierPage.choose(Supplier.GEOAMEY)
    dashboardPage.isAt()
  }
}
