package uk.gov.justice.digital.hmpps.pecs.jpc.integration

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.cancelledMoveM60
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.lockoutMoveM40
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.longHaulMoveM30
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.multiMoveM50
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.redirectMoveM20
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Dec2020MoveData.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.Dashboard
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MoveDetails
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.MovesByType
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.Pages.SelectMonthYear
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.cancelledMoveCM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.lockoutMoveLM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.longHaulMoveLHM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.multiMoveMM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.redirectMoveRM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.SercoPreviousMonthMoveData.standardMoveSM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.previousMonth
import uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages.previousMonthYear
import java.io.File
import java.lang.Error
import java.time.LocalDate
import java.time.Month
import java.time.Year

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class ViewAllMoveTypesTest : IntegrationTest() {

  private val currentDate = LocalDate.now()

  private val year = Year.now()

  @Test
  @Order(1)
  fun `view one of each move type for Serco in previous month`() {
    loginAndGotoDashboardFor(Supplier.SERCO)

    isAtPage(Dashboard)
      .isAtMonthYear(currentDate.month, year)
      .navigateToSelectMonthPage()

    isAtPage(SelectMonthYear).navigateToDashboardFor(currentDate.previousMonth(), currentDate.previousMonthYear())

    listOf(
      standardMoveSM1(),
      redirectMoveRM1(),
      longHaulMoveLHM1(),
      lockoutMoveLM1(),
      multiMoveMM1(),
      cancelledMoveCM1(),
    ).forEach { move -> verifyDetailsOf(move, currentDate.previousMonth(), currentDate.previousMonthYear()) }
  }

  @Test
  @Order(2)
  fun `view one of each move type for GEOAmey in Dec 2020`() {
    try {
      loginAndGotoDashboardFor(Supplier.GEOAMEY)

      isAtPage(Dashboard)
        .isAtMonthYear(currentDate.month, year)
        .navigateToSelectMonthPage()

      isAtPage(SelectMonthYear).navigateToDashboardFor(Month.DECEMBER, Year.of(2020))

      listOf(
        standardMoveM4(),
        longHaulMoveM30(),
        redirectMoveM20(),
        lockoutMoveM40(),
        multiMoveM50(),
        cancelledMoveM60(),
      ).forEach { move -> verifyDetailsOf(move, Month.DECEMBER, Year.of(2020)) }
    } catch (e: Error) {
      val scrFile: File = (driver as TakesScreenshot).getScreenshotAs(OutputType.FILE)
      FileUtils.copyFile(
        scrFile,
        File(imageLocation + "view-one-of-each-move-type-for-GEOAmey-in-Dec-2020.jpg"),
      )
      throw e
    }
  }

  private fun verifyDetailsOf(move: Move, month: Month, year: Year) {
    goToPage(Dashboard)

    isAtPage(Dashboard)
      .isAtMonthYear(month, year)
      .navigateToMovesBy(move.moveType!!)

    isAtPage(MovesByType)
      .isAtPageFor(move.moveType!!)
      .navigateToDetailsFor(move)

    isAtPage(MoveDetails).isAtPageFor(move)
  }
}
