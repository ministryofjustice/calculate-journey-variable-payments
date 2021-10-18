package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.By
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import java.time.Month

@PageUrl("http://localhost:8080/update-price/{fromAgencyId-toAgencyId}")
class UpdatePricePage : ApplicationPage() {

  @FindBy(id = "price")
  private lateinit var price: FluentWebElement

  @FindBy(id = "confirm-save-price")
  private lateinit var submit: FluentWebElement

  @FindBy(id = "tab_price-exceptions")
  private lateinit var priceExceptionsTab: FluentWebElement

  @FindBy(id = "exception-month")
  private lateinit var exceptionMonth: FluentWebElement

  @FindBy(id = "exception-price")
  private lateinit var exceptionPrice: FluentWebElement

  @FindBy(id = "confirm-save-exception")
  private lateinit var submitException: FluentWebElement

  fun isAtPricePageForJourney(fromAgencyId: String, toAgencyId: String): UpdatePricePage {
    this.isAt("$fromAgencyId-$toAgencyId")

    return this
  }

  fun updatePriceForJourney(fromAgencyId: String, toAgencyId: String, amount: Money) {
    this.isAtPricePageForJourney(fromAgencyId, toAgencyId)
    price.fill().withText(amount.toString())
    submit.click()
  }

  fun assertTextIsPresent(text: String): UpdatePricePage {
    assertThat(super.pageSource()).containsIgnoringCase(text)

    return this
  }

  fun showPriceExceptionsTab(): UpdatePricePage {
    priceExceptionsTab.click()

    return this
  }

  fun addPriceException(month: Month, amount: Money) {
    exceptionMonth.fillSelect().withValue(month.name)
    exceptionPrice.fill().withText(amount.toString())

    submitException.click()
  }

  fun removePriceException(month: Month): UpdatePricePage {
    val element = super.find(By.id("remove-exception-${month.name}")).firstOrNull()

    assertThat(element).isNotNull

    element!!.click()

    return this
  }
}
