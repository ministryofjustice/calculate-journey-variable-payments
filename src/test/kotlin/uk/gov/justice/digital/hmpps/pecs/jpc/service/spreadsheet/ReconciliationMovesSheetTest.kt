package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDateTime

class ReconciliationMovesSheetTest {

  private val date = LocalDateTime.of(2022, 5, 13, 12, 0)

  private val sheet = ReconciliationMovesSheet(
    SXSSFWorkbook(),
    PriceSheet.Header(date.toLocalDate(), DateRange(date.toLocalDate(), date.toLocalDate()), Supplier.GEOAMEY),
  )

  private val move1 = Move(
    moveId = "1",
    updatedAt = date,
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.STANDARD,
    status = MoveStatus.Completed,
    reference = "ABC12345",
    moveDate = date.toLocalDate(),
    fromNomisAgencyId = "AGENCY_ID",
    pickUpDateTime = date,
    dropOffOrCancelledDateTime = date.plusMinutes(10),
    reportFromLocationType = LocationType.CC.name,
  )

  @Test
  fun `sheet is named correctly`() {
    assertOnSheetName(sheet, "Reconciliation Moves")
  }

  @Test
  fun `column names are present and correct`() {
    assertOnColumnDataHeadings(
      sheet,
      "Move ID",
      "Move date",
      "Move type",
      "Move status",
      "Pick up date",
      "Pick up time",
      "Drop off date",
      "Drop off time",
    )
  }

  @Test
  fun `move data is applied and correct`() {
    sheet.writeMoves(
      listOf(move1),
    )

    assertOnRow(
      sheet,
      9,
      "ABC12345",
      "13/05/2022",
      MoveType.STANDARD,
      MoveStatus.Completed,
      "13/05/2022",
      "12:00",
      "13/05/2022",
      "12:10",
    )
  }
}
