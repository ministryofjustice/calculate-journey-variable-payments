package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.ClosedRangeLocalDate

internal class LongHaulMovesSheetTest {
  private val journey1 = journeyJ1()
  private val journey2 = journeyJ1(journeyId = "J2")
  private val move =
    moveM1(journeys = listOf(journey1, journey2)).copy(moveType = MoveType.REDIRECTION, notes = "long haul")
  private val longHaulSheet = LongHaulMovesSheet(
    SXSSFWorkbook(),
    PriceSheet.Header(
      defaultMoveDate10Sep2020,
      ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
      Supplier.SERCO
    )
  )

  @Test
  internal fun `long haul moves`() {
    longHaulSheet.writeMoves(listOf(move))

    assertOnSheetName(longHaulSheet, "Long haul")
    assertOnSubheading(longHaulSheet, "LONG HAUL")
    assertOnColumnDataHeadings(
      longHaulSheet,
      "Move ID",
      "Pick up",
      "Location Type",
      "Drop off",
      "Location Type",
      "Pick up date",
      "Pick up time",
      "Drop off date",
      "Drop off time",
      "Vehicle reg",
      "NOMIS prison ID",
      "Price",
      "Contractor billable?",
      "Notes"
    )
    assertOnMove()
    assertOnJourneys()
  }

  private fun assertOnMove() {
    assertCellEquals(longHaulSheet, 9, 0, move.reference)
    assertCellEquals(longHaulSheet, 9, 1, move.fromSiteName)
    assertCellEquals(longHaulSheet, 9, 2, move.fromLocationType?.name)
    assertCellEquals(longHaulSheet, 9, 3, move.toSiteName)
    assertCellEquals(longHaulSheet, 9, 4, move.toLocationType?.name)
    assertCellEquals(longHaulSheet, 9, 5, move.pickUpDateTime?.toLocalDate())
    assertCellEquals(longHaulSheet, 9, 6, move.pickUpDateTime?.toLocalTime())
    assertCellEquals(longHaulSheet, 9, 7, move.dropOffOrCancelledDateTime?.toLocalDate())
    assertCellEquals(longHaulSheet, 9, 8, move.dropOffOrCancelledDateTime?.toLocalTime())
    assertCellEquals(longHaulSheet, 9, 9, move.registration())
    assertCellEquals(longHaulSheet, 9, 10, move.person?.prisonNumber)
    assertCellEquals(longHaulSheet, 9, 11, move.totalInPounds())
    assertCellEquals(longHaulSheet, 9, 12, "")
    assertCellEquals(longHaulSheet, 9, 13, move.notes)
  }

  private fun assertOnJourneys() {
    var row = 10

    listOf(journey1, journey2).forEachIndexed { index, journey ->
      assertCellEquals(longHaulSheet, row, 0, "Journey ${index + 1}")
      assertCellEquals(longHaulSheet, row, 1, journey.fromSiteName)
      assertCellEquals(longHaulSheet, row, 2, journey.fromLocationType?.name)
      assertCellEquals(longHaulSheet, row, 3, journey.toSiteName)
      assertCellEquals(longHaulSheet, row, 4, journey.toLocationType?.name)
      assertCellEquals(longHaulSheet, row, 5, journey.pickUpDateTime?.toLocalDate())
      assertCellEquals(longHaulSheet, row, 6, journey.pickUpDateTime?.toLocalTime())
      assertCellEquals(longHaulSheet, row, 7, journey.dropOffDateTime?.toLocalDate())
      assertCellEquals(longHaulSheet, row, 8, journey.dropOffDateTime?.toLocalTime())
      assertCellEquals(longHaulSheet, row, 9, journey.vehicleRegistration)
      assertCellEquals(longHaulSheet, row, 10, "")
      assertCellEquals(longHaulSheet, row, 11, journey.priceInPounds())
      assertCellEquals(longHaulSheet, row, 12, journey.isBillable())
      assertCellEquals(longHaulSheet, row, 13, journey.notes)
      row++
    }
  }
}
