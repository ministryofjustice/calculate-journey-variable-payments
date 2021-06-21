package uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.CONTRACTOR_BILLABLE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.DROP_OFF
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.DROP_OFF_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.DROP_OFF_TIME
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.LOCATION_TYPE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.MOVE_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.NOMIS_PRISON_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.NOTES
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.PICK_UP
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.PICK_UP_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.PICK_UP_TIME
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.PRICE
import uk.gov.justice.digital.hmpps.pecs.jpc.spreadsheet.PriceSheet.DataColumn.VEHICLE_REG

class MultiTypeMovesSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Multi-type"),
  header = header,
  subheading = "MULTI-TYPE MOVES (includes combinations of move types)",
  dataColumns = listOf(
    MOVE_ID,
    PICK_UP,
    LOCATION_TYPE,
    DROP_OFF,
    LOCATION_TYPE,
    PICK_UP_DATE,
    PICK_UP_TIME,
    DROP_OFF_DATE,
    DROP_OFF_TIME,
    VEHICLE_REG,
    NOMIS_PRISON_ID,
    PRICE,
    CONTRACTOR_BILLABLE,
    NOTES
  )
) {

  override fun writeMove(move: Move) {
    writeMoveRow(move)
    writeJourneyRows(move.journeys)
  }
}
