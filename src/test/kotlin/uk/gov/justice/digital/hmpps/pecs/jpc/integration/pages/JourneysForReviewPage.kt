package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.openqa.selenium.By
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money

@PageUrl("http://localhost:8080/journeys")
class JourneysForReviewPage : ApplicationPage() {

  fun addPriceForJourney(fromAgencyId: String, toAgencyId: String) {
    val element = this.find(By.xpath("//a[@href='/add-price/$fromAgencyId-$toAgencyId']")).firstOrNull()

    assertThat(element).isNotNull

    element?.click()
  }

  fun isPriceAddedMessagePresent(fromAgency: String, toAgency: String, price: Money) {
    assertTextIsPresent("Price added")
    assertTextIsPresent("Journey from $fromAgency to $toAgency priced at £$price")
  }

  private fun assertTextIsPresent(text: String) {
    find(By.xpath("//p[normalize-space(text())='$text']")).firstOrNull().let { assertThat(it).isNotNull }
  }
}
