package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.Year

internal class GotoDashboardTest : IntegrationTest(true) {

  @Test
  fun `User can login, choose GEOAmey and download the spreadsheet from the dashboard`() {
    loginAsSupplierAndDownloadSpreadsheet(Supplier.GEOAMEY)
  }

  @Test
  fun `User can login, choose Serco and download the spreadsheet from the dashboard`() {
    loginAsSupplierAndDownloadSpreadsheet(Supplier.SERCO)
  }

  private fun loginAsSupplierAndDownloadSpreadsheet(supplier: Supplier) {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(supplier)

    isAtPage(Dashboard)
      .isAtMonthYear(LocalDate.now().month, Year.now())
      .downloadAllMoves().apply {
        assertThat(this.statusCode).isEqualTo(200)
        assertThat(this.contentType).isEqualTo("application/vnd.ms-excel")
        assertThat(this.getResponseHeaderValue("Content-Disposition")).startsWith("attachment;filename=Journey_Variable_Payment_Output_$supplier")
      }
  }
}
