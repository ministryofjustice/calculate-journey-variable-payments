package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import java.io.File
import java.io.FileOutputStream

class OutputSpreadsheet {

    fun generateSpreadsheet(prices: Sequence<MovePrice>) : File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Standard Moves")

        // Row for Header
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Ref")
        headerRow.createCell(1).setCellValue("From")
        headerRow.createCell(2).setCellValue("To")
        headerRow.createCell(3).setCellValue("Price")

        prices.forEachIndexed{i, p ->
            val row = sheet.createRow(i+1)

            with(p.moveReport.move){
                row.createCell(0).setCellValue(reference)
                row.createCell(1).setCellValue(fromLocation)
                row.createCell(2).setCellValue(toLocation)
            }

            p.totalInPence()?.let{
                row.createCell(3).setCellValue(it.toDouble()) } ?:
                row.createCell(3).setCellValue("NOT PRESENT")
        }

        return createTempFile(suffix = "xlsx").apply {
            val fileOut = FileOutputStream(this)
            workbook.write(fileOut)
            fileOut.close()
            workbook.close()
        }
    }
}