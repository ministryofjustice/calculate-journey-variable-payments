package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import uk.gov.justice.digital.hmpps.pecs.jpc.controller.titleCased
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType.LONG_HAUL
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
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.lodgingMoveLDGM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.UpdatePricePage
import java.time.Year

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class LodgingPriceTest : IntegrationTest() {

  private val move = lodgingMoveLDGM1()

  private val date = move.updatedAt

  private val month = date.month

  private val year = Year.of(date.year)

  @Test
  @Order(1)
  fun `add price exceptions and verify the move price matches the exception prices`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard).navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(month, year)

    isAtPage(Dashboard).navigateToManageJourneyPrice()

    isAtPage(ManageJourneyPriceCatalogue).navigateToFindJourneys()

    isAtPage(ManageJourneyPrice).findJourneyForPricing("PRISON ONE L", "POLICE ONE L")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("PRISON ONE L", "POLICE ONE L")
      .isJourneyRowPresent("PRISON ONE L", "POLICE ONE L", Money.valueOf("15.99"))
      .navigateToUpdatePriceFor("PRISON1L", "POLICE1L")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("PRISON1L", "POLICE1L")
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .showPriceExceptionsTab()
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .addPriceException(date.month, Money.valueOf("3000.00"))
    wait.until {
      isAtPage(UpdatePrice)
        .assertTextIsPresent("Existing price exceptions")
        .isRowPresent<UpdatePricePage>(month.name.titleCased(), year.value, "£3000.00")
    }
    goToPage(ManageJourneyPrice)

    isAtPage(ManageJourneyPrice).findJourneyForPricing("POLICE ONE L", "POLICE TWO L")

    isAtPage(JourneyResults)
      .isAtResultsPageForJourney("POLICE ONE L", "POLICE TWO L")
      .isJourneyRowPresent("POLICE ONE L", "POLICE TWO L", Money.valueOf("18.75"))
      .navigateToUpdatePriceFor("POLICE1L", "POLICE2L")

    isAtPage(UpdatePrice)
      .isAtPricePageForJourney("POLICE1L", "POLICE2L")
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .showPriceExceptionsTab()
      .assertTextIsNotPresent<UpdatePricePage>("Existing price exceptions")
      .addPriceException(date.month, Money.valueOf("2.00"))
    wait.until {
      isAtPage(UpdatePrice)
        .assertTextIsPresent("Existing price exceptions")
        .isRowPresent<UpdatePricePage>(month.name.titleCased(), year.value, "£2.00")
    }
    goToPage(Dashboard)

    isAtPage(Dashboard)
      .navigateToMovesBy(LONG_HAUL)

    isAtPage(MovesByType)
      .isAtPageFor(LONG_HAUL)
      .navigateToDetailsFor(move)

    isAtPage(MoveDetails)
      .isAtPageFor(move, Money.valueOf("3002.00"))
  }
}
