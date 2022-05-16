package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.util.DateRange
import java.time.LocalDate

internal class StandardMovesSheetTest {

  private val date: LocalDate = LocalDate.now()

  @Test
  internal fun `headings are present for Serco`() {
    assertOnHeadingsFor(date, Supplier.SERCO)
  }

  @Test
  internal fun `headings are present for Geoamey`() {
    assertOnHeadingsFor(date, Supplier.GEOAMEY)
  }

  private fun assertOnHeadingsFor(date: LocalDate, supplier: Supplier) {
    val sms = StandardMovesSheet(SXSSFWorkbook(), PriceSheet.Header(date, DateRange(date, date), supplier))

    assertOnSheetName(sms, "Standard")
    assertThat(sms.sheet.getRow(4).getCell(2).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
    assertThat(sms.sheet.getRow(4).getCell(0).stringCellValue.uppercase()).isEqualTo(supplier.name)
    assertOnSubheading(
      sms,
      "STANDARD MOVES (includes single journeys, cross supplier and redirects before the move has started)"
    )
    assertOnColumnDataHeadings(
      sms,
      "Move ID",
      "Pick up",
      "Location type",
      "Drop off",
      "Location type",
      "Pick up date",
      "Pick up time",
      "Drop off date",
      "Drop off time",
      "Vehicle reg",
      "NOMIS prison ID",
      "Price"
    )
  }

  @Test
  internal fun `test prices`() {
    val sms = StandardMovesSheet(
      SXSSFWorkbook(),
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        DateRange(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
        Supplier.SERCO
      )
    )

    val move = moveM1(journeys = listOf(journeyJ1()))
    val moves = listOf(move)

    sms.writeMoves(moves)

    assertCellEquals(sms, 9, 0, "REF1")
    assertCellEquals(sms, 9, 1, "from") // pick up sitename
    assertCellEquals(sms, 9, 2, "PR") // pick up location type

    assertCellEquals(sms, 9, 3, "to") // drop off sitename
    assertCellEquals(sms, 9, 4, "PR") // drop off location type

    assertCellEquals(sms, 9, 5, "10/09/2020") // Pick up date
    assertCellEquals(sms, 9, 6, "00:00") // Pick up time
    assertCellEquals(sms, 9, 7, "10/09/2020") // Drop off date
    assertCellEquals(sms, 9, 8, "10:00") // Drop off time

    assertCellEquals(sms, 9, 9, "REG100") // vehicle reg

    assertCellEquals(sms, 9, 10, "PR101") // prison number

    assertCellEquals(sms, 9, 11, 1.0) // price

    assertThat(sms.getRow(9).getCell(12)).isNull() // billable shouldn't be shown
    assertThat(sms.getRow(9).getCell(13)).isNull() // notes shouldn't be shown for a standard move
  }
}
