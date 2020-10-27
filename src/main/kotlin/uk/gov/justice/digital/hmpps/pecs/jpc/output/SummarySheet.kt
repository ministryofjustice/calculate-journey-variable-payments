package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.*
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.MoveModel


class SummarySheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Summary")!!, header) {

    override fun writeMove(moveModel: MoveModel) {}

    fun writeSummaries(summaries: List<PriceSummary>) {
        summaries.forEachIndexed {i,summary ->
            writeSummary(3 * i + 9, summary)
        }
        writeSummary(28, summaries.summary())
    }

    private fun writeSummary(rIndex: Int, summary: PriceSummary){
        val row = getRow(rIndex)
        row.addCell(1, summary.percentage, ::dataFormatPercentage)
        row.addCell(2, summary.volume)
        row.addCell(3, summary.volumeUnpriced)
        row.addCell(4, summary.totalPriceInPounds, ::dataFormatPound)
    }
}
