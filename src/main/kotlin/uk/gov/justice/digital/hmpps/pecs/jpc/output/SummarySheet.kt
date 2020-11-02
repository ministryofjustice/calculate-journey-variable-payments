package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.*
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveModel
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MovePriceTypeWithMovesAndSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Summary


class SummarySheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Summary")!!, header) {

    override fun writeMove(moveModel: MoveModel) {}

    fun writeSummaries(moves: MovePriceTypeWithMovesAndSummary) {
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
