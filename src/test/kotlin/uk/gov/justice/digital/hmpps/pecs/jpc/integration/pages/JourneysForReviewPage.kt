package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.openqa.selenium.By
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money

@PageUrl("http://localhost:8080/journeys")
class JourneysForReviewPage : ApplicationPage() {

  fun addPriceForJourney(fromAgencyId: String, toAgencyId: String) {
    val element = this.find(By.xpath("//a[@href='/add-price/$fromAgencyId-$toAgencyId']")).firstOrNull()

    assertThat(element).isNotNull

    element?.click()
  }

  fun chooseLocationToMap(agencyId: String) {
    val element = this.find(By.xpath("//a[@href='/map-location/$agencyId']")).firstOrNull()

    assertThat(element).isNotNull

    element?.click()
  }

  fun isPriceAddedMessagePresent(fromAgency: String, toAgency: String, price: Money) {
    assertBannerIsPresent("Price added", "Journey from $fromAgency to $toAgency priced at £$price")
  }

  fun isLocationUpdatedMessagePresent(agencyId: String, locationName: String): JourneysForReviewPage {
    assertBannerIsPresent("Location updated", "$agencyId successfully mapped to $locationName")

    return this
  }

  fun isJourneysPresentInOrder(vararg journeys: Journey): JourneysForReviewPage {
    journeys.forEachIndexed { index, journey ->
      isRowPresentAtIndex<JourneysForReviewPage>(
        "journeys",
        journey.from,
        journey.to,
        index = index + 1
      )
    }

    return this
  }
}

data class Journey(val from: String, val to: String)
