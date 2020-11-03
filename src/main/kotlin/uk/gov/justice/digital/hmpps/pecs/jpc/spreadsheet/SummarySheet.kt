package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveTypeWithMovesAndSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Summary


class SummarySheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Summary")!!, header) {

    override fun writeMove(move: Move) {}

    fun writeSummaries(moves: MoveTypeWithMovesAndSummary) {
        with(moves){
            writeSummary(9, standard.summary)
            writeSummary(12, longHaul.summary)
            writeSummary(15, redirection.summary)
            writeSummary(18, lockout.summary)
            writeSummary(21, multi.summary)
            writeSummary(24, cancelled.summary)
            writeSummary(28, summary())
        }

    }

    private fun writeSummary(rIndex: Int, summary: Summary){
        val row = getRow(rIndex)
        row.addCell(1, summary.percentage, ::dataFormatPercentage)
        row.addCell(2, summary.volume)
        row.addCell(3, summary.volumeUnpriced)
        row.addCell(4, summary.totalPriceInPounds, ::dataFormatPound)
    }
}
