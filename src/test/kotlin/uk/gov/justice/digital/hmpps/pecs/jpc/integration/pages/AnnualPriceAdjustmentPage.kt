package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:8080/annual-price-adjustment")
class AnnualPriceAdjustmentPage : ApplicationPage() {

  @FindBy(id = "rate")
  private lateinit var rate: FluentWebElement

  @FindBy(id = "details")
  private lateinit var details: FluentWebElement

  @FindBy(id = "confirm-price-adjustment")
  private lateinit var submitButton: FluentWebElement

  fun applyAdjustment(rate: Double, details: String) {
    this.rate.fill().withText(rate.toString())
    this.details.fill().withText(details)

    submitButton.submit()
  }
}
