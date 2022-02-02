package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.ClosedRangeLocalDate

internal class LockoutMovesSheetTest {
  private val journey1 = journeyJ1()
  private val journey2 = journeyJ1(journeyId = "J2", vehicleRegistration = "REG200")
  private val move = moveM1(journeys = listOf(journey1, journey2)).copy(moveType = MoveType.LOCKOUT, notes = "lockout")
  private val lockoutMovesSheet = LockoutMovesSheet(
    SXSSFWorkbook(),
    PriceSheet.Header(
      defaultMoveDate10Sep2020,
      ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
      Supplier.SERCO
    )
  )

  @Test
  internal fun `lockout moves`() {
    lockoutMovesSheet.writeMoves(listOf(move))

    assertOnSheetName(lockoutMovesSheet, "Lockouts")
    assertOnSubheading(lockoutMovesSheet, "LOCKOUTS (refused admission to prison)")
    assertOnColumnDataHeadings(
      lockoutMovesSheet,
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
    assertCellEquals(lockoutMovesSheet, 9, 0, move.reference)
    assertCellEquals(lockoutMovesSheet, 9, 1, move.fromSiteName)
    assertCellEquals(lockoutMovesSheet, 9, 2, move.fromLocationType?.name)
    assertCellEquals(lockoutMovesSheet, 9, 3, move.toSiteName)
    assertCellEquals(lockoutMovesSheet, 9, 4, move.toLocationType?.name)
    assertCellEquals(lockoutMovesSheet, 9, 5, move.pickUpDateTime?.toLocalDate())
    assertCellEquals(lockoutMovesSheet, 9, 6, move.pickUpDateTime?.toLocalTime())
    assertCellEquals(lockoutMovesSheet, 9, 7, move.dropOffOrCancelledDateTime?.toLocalDate())
    assertCellEquals(lockoutMovesSheet, 9, 8, move.dropOffOrCancelledDateTime?.toLocalTime())
    assertCellEquals(lockoutMovesSheet, 9, 9, "REG100, REG200")
    assertCellEquals(lockoutMovesSheet, 9, 10, move.person?.prisonNumber)
    assertCellEquals(lockoutMovesSheet, 9, 11, move.totalInPounds())
    assertCellEquals(lockoutMovesSheet, 9, 12, "")
    assertCellEquals(lockoutMovesSheet, 9, 13, move.notes)
  }

  private fun assertOnJourneys() {
    var row = 10

    listOf(journey1, journey2).forEachIndexed { index, journey ->
      assertCellEquals(lockoutMovesSheet, row, 0, "Journey ${index + 1}")
      assertCellEquals(lockoutMovesSheet, row, 1, journey.fromSiteName)
      assertCellEquals(lockoutMovesSheet, row, 2, journey.fromLocationType?.name)
      assertCellEquals(lockoutMovesSheet, row, 3, journey.toSiteName)
      assertCellEquals(lockoutMovesSheet, row, 4, journey.toLocationType?.name)
      assertCellEquals(lockoutMovesSheet, row, 5, journey.pickUpDateTime?.toLocalDate())
      assertCellEquals(lockoutMovesSheet, row, 6, journey.pickUpDateTime?.toLocalTime())
      assertCellEquals(lockoutMovesSheet, row, 7, journey.dropOffDateTime?.toLocalDate())
      assertCellEquals(lockoutMovesSheet, row, 8, journey.dropOffDateTime?.toLocalTime())
      assertCellEquals(lockoutMovesSheet, row, 9, journey.vehicleRegistration)
      assertCellEquals(lockoutMovesSheet, row, 10, "")
      assertCellEquals(lockoutMovesSheet, row, 11, journey.priceInPounds())
      assertCellEquals(lockoutMovesSheet, row, 12, journey.isBillable())
      assertCellEquals(lockoutMovesSheet, row, 13, journey.notes)
      row++
    }
  }
}
