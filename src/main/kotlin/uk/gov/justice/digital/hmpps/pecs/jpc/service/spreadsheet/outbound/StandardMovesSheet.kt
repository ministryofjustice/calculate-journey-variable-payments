package uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound

import org.apache.poi.ss.usermodel.Workbook
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.DROP_OFF
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.DROP_OFF_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.DROP_OFF_TIME
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.LOCATION_TYPE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.MOVE_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.NOMIS_PRISON_ID
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.PICK_UP
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.PICK_UP_DATE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.PICK_UP_TIME
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.PRICE
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.outbound.PriceSheet.DataColumn.VEHICLE_REG

class StandardMovesSheet(workbook: Workbook, header: Header) : PriceSheet(
  sheet = workbook.createSheet("Standard"),
  header = header,
  subheading = "STANDARD MOVES (includes single journeys, cross supplier and redirects before the move has started)",
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
    PRICE
  )
) {
  override fun writeMove(move: Move) = writeMoveRow(move, false)
}
