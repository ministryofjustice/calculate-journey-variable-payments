package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.CONTRACTOR_BILLABLE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.DROP_OFF
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.DROP_OFF_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.DROP_OFF_TIME
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.LOCATION_TYPE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.MOVE_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.NOMIS_PRISON_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.NOTES
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.PICK_UP
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.PICK_UP_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.PICK_UP_TIME
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.PRICE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.PriceSheet.DataColumn.VEHICLE_REG

class LockoutMovesSheet(workbook: Workbook, header: Header) :
  PriceSheet(
    sheet = workbook.createSheet("Lockouts"),
    header = header,
    subheading = "LOCKOUTS (refused admission to prison)",
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
      NOTES,
    ),
  ) {

  override fun writeMove(move: Move) {
    writeMoveRow(move)
    writeJourneyRows(move.journeys)
  }
}
