package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.ChooseSupplierPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.DashboardPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.Dec2020MoveData.cancelledMoveM60
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.Dec2020MoveData.lockoutMoveM40
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.Dec2020MoveData.longHaulMoveM30
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.Dec2020MoveData.multiMoveM50
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.Dec2020MoveData.redirectMoveM20
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.Dec2020MoveData.standardMoveM4
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.FindMovePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.LoginPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.MovesByTypePage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SelectMonthPage
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SercoPreviousMonthMoveData.cancelledMoveCM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SercoPreviousMonthMoveData.lockoutMoveLM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SercoPreviousMonthMoveData.longHaulMoveLHM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SercoPreviousMonthMoveData.multiMoveMM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SercoPreviousMonthMoveData.redirectMoveRM1
import uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages.SercoPreviousMonthMoveData.standardMoveSM1
import java.time.LocalDate

internal class ViewAllMoveTypesTest : PlayWrightTest() {

  @Test
  fun `view one of each move type for Serco in previous month`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val selectMonth = SelectMonthPage(page)
    val movesByTypePage = MovesByTypePage(page)
    val findMovePage = FindMovePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    supplierPage.isPageSuccessful()
    supplierPage.goToSercoDashboard()
    selectMonth.gotToPage()
    val date = LocalDate.now()
    selectMonth.goToMonth(date.minusMonths(1))

    listOf(
      standardMoveSM1(),
      redirectMoveRM1(),
      longHaulMoveLHM1(),
      lockoutMoveLM1(),
      multiMoveMM1(),
      cancelledMoveCM1(),
    ).forEach { m ->
      run {
        dashboardPage.gotToPage()
        dashboardPage.navigateToMovesBy(m.moveType!!.label)
        movesByTypePage.clickMoveByReference(m.reference)
        findMovePage.assertPageShowsTheMove(m)
      }
    }
  }

  // @Test
  fun `view one of each move type for GEOAmey in Dec 2020`() {
    val loginPage = LoginPage(page)
    val supplierPage = ChooseSupplierPage(page)
    val dashboardPage = DashboardPage(page)
    val selectMonth = SelectMonthPage(page)
    val movesByTypePage = MovesByTypePage(page)
    val findMovePage = FindMovePage(page)

    loginPage.login()
    supplierPage.gotToPage()
    supplierPage.isPageSuccessful()
    supplierPage.goToGeoameyDashboard()
    selectMonth.gotToPage()
    selectMonth.goToMonth(LocalDate.of(2020, 12, 1))

    listOf(
      standardMoveM4(),
      longHaulMoveM30(),
      redirectMoveM20(),
      lockoutMoveM40(),
      multiMoveM50(),
      cancelledMoveM60(),
    ).forEach { m ->
      run {
        dashboardPage.gotToPage()
        dashboardPage.navigateToMovesBy(m.moveType!!.label)
        movesByTypePage.clickMoveByReference(m.reference)
        findMovePage.assertPageShowsTheMove(m)
      }
    }
  }
}
