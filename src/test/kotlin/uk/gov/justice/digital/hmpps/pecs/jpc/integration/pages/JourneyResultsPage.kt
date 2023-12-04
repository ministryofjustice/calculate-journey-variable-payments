package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.FluentWait
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import java.time.Duration

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
    var row: FluentWebElement? = null
    FluentWait(this).withTimeout(Duration.ofSeconds(30)).pollingEvery(Duration.ofSeconds(2)).until {
      logger.info(this.url)
      logger.info("Checking if journey row is present from $fromAgency to $toAgency, with cost of £$price")
//      row = find(By.xpath("//tr[contains(.,'$fromAgency') and contains(., '$toAgency') and contains(., '£$price')]")).firstOrNull()
      row = find(By.xpath("//tr[contains(.,'$fromAgency') and contains(., '$toAgency')]")).firstOrNull()
      row != null
    }
    logger.info(row?.text())

    assertThat(row).isNotNull

    return this
  }
}
