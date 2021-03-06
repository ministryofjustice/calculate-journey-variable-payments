package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money

@PageUrl("http://localhost:8080/update-price/{fromAgencyId-toAgencyId}")
class UpdatePricePage : ApplicationPage() {

  @FindBy(id = "price")
  private lateinit var price: FluentWebElement

  @FindBy(id = "confirm-save-price")
  private lateinit var submit: FluentWebElement

  fun isAtPricePageForJourney(fromAgencyId: String, toAgencyId: String): UpdatePricePage {
    this.isAt("$fromAgencyId-$toAgencyId")

    return this
  }

  fun updatePriceForJourney(fromAgencyId: String, toAgencyId: String, amount: Money) {
    this.isAtPricePageForJourney(fromAgencyId, toAgencyId)
    price.fill().withText(amount.toString())
    submit.click()
  }
}
