package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy

@PageUrl("http://localhost:8080/search-locations")
class SearchLocationsPage : ApplicationPage() {

  @FindBy(id = "location")
  private lateinit var locationNameInput: FluentWebElement

  @FindBy(id = "find-location")
  private lateinit var submitButton: FluentWebElement

  fun searchForLocation(locationName: String) {
    locationNameInput.fill().withText(locationName)
    submitButton.submit()
  }

  fun isLocationUpdatedMessagePresent(agencyId: String, locationName: String): SearchLocationsPage {
    assertBannerIsPresent("Location updated", "$agencyId successfully mapped to $locationName")

    return this
  }
}
