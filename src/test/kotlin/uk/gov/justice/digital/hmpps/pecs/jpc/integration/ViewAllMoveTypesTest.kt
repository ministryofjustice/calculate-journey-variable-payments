package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.ChooseSupplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Login
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MovesByType
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.cancelledMoveM60
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.lockoutMoveM40
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.longHaulMoveM30
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.multiMoveM50
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.redirectMoveM20
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.Month
import java.time.Year

internal class ViewAllMoveTypesTest : IntegrationTest() {

  @Test
  fun `go to a previous month and view all available move types`() {
    goToPage(Dashboard)

    isAtPage(Login).login()

    isAtPage(ChooseSupplier).choose(Supplier.GEOAMEY)

    isAtPage(Dashboard)
      .isAtMonthYear(LocalDate.now().month, Year.now())
      .navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor("dec 2020")

    listOf(
      standardMoveM4(),
      longHaulMoveM30(),
      redirectMoveM20(),
      lockoutMoveM40(),
      multiMoveM50(),
      cancelledMoveM60()
    ).forEach { move -> verifyDetailsOf(move) }
  }

  private fun verifyDetailsOf(move: Move) {
    goToPage(Dashboard)

    isAtPage(Dashboard)
      .isAtMonthYear(Month.DECEMBER, Year.of(2020))
      .navigateToMovesBy(move.moveType!!)

    isAtPage(MovesByType)
      .isAtPageFor(move.moveType!!)
      .navigateToDetailsFor(move)

    isAtPage(MoveDetails).isAtPageFor(move)
  }
}
