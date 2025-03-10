package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login

internal class GotoDashboardTest : IntegrationTest() {

  @Test
  fun `User can login and logout from the dashboard`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard)

    logout()

    isAtPage(Login)
  }
}
