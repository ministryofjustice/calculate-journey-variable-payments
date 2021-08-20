package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType

@PageUrl("http://localhost:8080/manage-location/{agencyId}")
class ManageLocationPage : ApplicationPage() {

  @FindBy(id = "location-name")
  private lateinit var locationName: FluentWebElement

  @FindBy(id = "location-type-id")
  private lateinit var locationType: FluentWebElement

  @FindBy(id = "confirm-save-location")
  private lateinit var submit: FluentWebElement

  fun isAtManageLocationPageForAgency(agencyId: String): ManageLocationPage {
    this.isAt(agencyId)

    return this
  }

  fun updateLocation(name: String, type: LocationType) {
    locationName.fill().withText(name)
    locationType.fillSelect().withValue(type.name)

    submit.submit()
  }
}
