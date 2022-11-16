package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:8080/annual-price-adjustment")
class AnnualPriceAdjustmentPage : ApplicationPage() {

  @FindBy(id = "inflationary-rate")
  private lateinit var inflationaryRate: FluentWebElement

  @FindBy(id = "volumetric-rate")
  private lateinit var volumetricRate: FluentWebElement

  @FindBy(id = "details")
  private lateinit var details: FluentWebElement

  @FindBy(id = "confirm-price-adjustment")
  private lateinit var submitButton: FluentWebElement

  @FindBy(linkText = "Price adjustment history")
  private lateinit var priceAdjustmentHistoryTab: FluentWebElement

  fun applyAdjustment(rate: Double, details: String) {
    this.inflationaryRate.fill().withText(rate.toString())
    this.details.fill().withText(details)

    submitButton.submit()
  }

  fun applyAdjustments(inflation: Double, volume: Double, details: String) {
    this.inflationaryRate.fill().withText(inflation.toString())
    this.volumetricRate.fill().withText(volume.toString())
    this.details.fill().withText(details)

    submitButton.submit()
  }

  fun showPriceAdjustmentHistoryTab(): AnnualPriceAdjustmentPage {
    priceAdjustmentHistoryTab.click()

    return this
  }

  fun isPriceHistoryRowPresent(type: String, rate: Double, year: Int, notes: String): AnnualPriceAdjustmentPage {
    isRowPresent<AnnualPriceAdjustmentPage>("$type price adjustment by rate of $rate for contractual year $year", notes)

    return this
  }
}
