package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.JourneysForReviewPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.previousMonth
import java.time.LocalDate
import java.time.Year

/**
 * Management of prices and locations are combined due the need to control the order in which things are done.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class ManageLocationsAndPricesTest : IntegrationTest() {

  private val currentDate = LocalDate.now()

  private val year = Year.now()

  @Test
  @Order(1)
  fun `missing price is added for Serco journey from Prison One to Prison Two`() {
    goToPage(Pages.Dashboard)

    isAtPage(Pages.Login).login()

    isAtPage(Pages.ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Pages.Dashboard)
      .isAtMonthYear(currentDate.month, Year.now())
      .navigateToSelectMonthPage()

    isAtPage(Pages.SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), year)

    isAtPage(Pages.Dashboard)
      .isAtMonthYear(currentDate.previousMonth(), year)
      .navigateToJourneysForReview()

    isAtPage(Pages.JourneysForReview).addPriceForJourney("PRISON1", "PRISON2")

    isAtPage(Pages.AddPrice)
      .isAtPricePageForJourney("PRISON1", "PRISON2")
      .addPriceForJourney("PRISON1", "PRISON2", Money(1000))

    isAtPage(Pages.JourneysForReview).isPriceAddedMessagePresent("PRISON ONE", "PRISON TWO", Money(1000))
  }

  @Test
  @Order(2)
  fun `price is updated for Serco journey from Prison One to Prison Two`() {
    goToPage(Pages.Dashboard)

    isAtPage(Pages.Login).login()

    isAtPage(Pages.ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Pages.Dashboard).navigateToSelectMonthPage()

    isAtPage(Pages.SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), year)

    isAtPage(Pages.Dashboard).navigateToManageJourneyPrice()

    isAtPage(Pages.ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(Pages.ManageJourneyPrice).findJourneyForPricing("PRISON ONE", "PRISON TWO")

    isAtPage(Pages.JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "PRISON TWO")
      .isJourneyRowPresent("PRISON ONE", "PRISON TWO", Money(1000))
      .navigateToUpdatePriceFor("PRISON1", "PRISON2")

    isAtPage(Pages.UpdatePrice)
      .isAtPricePageForJourney("PRISON1", "PRISON2")
      .updatePriceForJourney("PRISON1", "PRISON2", Money(2000))

    isAtPage(Pages.JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "PRISON TWO")
      .isPriceUpdatedMessagePresent("PRISON ONE", "PRISON TWO", Money(2000))
      .isJourneyRowPresent("PRISON ONE", "PRISON TWO", Money(2000))
  }

  @Test
  @Order(3)
  fun `map missing location name and location type to agency id STOPOVER_AGENCY`() {
    goToPage(Pages.Dashboard)

    isAtPage(Pages.Login).login()

    isAtPage(Pages.ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Pages.Dashboard)
      .isAtMonthYear(currentDate.month, year)
      .navigateToSelectMonthPage()

    isAtPage(Pages.SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), year)

    isAtPage(Pages.Dashboard)
      .isAtMonthYear(currentDate.previousMonth(), year)
      .navigateToJourneysForReview()

    isAtPage(Pages.JourneysForReview)
      .isJourneysPresentInOrder(
        Journey("FROM_AGENCY", "LOCKOUT_AGENCY"),
        Journey("FROM_AGENCY", "STOPOVER_AGENCY"),
        Journey("FROM_AGENCY", "TO_AGENCY"),
        Journey("FROM_AGENCY", "TO_AGENCY2"),
        Journey("FROM_AGENCY2", "TO_AGENCY3"),
        Journey("FROM_AGENCY2", "TO_AGENCY4"),
        Journey("LOCKOUT_AGENCY", "TO_AGENCY"),
        Journey("STOPOVER_AGENCY", "TO_AGENCY")
      )
      .chooseLocationToMap("STOPOVER_AGENCY")

    isAtPage(Pages.MapLocation)
      .isAtMapLocationPageForAgency("STOPOVER_AGENCY")
      .mapLocation("STOP OVER", LocationType.PR)

    isAtPage(Pages.JourneysForReview)
      .isLocationUpdatedMessagePresent("STOPOVER_AGENCY", "STOP OVER")
      .isRowPresent<JourneysForReviewPage>("STOP OVER", LocationType.PR.name)
  }

  @Test
  @Order(4)
  fun `update location name and type for agency id STOPOVER_AGENCY`() {
    goToPage(Pages.Dashboard)

    isAtPage(Pages.Login).login()

    isAtPage(Pages.ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Pages.Dashboard).navigateToManageLocations()

    isAtPage(Pages.SearchLocations).searchForLocation("STOP OVER")

    isAtPage(Pages.ManageLocation)
      .isAtManageLocationPageForAgency("STOPOVER_AGENCY")
      .updateLocation("STOP OVER AGENCY", LocationType.PS)

    isAtPage(Pages.SearchLocations).isLocationUpdatedMessagePresent("STOPOVER_AGENCY", "STOP OVER AGENCY")
  }
}
