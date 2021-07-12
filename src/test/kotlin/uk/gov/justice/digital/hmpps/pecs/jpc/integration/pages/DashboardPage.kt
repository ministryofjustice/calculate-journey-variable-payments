package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import com.gargoylesoftware.htmlunit.WebResponse
import org.assertj.core.api.Assertions.assertThat
import org.fluentlenium.core.annotation.PageUrl
import org.fluentlenium.core.domain.FluentWebElement
import org.openqa.selenium.support.FindBy
import java.time.Month
import java.time.Year

@PageUrl("http://localhost:8080/dashboard")
class DashboardPage : ApplicationPage() {

  @FindBy(linkText = "Jump to month")
  private lateinit var selectMonthLink: FluentWebElement

  @FindBy(linkText = "Manage Journey Price Catalogue")
  private lateinit var manageJourneyPriceCatalogueLink: FluentWebElement

  @FindBy(linkText = "Journeys for review")
  private lateinit var journeysForReviewLink: FluentWebElement

  @FindBy(linkText = "Manage Locations")
  private lateinit var manageLocationsLink: FluentWebElement

  @FindBy(linkText = "Find by move reference ID")
  private lateinit var findMoveByReferenceIdLink: FluentWebElement

  @FindBy(id = "month-year-heading")
  private lateinit var monthYear: FluentWebElement

  fun navigateToSelectMonthPage() {
    selectMonthLink.click()
  }

  fun isAtMonthYear(month: Month, year: Year): DashboardPage {
    assertThat(monthYear.text().uppercase()).isEqualTo("$month ${year.value}")

    return this
  }

  fun downloadAllMoves(): WebResponse = clickOnLinkText("Download all moves")

  fun navigateToJourneysForReview() {
    journeysForReviewLink.click()
  }

  fun navigateToManageJourneyPrice() {
    manageJourneyPriceCatalogueLink.click()
  }

  fun navigateToManageLocations() {
    manageLocationsLink.click()
  }

  fun navigateToFindMoveByReferenceId() {
    findMoveByReferenceIdLink.click()
  }
}
