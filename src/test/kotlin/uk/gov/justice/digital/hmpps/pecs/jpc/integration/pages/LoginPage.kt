package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.FluentPage
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:9090/auth/login")
class LoginPage : FluentPage() {

  @FindBy(css = "input[type='submit']")
  private lateinit var signInButton: FluentWebElement

  @FindBy(css = "input[name='username']")
  private lateinit var username: FluentWebElement

  @FindBy(css = "input[name='password']")
  private lateinit var password: FluentWebElement

  fun login(username: String = "JPC_USER", password: String = "password123456") {
    this.username.fill().withText(username)
    this.password.fill().withText(password)
    signInButton.submit()
  }
}
