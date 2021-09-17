package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.JourneysForReviewPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneysForReview
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MapLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SearchLocations
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.previousMonth
import java.time.LocalDate
import java.time.Year

@TestMethodOrder(OrderAnnotation::class)
internal class ManageLocationsTest : IntegrationTest() {

  private val currentDate = LocalDate.now()

  private val year = Year.now()

  @Test
  @Order(1)
  fun `map missing location name and location type to agency id STOPOVER_AGENCY`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Dashboard)
      .isAtMonthYear(currentDate.month, year)
      .navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), year)

    isAtPage(Dashboard)
      .isAtMonthYear(currentDate.previousMonth(), year)
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
        Journey("STOPOVER_AGENCY", "TO_AGENCY")
      )
      .chooseLocationToMap("STOPOVER_AGENCY")

    isAtPage(MapLocation)
      .isAtMapLocationPageForAgency("STOPOVER_AGENCY")
      .mapLocation("STOP OVER", LocationType.PR)

    isAtPage(JourneysForReview)
      .isLocationUpdatedMessagePresent("STOPOVER_AGENCY", "STOP OVER")
      .isRowPresent<JourneysForReviewPage>("STOP OVER", LocationType.PR.name)
  }

  @Test
  @Order(2)
  fun `update location name and type for agency id STOPOVER_AGENCY`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Dashboard).navigateToManageLocations()

    isAtPage(SearchLocations).searchForLocation("STOP OVER")

    isAtPage(ManageLocation)
      .isAtManageLocationPageForAgency("STOPOVER_AGENCY")
      .updateLocation("STOP OVER AGENCY", LocationType.PS)

    isAtPage(SearchLocations).isLocationUpdatedMessagePresent("STOPOVER_AGENCY", "STOP OVER AGENCY")
  }
}
