package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.service.moves.MoveTypeSummaries
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange

internal class MovesSummarySheetTest {

  private val standardSummary = MovesSummary(MoveType.STANDARD, 10.0, 2, 1, 200)
  private val longHaulSummary = MovesSummary(MoveType.LONG_HAUL, 50.0, 10, 10, 400)
  private val redirectionSummary = MovesSummary(MoveType.REDIRECTION, 20.0, 4, 2, 300)
  private val lockoutSummary = MovesSummary(MoveType.LOCKOUT, 5.0, 1, 0, 100)
  private val multiTypeSummary = MovesSummary(MoveType.MULTI, 5.0, 1, 1, 0)
  private val cancelledSummary = MovesSummary(MoveType.CANCELLED, 10.0, 2, 1, 200)

  private val summaries = MoveTypeSummaries(
    1,
    listOf(standardSummary, longHaulSummary, redirectionSummary, lockoutSummary, multiTypeSummary, cancelledSummary)
  )
  private val summarySheet = SummarySheet(
    SXSSFWorkbook(),
    PriceSheet.Header(
      defaultMoveDate10Sep2020,
      DateRange(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
      Supplier.SERCO
    )
  )

  @Test
  internal fun `summary prices`() {
    summarySheet.writeSummaries(summaries)

    assertOnSheetName(summarySheet, "Summary")
    assertOnSubheading(summarySheet, "OUTPUT SUMMARY")
    assertOnMoveSummary(standardSummary, 9)
    assertOnMoveSummary(longHaulSummary, 12)
    assertOnMoveSummary(redirectionSummary, 15)
    assertOnMoveSummary(lockoutSummary, 18)
    assertOnMoveSummary(multiTypeSummary, 21)
    assertOnMoveSummary(cancelledSummary, 24)

    assertOnSummaryOfSummaries()
  }

  private fun assertOnMoveSummary(movesSummary: MovesSummary, startAtRowIndex: Int) {
    assertCellEquals(summarySheet, startAtRowIndex, 0, movesSummary.moveType?.label)
    assertCellEquals(summarySheet, startAtRowIndex, 1, movesSummary.percentage)
    assertCellEquals(summarySheet, startAtRowIndex, 2, movesSummary.volume)
    assertCellEquals(summarySheet, startAtRowIndex, 3, movesSummary.volumeUnpriced)
    assertCellEquals(summarySheet, startAtRowIndex, 4, movesSummary.totalPriceInPounds)
    assertCellEquals(summarySheet, startAtRowIndex + 1, 0, movesSummary.moveType?.description)
  }

  private fun assertOnSummaryOfSummaries() {
    assertCellEquals(summarySheet, 27, 1, "Total %")
    assertCellEquals(summarySheet, 27, 2, "Total volume")
    assertCellEquals(summarySheet, 27, 3, "Without prices")
    assertCellEquals(summarySheet, 27, 4, "Total price")
    assertCellEquals(summarySheet, 28, 1, 100.0) // overall %
    assertCellEquals(summarySheet, 28, 2, 20.0) // overall volume
    assertCellEquals(summarySheet, 28, 3, 15.0) // overall volume unpriced
    assertCellEquals(summarySheet, 28, 4, 12.0) // overall total in £
  }
}
