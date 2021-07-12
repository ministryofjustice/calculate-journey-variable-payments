package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.fluentlenium.core.annotation.Page
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.JourneysForReviewPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.ManageLocationPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.MapLocationPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.CHOOSE_SUPPLIER
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.DASHBOARD
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JOURNEYS_FOR_REVIEW
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.LOGIN
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SEARCH_LOCATIONS
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SELECT_MONTH_YEAR
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.Month
import java.time.Year

@TestMethodOrder(OrderAnnotation::class)
internal class ManageLocationsTest : IntegrationTest() {

  @Page
  private lateinit var mapLocationPage: MapLocationPage

  @Page
  private lateinit var manageLocationPage: ManageLocationPage

  @Test
  @Order(1)
  fun `map missing location name and location type to agency id WYI`() {
    goToPage(DASHBOARD)

    isAtPage(LOGIN).login()

    isAtPage(CHOOSE_SUPPLIER).choose(Supplier.GEOAMEY)

    isAtPage(DASHBOARD)
      .isAtMonthYear(LocalDate.now().month, Year.now())
      .navigateToSelectMonthPage()

    isAtPage(SELECT_MONTH_YEAR).navigateToDashboardFor("dec 2020")

    isAtPage(DASHBOARD)
      .isAtMonthYear(Month.DECEMBER, Year.of(2020))
      .navigateToJourneysForReview()

    isAtPage(JOURNEYS_FOR_REVIEW).chooseLocationToMap("WYI")

    mapLocationPage
      .isAtMapLocationPageForAgency("WYI")
      .mapLocation("a prison", LocationType.PR)

    isAtPage(JOURNEYS_FOR_REVIEW)
      .isLocationUpdatedMessagePresent("WYI", "A PRISON")
      .isRowPresent<JourneysForReviewPage>("A PRISON", LocationType.PR.name)
  }

  @Test
  @Order(2)
  fun `update location name and type for agency id WYI`() {
    goToPage(DASHBOARD)

    isAtPage(LOGIN).login()

    isAtPage(CHOOSE_SUPPLIER).choose(Supplier.GEOAMEY)

    isAtPage(DASHBOARD).navigateToManageLocations()

    isAtPage(SEARCH_LOCATIONS).searchForLocation("A PRISON")

    manageLocationPage
      .isAtManageLocationPageForAgency("WYI")
      .updateLocation("A POLICE STATION", LocationType.PS)

    isAtPage(SEARCH_LOCATIONS).isLocationUpdatedMessagePresent("WYI", "A POLICE STATION")
  }
}
