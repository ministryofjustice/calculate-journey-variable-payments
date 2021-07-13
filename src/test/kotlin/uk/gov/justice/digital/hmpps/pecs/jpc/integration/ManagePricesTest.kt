package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.AddPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneyResults
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneysForReview
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.UpdatePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.Month
import java.time.Year

@TestMethodOrder(OrderAnnotation::class)
internal class ManagePricesTest : IntegrationTest() {

  @Test
  @Order(1)
  fun `missing price is added for GEOAmey journey from Prison One to Prison Two`() {
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

    isAtPage(JourneysForReview).addPriceForJourney("PRISON1", "PRISON2")

    isAtPage(AddPrice)
      .isAtPricePageForJourney("PRISON1", "PRISON2")
      .addPriceForJourney("PRISON1", "PRISON2", Money(1000))

    isAtPage(JourneysForReview).isPriceAddedMessagePresent("PRISON ONE", "PRISON TWO", Money(1000))
  }

  @Test
  @Order(2)
  fun `price is updated for GEOAmey journey from Prison One to Prison Two`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.GEOAMEY)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

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
}
