package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType

@PageUrl("http://localhost:8080/map-location/{agencyId}")
class MapLocationPage : ApplicationPage() {

  @FindBy(id = "location-name")
  private lateinit var locationNameInput: FluentWebElement

  @FindBy(id = "location-type-id")
  private lateinit var locationTypeSelect: FluentWebElement

  @FindBy(id = "confirm-save-location")
  private lateinit var submitButton: FluentWebElement

  fun isAtMapLocationPageForAgency(agencyId: String): MapLocationPage {
    this.isAt(agencyId)

    return this
  }

  fun mapLocation(name: String, type: LocationType) {
    locationNameInput.fill().withText(name)
    locationTypeSelect.fillSelect().withValue(type.name)

    submitButton.click()
  }
}
