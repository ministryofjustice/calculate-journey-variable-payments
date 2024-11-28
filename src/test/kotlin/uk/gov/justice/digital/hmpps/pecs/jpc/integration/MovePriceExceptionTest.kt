package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.titleCased
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType.STANDARD
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneyResults
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MovesByType
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.UpdatePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.standardMoveSM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.UpdatePricePage
import java.time.Year

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class MovePriceExceptionTest : IntegrationTest() {

  private val move = standardMoveSM4()

  private val date = move.updatedAt

  private val month = date.month

  private val year = Year.of(date.year)

  @Test
  @Order(1)
  fun `add a price exception and verify the move price matches the exception price`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(month, year)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(ManageJourneyPrice).findJourneyForPricing("PRISON ONE", "POLICE ONE")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "POLICE ONE")
      .isJourneyRowPresent("PRISON ONE", "POLICE ONE", Money.valueOf("100.00"))
      .navigateToUpdatePriceFor("PRISON1", "POLICE1")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("PRISON1", "POLICE1")
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .showPriceExceptionsTab()
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .addPriceException(month, Money.valueOf("3000.00"))
    wait.until {
      isAtPage(UpdatePrice)
        .assertTextIsPresent("Existing price exceptions")
        .isRowPresent<UpdatePricePage>(month.name.titleCased(), year.value, "£3000.00")
    }
    goToPage(Dashboard)

    isAtPage(Dashboard)
      .navigateToMovesBy(STANDARD)

    isAtPage(MovesByType)
      .isAtPageFor(STANDARD)
      .navigateToDetailsFor(move)

    isAtPage(MoveDetails)
      .isAtPageFor(move, Money.valueOf("3000.00"))
  }

  @Test
  @Order(2)
  fun `remove a price exception and verify the move price is back to its original price`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(month, year)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(ManageJourneyPrice).findJourneyForPricing("PRISON ONE", "POLICE ONE")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "POLICE ONE")
      // The price here is affected by the volume adjustment test, need to make them not affect each other
      .isJourneyRowPresent("PRISON ONE", "POLICE ONE", Money.valueOf("101.00"))
      .navigateToUpdatePriceFor("PRISON1", "POLICE1")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("PRISON1", "POLICE1")
      .assertTextIsPresent("Existing price exceptions")
      .isRowPresent<UpdatePricePage>(month.name.titleCased(), year.value, "£3000.00")
      .showPriceExceptionsTab()
      .removePriceException(month)
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")

    goToPage(Dashboard)

    isAtPage(Dashboard)
      .navigateToMovesBy(STANDARD)

    isAtPage(MovesByType)
      .isAtPageFor(STANDARD)
      .navigateToDetailsFor(move)

    isAtPage(MoveDetails)
      .isAtPageFor(move, Money.valueOf("101.00"))
  }
}
