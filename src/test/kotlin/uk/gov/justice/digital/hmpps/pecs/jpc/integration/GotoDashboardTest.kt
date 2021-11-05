package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
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

  @Test
  fun `User can login and logout from the dashboard`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard)

    logout()

    isAtPage(Login)
  }

  private fun loginAsSupplierAndDownloadSpreadsheet(supplier: Supplier) {
    loginAndGotoDashboardFor(supplier)

    isAtPage(Dashboard)
      .isAtMonthYear(LocalDate.now().month, Year.now())
      .downloadAllMoves().apply {
        assertThat(this.statusCode).isEqualTo(200)
        assertThat(this.contentType).isEqualTo("application/vnd.ms-excel")
        assertThat(this.getResponseHeaderValue("Content-Disposition")).startsWith("attachment;filename=Journey_Variable_Payment_Output_$supplier")
      }
  }
}
