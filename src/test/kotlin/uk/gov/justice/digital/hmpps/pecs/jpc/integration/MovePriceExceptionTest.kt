package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType.STANDARD
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.JourneyResults
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ManageJourneyPriceCatalogue
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MovesByType
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.UpdatePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.standardMoveSM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.UpdatePricePage
import java.time.LocalDate
import java.time.Year

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class MovePriceExceptionTest : IntegrationTest() {

  private val currentDate = LocalDate.now()

  private val date = currentDate.minusMonths(2)

  private val year = Year.of(date.year)

  @Test
  @Order(1)
  fun `add a price exception and verify the move price matches the exception price`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(date.month, year)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(ManageJourneyPrice).findJourneyForPricing("PRISON ONE", "POLICE ONE")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "POLICE ONE")
      .isJourneyRowPresent("PRISON ONE", "POLICE ONE", Money.valueOf(100.00))
      .navigateToUpdatePriceFor("PRISON1", "POLICE1")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("PRISON1", "POLICE1")
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .showPriceExceptionsTab()
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .addPriceException(date.month, Money.valueOf(3000.00))

    goToPage(Dashboard)

    isAtPage(Dashboard)
      .navigateToMovesBy(STANDARD)

    isAtPage(MovesByType)
      .isAtPageFor(STANDARD)
      .navigateToDetailsFor(standardMoveSM4())

    isAtPage(MoveDetails)
      .isAtPageFor(standardMoveSM4(), Money.valueOf(3000.00))
  }

  @Test
  @Order(2)
  fun `remove a price exception and verify the move price is back to its original price`() {
    goToPage(Dashboard)

    isAtPage(Login)
      .login()

    isAtPage(ChooseSupplier).choose(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(date.month, year)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(ManageJourneyPrice).findJourneyForPricing("PRISON ONE", "POLICE ONE")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE", "POLICE ONE")
      .isJourneyRowPresent("PRISON ONE", "POLICE ONE", Money.valueOf(100.00))
      .navigateToUpdatePriceFor("PRISON1", "POLICE1")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("PRISON1", "POLICE1")
      .assertTextIsPresent("Existing price exceptions")
      .isRowPresent<UpdatePricePage>("August", "£3000.00")
      .showPriceExceptionsTab()
      .removePriceException(date.month)
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")

    goToPage(Dashboard)

    isAtPage(Dashboard)
      .navigateToMovesBy(STANDARD)

    isAtPage(MovesByType)
      .isAtPageFor(STANDARD)
      .navigateToDetailsFor(standardMoveSM4())

    isAtPage(MoveDetails)
      .isAtPageFor(standardMoveSM4(), Money.valueOf(100.00))
  }
}
