package uk.gov.justice.digital.hmpps.pecs.jpc.output

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.EventType
import java.time.format.DateTimeFormatter

data class StandardValues(
        val ref: String,
        val pickUp: String?,
        val pickUpLocationType: String?,
        val dropOff: String?,
        val dropOffLocationType: String?,
        val pickUpDate: String?,
        val pickUpTime: String?,
        val dropOffDate: String?,
        val dropOffTime: String?,
        val vehicleReg: String?,
        val prisonNumber: String?,
        val totalInPence: Int?
){

    companion object {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun fromMovePrice(price: MovePrice): StandardValues {
            val pickUpDate = Event.getLatestByType(price.moveReport.events, EventType.MOVE_START)?.occurredAt
            val dropOffDate = Event.getLatestByType(price.moveReport.events, EventType.MOVE_COMPLETE)?.occurredAt

            return with(price) {
                StandardValues(
                        moveReport.move.reference,
                        fromLocation?.siteName ?: moveReport.move.fromLocation,
                        fromLocation?.locationType?.name ?: "NOT MAPPED",
                        toLocation?.siteName ?: moveReport.move.toLocation,
                        toLocation?.locationType?.name ?: "NOT MAPPED",
                        pickUpDate?.format(dateFormatter),
                        pickUpDate?.format(timeFormatter),
                        dropOffDate?.format(dateFormatter),
                        dropOffDate?.format(timeFormatter),
                        moveReport.journeysWithEvents.withIndex().joinToString(separator = ", ") {
                            it.value.journey.vehicleRegistration ?: "NOT GIVEN"},
                        moveReport.person?.prisonNumber,
                        totalInPence()
                )
            }
        }
    }
}