package uk.gov.justice.digital.hmpps.pecs.jpc

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import java.io.Closeable

/**
 * Supertype for inbound spreadsheets e.g. locations and prices spreadsheets.
 */
open class InboundSpreadsheet(private val spreadsheet: Workbook): Closeable {

    /**
     * Strips all whitespace and converts to uppercase.  If blank then returns null.
     */
    infix fun Row.getFormattedStringCell(cell: Int): String? {
        return getCell(cell).stringCellValue.toUpperCase().trim().takeIf { it.isNotBlank() }
    }

    override fun close() {
        spreadsheet.close()
    }
}
