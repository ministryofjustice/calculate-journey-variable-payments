package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.ClosedRangeLocalDate

internal class RedirectionMovesSheetTest {

  private val journey1 = journeyJ1()
  private val journey2 = journeyJ1(journeyId = "J2", vehicleRegistration = "REG200")
  private val move = moveM1(journeys = listOf(journey1, journey2))
  private val moves = listOf(move)
  private val redirectionMovesSheet = RedirectionMovesSheet(
    SXSSFWorkbook(),
    PriceSheet.Header(
      defaultMoveDate10Sep2020,
      ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
      Supplier.SERCO
    )
  )

  @Test
  internal fun `test redirection prices`() {
    redirectionMovesSheet.writeMoves(moves)

    assertOnSheetName(redirectionMovesSheet, "Redirections")
    assertOnSubheading(redirectionMovesSheet, "REDIRECTIONS (a redirection after the move has started)")
    assertOnColumnDataHeadings(
      redirectionMovesSheet,
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

    assertCellEquals(redirectionMovesSheet, 9, 0, "REF1")

    assertCellEquals(redirectionMovesSheet, 9, 1, "from") // pick up site name
    assertCellEquals(redirectionMovesSheet, 9, 2, "PR") // pick up location type
    assertCellEquals(redirectionMovesSheet, 9, 3, "to") // drop off site name
    assertCellEquals(redirectionMovesSheet, 9, 4, "PR") // drop off location type

    assertCellEquals(redirectionMovesSheet, 9, 5, "10/09/2020") // Pick up date
    assertCellEquals(redirectionMovesSheet, 9, 6, "00:00") // Pick up time
    assertCellEquals(redirectionMovesSheet, 9, 7, "10/09/2020") // Drop off date
    assertCellEquals(redirectionMovesSheet, 9, 8, "10:00") // Drop off time

    assertCellEquals(redirectionMovesSheet, 9, 9, "REG100, REG200") // vehicle reg
    assertCellEquals(redirectionMovesSheet, 9, 10, "PR101") // prison number
    assertCellEquals(redirectionMovesSheet, 9, 11, 2.0) // price
    assertCellEquals(redirectionMovesSheet, 9, 12, "") // billable shouldn't be shown
    assertCellEquals(redirectionMovesSheet, 9, 13, "some notes") // should only show the redirect event notes

    // Journey 1
    assertCellEquals(redirectionMovesSheet, 10, 0, "Journey 1")

    assertCellEquals(redirectionMovesSheet, 10, 1, "from")
    assertCellEquals(redirectionMovesSheet, 10, 2, "PR")
    assertCellEquals(redirectionMovesSheet, 10, 3, "to")
    assertCellEquals(redirectionMovesSheet, 10, 4, "PR")

    assertCellEquals(redirectionMovesSheet, 10, 5, "10/09/2020") // Pick up date
    assertCellEquals(redirectionMovesSheet, 10, 6, "00:00") // Pick up time
    assertCellEquals(redirectionMovesSheet, 10, 7, "10/09/2020") // Drop off date
    assertCellEquals(redirectionMovesSheet, 10, 8, "10:00") // Drop off time

    assertCellEquals(redirectionMovesSheet, 10, 9, "REG100") // vehicle reg
    assertCellEquals(redirectionMovesSheet, 10, 10, "") // no prison number for journeys
    assertCellEquals(redirectionMovesSheet, 10, 11, 1.0) // price
    assertCellEquals(redirectionMovesSheet, 10, 12, "YES") // contractor billable
    assertCellEquals(redirectionMovesSheet, 10, 13, "some notes")

    // Journey 2
    assertCellEquals(redirectionMovesSheet, 11, 0, "Journey 2")
    assertCellEquals(redirectionMovesSheet, 11, 9, "REG200") // vehicle reg
  }
}
