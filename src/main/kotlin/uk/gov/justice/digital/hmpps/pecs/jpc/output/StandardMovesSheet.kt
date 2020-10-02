package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.EventType
import java.time.format.DateTimeFormatter

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Standard")!!, header) {

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun add(row: Row, price: MovePrice) {

        val pickUpDate = Event.getLatestByType(price.movePersonJourneysEvents.events, EventType.MOVE_START)?.occurredAt
        val dropOffDate = Event.getLatestByType(price.movePersonJourneysEvents.events, EventType.MOVE_COMPLETE)?.occurredAt

        with(price.movePersonJourneysEvents.move) {
            row.createCell(0).setCellValue(reference)
            row.createCell(1).setCellValue(fromLocation)
            row.createCell(3).setCellValue(toLocation)
            row.createCell(5).setCellValue(pickUpDate?.format(dateFormatter))
            row.createCell(6).setCellValue(pickUpDate?.format(timeFormatter))
            row.createCell(7).setCellValue(dropOffDate?.format(dateFormatter))
            row.createCell(8).setCellValue(dropOffDate?.format(timeFormatter))

        }

        price.totalInPence()?.let {
            row.createCell(11).setCellValue(it.toDouble())
        } ?: row.createCell(11).setCellValue("NOT PRESENT")
    }
}
