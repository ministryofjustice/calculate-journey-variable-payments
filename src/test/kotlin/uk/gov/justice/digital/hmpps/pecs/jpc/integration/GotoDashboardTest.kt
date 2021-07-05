package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.Year

internal class GotoDashboardTest : IntegrationTest() {

  @Page
  private lateinit var dashboardPage: DashboardPage

  @Page
  private lateinit var chooseSupplierPage: ChooseSupplierPage

  @Test
  fun `User can login, choose GEOAmey and download the spreadsheet from the dashboard`() {
    loginAsSupplierAndDownloadSpreadsheet(Supplier.GEOAMEY)
  }

  @Test
  fun `User can login, choose Serco and download the spreadsheet from the dashboard`() {
    loginAsSupplierAndDownloadSpreadsheet(Supplier.SERCO)
  }

  private fun loginAsSupplierAndDownloadSpreadsheet(supplier: Supplier) {
    goTo(dashboardPage)
    loginPage.isAt()
    loginPage.login()
    chooseSupplierPage.isAt()
    chooseSupplierPage.choose(supplier)
    dashboardPage.isAt()
    dashboardPage.isAtMonthYear(LocalDate.now().month, Year.now())

    dashboardPage.downloadAllMoves().apply {
      assertThat(this.statusCode).isEqualTo(200)
      assertThat(this.contentType).isEqualTo("application/vnd.ms-excel")
      assertThat(this.getResponseHeaderValue("Content-Disposition")).startsWith("attachment;filename=Journey_Variable_Payment_Output_$supplier")
    }
  }
}
