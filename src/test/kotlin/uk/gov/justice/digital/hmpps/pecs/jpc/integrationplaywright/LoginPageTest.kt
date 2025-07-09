package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage

internal class LoginPageTest : PlayWrightTest() {

  @Test
  fun `can load index`() {
    val loginPage = LoginPage(page)
    loginPage.login()
    loginPage.isLoginSuccessful()
  }
}
