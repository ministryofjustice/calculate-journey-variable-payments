package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class JPCPriceBookSheetTest {

    private val jpcPriceBookSheet: JPCPriceBookSheet = JPCPriceBookSheet(outputSpreadsheet())

    private fun outputSpreadsheet(): XSSFWorkbook {
        return XSSFWorkbook().apply { createSheet("JPC Price book") }
    }

    @Test
    internal fun `fails if output sheet is missing`() {
        assertThatThrownBy { JPCPriceBookSheet(XSSFWorkbook()) }.isInstanceOf(NullPointerException::class.java)
    }

    @Test
    internal fun `original prices are copied over to output spreadsheet`() {
        assertThat(jpcPriceBookSheet.sheet.toList()).isEmpty()

        jpcPriceBookSheet.copyPricesFrom(originalPricesSpreadsheet(
                PriceRow(1.0, "FROM SITE 1", "TO SITE 2", 100.00),
                PriceRow(2.0, "FROM SITE 2", "TO SITE 2", 200.00)))

        assertThat(jpcPriceBookSheet.sheet.getRow(0).getCell(0).stringCellValue).isEqualTo("Header Row")
        assertOnPriceRow(jpcPriceBookSheet.sheet.getRow(1), PriceRow(1.0, "FROM SITE 1", "TO SITE 2", 100.00))
        assertOnPriceRow(jpcPriceBookSheet.sheet.getRow(2), PriceRow(2.0, "FROM SITE 2", "TO SITE 2", 200.00))
    }

    private fun assertOnPriceRow(row: Row, priceRow: PriceRow) {
        assertThat(row.getCell(0).numericCellValue).isEqualTo(priceRow.journeyId)
        assertThat(row.getCell(1).stringCellValue).isEqualTo(priceRow.fromSite)
        assertThat(row.getCell(2).stringCellValue).isEqualTo(priceRow.toSite)
        assertThat(row.getCell(3).numericCellValue).isEqualTo(priceRow.price)
    }

    private fun originalPricesSpreadsheet(vararg priceRows: PriceRow): Sheet {
        return XSSFWorkbook().apply {
            this.createSheet().apply {
                this.createRow(0).apply {
                    this.createCell(0).setCellValue("Header Row")
                }

                priceRows.forEachIndexed { i, pr ->
                    this.createRow(i + 1).apply {
                        this.createCell(0).setCellValue(pr.journeyId)
                        this.createCell(1).setCellValue(pr.fromSite)
                        this.createCell(2).setCellValue(pr.toSite)
                        this.createCell(3).setCellValue(pr.price)
                    }
                }
            }
        }.getSheetAt(0)
    }

    internal data class PriceRow(val journeyId: Double, val fromSite: String, val toSite: String, val price: Double)
}
