package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:8080/search-journeys")
class ManageJourneyPricePage : ApplicationPage() {

  @FindBy(css = "input[id='from']")
  private lateinit var from: FluentWebElement

  @FindBy(css = "input[id='to']")
  private lateinit var to: FluentWebElement

  @FindBy(css = "button[id='find-journeys']")
  private lateinit var findJourneys: FluentWebElement

  fun findJourneyForPricing(fromAgency: String, toAgency: String) {
    from.fill().withText(fromAgency)
    to.fill().withText(toAgency)

    findJourneys.submit()
  }

  fun assertTextIsPresent(text: String): ManageJourneyPricePage {
    Assertions.assertThat(super.pageSource()).containsIgnoringCase(text)

    return this
  }
}
