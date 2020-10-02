package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.EventType
import java.time.format.DateTimeFormatter

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Standard")!!, header) {

    override fun add(row: Row, price: MovePrice) = addStandardRow(row, price)

}
