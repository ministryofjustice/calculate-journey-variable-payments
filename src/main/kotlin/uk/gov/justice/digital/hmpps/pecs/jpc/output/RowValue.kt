package uk.gov.justice.digital.hmpps.pecs.jpc.output

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.EventType
import java.time.format.DateTimeFormatter

data class StandardValues(
        val ref: String,
        val pickUp: String,
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
            val pickUpDate = Event.getLatestByType(price.movePersonJourneysEvents.events, EventType.MOVE_START)?.occurredAt
            val dropOffDate = Event.getLatestByType(price.movePersonJourneysEvents.events, EventType.MOVE_COMPLETE)?.occurredAt

            return with(price.movePersonJourneysEvents.move) {
                StandardValues(
                        reference,
                        fromLocation,
                        price.fromLocationType?.name,
                        toLocation,
                        price.toLocationType?.name,
                        pickUpDate?.format(dateFormatter),
                        pickUpDate?.format(timeFormatter),
                        dropOffDate?.format(dateFormatter),
                        dropOffDate?.format(timeFormatter),
                        price.movePersonJourneysEvents.journeysWithEvents.withIndex().joinToString(separator = ", ") {
                            "Journey ${(it.index+1).toString()}: " + it.value.journey.vehicleRegistration},
                        price.movePersonJourneysEvents.person?.prisonNumber,
                        price.totalInPence()
                )
            }
        }
    }
}