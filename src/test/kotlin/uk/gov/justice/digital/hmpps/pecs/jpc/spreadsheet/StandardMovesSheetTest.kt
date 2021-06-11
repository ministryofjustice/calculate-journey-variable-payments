package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.JPCTemplateProvider
import uk.gov.justice.digital.hmpps.pecs.jpc.move.defaultMoveDate10Sep2020
import uk.gov.justice.digital.hmpps.pecs.jpc.move.journeyJ1
import uk.gov.justice.digital.hmpps.pecs.jpc.move.moveM1
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate

@SpringJUnitConfig(TestConfig::class)
internal class StandardMovesSheetTest(@Autowired private val template: JPCTemplateProvider) {

  private val workbook: Workbook = XSSFWorkbook(template.get())

  private val date: LocalDate = LocalDate.now()

  @Test
  internal fun `fails to instantiate if expected sheet is missing`() {
    assertThatThrownBy {
      StandardMovesSheet(
        XSSFWorkbook(),
        PriceSheet.Header(LocalDate.now(), ClosedRangeLocalDate(LocalDate.now(), LocalDate.now()), Supplier.SERCO)
      )
    }.isInstanceOf(NullPointerException::class.java)
  }

  @Test
  internal fun `default headings are applied for Serco`() {
    val sms = StandardMovesSheet(workbook, PriceSheet.Header(date, ClosedRangeLocalDate(date, date), Supplier.SERCO))

    assertThat(sms.sheet.getRow(2).getCell(2).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
    assertThat(sms.sheet.getRow(4).getCell(0).stringCellValue.uppercase()).isEqualTo(Supplier.SERCO.name)
  }

  @Test
  internal fun `default headings are applied for Geoamey`() {
    val sms = StandardMovesSheet(workbook, PriceSheet.Header(date, ClosedRangeLocalDate(date, date), Supplier.GEOAMEY))

    assertThat(sms.sheet.getRow(2).getCell(2).localDateTimeCellValue.toLocalDate()).isEqualTo(date)
    assertThat(sms.sheet.getRow(4).getCell(0).stringCellValue.uppercase()).isEqualTo(Supplier.GEOAMEY.name)
  }

  @Test
  internal fun `test prices`() {
    val sms = StandardMovesSheet(
      workbook,
      PriceSheet.Header(
        defaultMoveDate10Sep2020,
        ClosedRangeLocalDate(defaultMoveDate10Sep2020, defaultMoveDate10Sep2020),
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

    assertCellEquals(sms, 9, 9, "reg100") // vehicle reg

    assertCellEquals(sms, 9, 10, "PR101") // prison number

    assertCellEquals(sms, 9, 11, 1.0) // price

    assertCellEquals(sms, 9, 12, "") // billable shouldn't be shown
    assertCellEquals(sms, 9, 13, "") // notes shouldn't be shown for a standard move
  }
}

fun <T> assertCellEquals(sheet: PriceSheet, row: Int, col: Int, expectedVal: T?) {
  val actualValue = when (expectedVal) {
    is String -> sheet.sheet.getRow(row).getCell(col).stringCellValue
    is Double -> sheet.sheet.getRow(row).getCell(col).numericCellValue
    is Int -> sheet.sheet.getRow(row).getCell(col).numericCellValue.toInt()
    else -> throw RuntimeException("Must be a string or numeric value")
  }
  assertThat(actualValue).isEqualTo(expectedVal)
}
