package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Summary
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MovesCountAndSummaries


class SummarySheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Summary")!!, header) {

    override fun writeMove(move: Move) {}

    fun writeSummaries(movesCountAndSummaries: MovesCountAndSummaries) {
        val summaries = movesCountAndSummaries.allSummaries()
        writeSummary(9, summaries[0])
        writeSummary(12, summaries[1])
        writeSummary(15, summaries[2])
        writeSummary(18, summaries[3])
        writeSummary(21, summaries[4])
        writeSummary(24, summaries[5])
        writeSummary(28, movesCountAndSummaries.summary())

    }

    private fun writeSummary(rIndex: Int, summary: Summary){
        val row = getRow(rIndex)
        row.addCell(1, summary.percentage, ::dataFormatPercentage)
        row.addCell(2, summary.volume)
        row.addCell(3, summary.volumeUnpriced)
        row.addCell(4, summary.totalPriceInPounds, ::dataFormatPound)
    }
}
