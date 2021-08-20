package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.openqa.selenium.By
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money

@PageUrl("http://localhost:8080/journeys-results?pick-up={fromAgency}&drop-off={toAgency}")
class JourneyResultsPage : ApplicationPage() {

  fun isAtResultsPageForJourney(fromAgency: String, toAgency: String): JourneyResultsPage {
    this.isAt(fromAgency.replace(" ", "%20"), toAgency.replace(" ", "%20"))

    return this
  }

  fun navigateToUpdatePriceFor(fromAgencyId: String, toAgencyId: String) {
    val element = this.find(By.xpath("//a[@href='/update-price/$fromAgencyId-$toAgencyId']")).firstOrNull()

    assertThat(element).isNotNull

    element?.click()
  }

  fun isPriceUpdatedMessagePresent(fromAgency: String, toAgency: String, price: Money): JourneyResultsPage {
    assertBannerIsPresent("Price updated", "Journey from $fromAgency to $toAgency priced at £$price")

    return this
  }

  fun isJourneyRowPresent(fromAgency: String, toAgency: String, price: Money): JourneyResultsPage {
    val row = this.find(By.xpath("//tr[contains(.,'$fromAgency') and contains(., '$toAgency') and contains(., '£$price')]")).firstOrNull()

    assertThat(row).isNotNull

    return this
  }
}
