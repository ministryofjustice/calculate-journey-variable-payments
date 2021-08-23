package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.JourneysForReviewPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneysForReview
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MapLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SearchLocations
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import java.time.LocalDate
import java.time.Month
import java.time.Year

@TestMethodOrder(OrderAnnotation::class)
internal class ManageLocationsTest : IntegrationTest() {

  @Test
  @Order(1)
  fun `map missing location name and location type to agency id WYI`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.GEOAMEY)

    isAtPage(Dashboard)
      .isAtMonthYear(LocalDate.now().month, Year.now())
      .navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor("dec 2020")

    isAtPage(Dashboard)
      .isAtMonthYear(Month.DECEMBER, Year.of(2020))
      .navigateToJourneysForReview()

    isAtPage(JourneysForReview).chooseLocationToMap("WYI")

    isAtPage(MapLocation)
      .isAtMapLocationPageForAgency("WYI")
      .mapLocation("a prison", LocationType.PR)

    isAtPage(JourneysForReview)
      .isLocationUpdatedMessagePresent("WYI", "A PRISON")
      .isRowPresent<JourneysForReviewPage>("A PRISON", LocationType.PR.name)
  }

  @Test
  @Order(2)
  fun `update location name and type for agency id WYI`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.GEOAMEY)

    isAtPage(Dashboard).navigateToManageLocations()

    isAtPage(SearchLocations).searchForLocation("A PRISON")

    isAtPage(ManageLocation)
      .isAtManageLocationPageForAgency("WYI")
      .updateLocation("A POLICE STATION", LocationType.PS)

    isAtPage(SearchLocations).isLocationUpdatedMessagePresent("WYI", "A POLICE STATION")
  }
}
