package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.move.UniqueJourney
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveTypeSummaries


class JourneysSheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Journeys")!!, header) {

    override fun writeMove(move: Move) {}

    fun writeJourneys(journeys: List<UniqueJourney>) {
        journeys.forEach {
            val row = createRow()
            row.addCell(0, it.fromSiteName())
            row.addCell(1, it.toSiteName())
            row.addCell(2, it.volume)
            row.addCell(3, it.unitPriceInPounds(), ::dataFormatPound)
            row.addCell(4, it.totalPriceInPounds(), ::dataFormatPound)
        }
    }
}
