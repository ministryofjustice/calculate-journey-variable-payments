package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MovesSummary
import uk.gov.justice.digital.hmpps.pecs.jpc.service.MoveTypeSummaries

class SummarySheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Summary"),
  header = header,
  subheading = "OUTPUT SUMMARY",
  dataColumnHeadings = listOf(
    "Move type",
    "Percentage",
    "Move volume",
    "Move volume without prices",
    "Price"
  )
) {

  private val fontBlackArialItalic = sheet.workbook.createFont().apply {
    color = IndexedColors.BLACK.index
    fontName = "ARIAL"
    italic = true
  }

  private val labelStyle: CellStyle = sheet.workbook.createCellStyle().apply {
    this.alignment = HorizontalAlignment.LEFT
    this.setFont(fontBlackArialBold)
  }

  private val descriptiveTextStyle: CellStyle = sheet.workbook.createCellStyle().apply {
    this.alignment = HorizontalAlignment.LEFT
    this.setFont(fontBlackArialItalic)
  }

  override fun writeMove(move: Move) {}

  fun writeSummaries(moveTypeSummaries: MoveTypeSummaries) {
    val summaries = moveTypeSummaries.allSummaries()
    writeSummary(9, summaries[0])
    writeDescription(10, summaries[0].moveType)
    writeSummary(12, summaries[1])
    writeDescription(13, summaries[1].moveType)
    writeSummary(15, summaries[2])
    writeDescription(16, summaries[2].moveType)
    writeSummary(18, summaries[3])
    writeDescription(19, summaries[3].moveType)
    writeSummary(21, summaries[4])
    writeDescription(22, summaries[4].moveType)
    writeSummary(24, summaries[5])
    writeDescription(25, summaries[5].moveType)
    writeFooterSummary(moveTypeSummaries.summary())
  }

  private fun writeDescription(rindex: Int, moveType: MoveType?) {
    moveType?.let { createRow(rindex).apply { addCell(0, moveType.description, descriptiveTextStyle) } }
  }

  private fun writeSummary(rIndex: Int, movesSummary: MovesSummary) {
    val row = createRow(rIndex)
    row.addCell(0, movesSummary.moveType?.label, labelStyle)
    row.addCell(1, movesSummary.percentage, fillWhitePercentage)
    row.addCell(2, movesSummary.volume)
    row.addCell(3, movesSummary.volumeUnpriced)
    row.addCell(4, movesSummary.totalPriceInPounds, fillWhitePound)
  }

  private fun writeFooterSummary(movesSummary: MovesSummary) {
    createRow(27).apply {
      addEmptyHeaderCell(0)
      addHeaderCell(1, "Total %")
      addHeaderCell(2, "Total volume")
      addHeaderCell(3, "Without prices")
      addHeaderCell(4, "Total price")
    }

    writeSummary(28, movesSummary)
  }
}
