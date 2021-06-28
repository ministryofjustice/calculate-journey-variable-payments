package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.FluentPage
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier

@PageUrl("http://localhost:8080/choose-supplier")
class ChooseSupplierPage : FluentPage() {

  @FindBy(linkText = "GEOAmey")
  private lateinit var geoamey: FluentWebElement

  @FindBy(linkText = "Serco")
  private lateinit var serco: FluentWebElement

  fun choose(supplier: Supplier) {
    if (supplier == Supplier.GEOAMEY) geoamey.click() else serco.click()
  }
}
