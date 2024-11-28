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
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.AddPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneyResults
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneysForReview
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MapLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SearchLocations
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.UpdatePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.UpdatePricePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.previousMonth
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.previousMonthYear
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
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard)
      .isAtMonthYear(currentDate.month, Year.now())
      .navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), currentDate.previousMonthYear())

    isAtPage(Dashboard)
      .isAtMonthYear(currentDate.previousMonth(), currentDate.previousMonthYear())
      .navigateToJourneysForReview()

    isAtPage(JourneysForReview).addPriceForJourney("PRISON1", "PRISON2")
    wait.until {
      isAtPage(AddPrice)
        .isAtPricePageForJourney("PRISON1", "PRISON2")
        .addPriceForJourney("PRISON1", "PRISON2", Money(1000))
    }
    wait.until {
      isAtPage(JourneysForReview).isPriceAddedMessagePresent("PRISON ONE", "PRISON TWO", Money(1000))
    }
  }

  @Test
  @Order(2)
  fun `price is updated for Serco journey from Prison One to Prison Two`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), currentDate.previousMonthYear())

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(ManageJourneyPrice).findJourneyForPricing("PRISON ONE", "PRISON TWO")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "PRISON TWO")
      .isJourneyRowPresent("PRISON ONE", "PRISON TWO", Money(1000))
      .navigateToUpdatePriceFor("PRISON1", "PRISON2")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("PRISON1", "PRISON2")
      .updatePriceForJourney("PRISON1", "PRISON2", Money(2000))

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "PRISON TWO")
      .isPriceUpdatedMessagePresent("PRISON ONE", "PRISON TWO", Money(2000))
      .isJourneyRowPresent("PRISON ONE", "PRISON TWO", Money(2000))
  }

  @Test
  @Order(3)
  fun `price book cannot be changed with journey two years old or more missing price`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), currentDate.previousMonthYear().minusYears(2))

    isAtPage(Dashboard).navigateToJourneysForReview()

    isAtPage(JourneysForReview)
      .isRowPresent<JourneysForReviewPage>("PRISON ONE", "PR", "PRISON TWO", "PR", 1, "Not priced")
      .assertTextIsNotPresent<JourneysForReviewPage>("Add price")
  }

  @Test
  @Order(4)
  fun `price book cannot be changed when journey price two years old or more`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), currentDate.previousMonthYear().minusYears(2))

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(ManageJourneyPrice).findJourneyForPricing("PRISON ONE", "POLICE ONE")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "POLICE ONE")
      .isJourneyRowPresent("PRISON ONE", "POLICE ONE", Money.valueOf("50.00"))
      .navigateToUpdatePriceFor("PRISON1", "POLICE1")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("PRISON1", "POLICE1")
      .assertTextIsNotPresent<UpdatePricePage>("Update price")
      .assertTextIsNotPresent<UpdatePricePage>("Price exceptions")
      .assertTextIsPresent("Price history")
  }

  @Test
  @Order(5)
  fun `map missing location name and location type to agency id STOPOVER_AGENCY`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard)
      .isAtMonthYear(currentDate.month, year)
      .navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), currentDate.previousMonthYear())

    isAtPage(Dashboard)
      .isAtMonthYear(currentDate.previousMonth(), currentDate.previousMonthYear())
      .navigateToJourneysForReview()

    isAtPage(JourneysForReview)
      .isJourneysPresentInOrder(
        Journey("FROM_AGENCY", "LOCKOUT_AGENCY"),
        Journey("FROM_AGENCY", "STOPOVER_AGENCY"),
        Journey("FROM_AGENCY", "TO_AGENCY"),
        Journey("FROM_AGENCY", "TO_AGENCY2"),
        Journey("FROM_AGENCY2", "TO_AGENCY3"),
        Journey("FROM_AGENCY2", "TO_AGENCY4"),
        Journey("LOCKOUT_AGENCY", "TO_AGENCY"),
        Journey("STOPOVER_AGENCY", "TO_AGENCY"),
      )
      .chooseLocationToMap("STOPOVER_AGENCY")

    isAtPage(MapLocation)
      .isAtMapLocationPageForAgency("STOPOVER_AGENCY")
      .mapLocation("STOP OVER", LocationType.PR)
    wait.until {
      isAtPage(JourneysForReview)
        .isLocationUpdatedMessagePresent("STOPOVER_AGENCY", "STOP OVER")
        .isRowPresent<JourneysForReviewPage>("STOP OVER", LocationType.PR.name)
      }
  }

  @Test
  @Order(6)
  fun `update location name and type for agency id STOPOVER_AGENCY`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToManageLocations()

    isAtPage(SearchLocations).searchForLocation("STOP OVER")

    isAtPage(ManageLocation)
      .isAtManageLocationPageForAgency("STOPOVER_AGENCY")
      .updateLocation("STOP OVER AGENCY", LocationType.PS)
    wait.until {
      isAtPage(SearchLocations).isLocationUpdatedMessagePresent("STOPOVER_AGENCY", "STOP OVER AGENCY")
    }
  }
}
