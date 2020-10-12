package uk.gov.justice.digital.hmpps.pecs.jpc.output

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.*


class SummarySheet(workbook: Workbook, header: Header) : PriceSheet(workbook.getSheet("Summary")!!, header) {

    override fun writeMove(price: MovePrice) {}

    fun writeSummaries(allPrices: List<MovePrices>) {
        MovePriceType.values().forEach {
            writeSummary(allPrices.withType(it).summary)
        }
        writeSummary(allPrices.summary())
    }

    private fun writeSummary(summary: PriceSummary){
        val row = createRow()
        row.addCell(1, summary.percentage, ::dataFormatPercentage)
        row.addCell(2, summary.volume)
        row.addCell(3, summary.volumeUnpriced)
        row.addCell(4, summary.totalPriceInPounds, ::dataFormatPound)
    }
}
