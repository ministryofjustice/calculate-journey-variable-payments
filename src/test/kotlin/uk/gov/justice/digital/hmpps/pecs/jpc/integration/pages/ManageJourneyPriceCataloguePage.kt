package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:8080/manage-journey-price-catalogue")
class ManageJourneyPriceCataloguePage : ApplicationPage() {

  @FindBy(linkText = "Find journeys")
  private lateinit var findJourneysLink: FluentWebElement

  fun navigateToFindJourneys() {
    findJourneysLink.click()
  }
}
