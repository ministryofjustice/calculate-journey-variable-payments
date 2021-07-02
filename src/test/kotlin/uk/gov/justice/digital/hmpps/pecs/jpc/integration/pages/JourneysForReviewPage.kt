package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.openqa.selenium.By

@PageUrl("http://localhost:8080/journeys")
class JourneysForReviewPage : ApplicationPage() {

  fun addPriceForJourney(fromAgencyId: String, toAgencyId: String) {
    val element = this.find(By.xpath("//a[@href='/add-price/$fromAgencyId-$toAgencyId']")).firstOrNull()

    assertThat(element).isNotNull

    element?.click()
  }
}
