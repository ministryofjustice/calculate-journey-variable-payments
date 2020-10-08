package uk.gov.justice.digital.hmpps.pecs.jpc.output

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.JourneyState
import java.time.format.DateTimeFormatter

data class RowValue(
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
        val priceInPounds: Double?,
        val billable: String,
        val notes: String
){

    companion object {
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun forMovePrice(price: MovePrice): RowValue {
            val pickUpDate = Event.getLatestByType(price.moveReport.events, EventType.MOVE_START)?.occurredAt
            val dropOffDate = Event.getLatestByType(price.moveReport.events, EventType.MOVE_COMPLETE)?.occurredAt

            return with(price) {
                RowValue(
                        moveReport.move.reference,
                        moveReport.move.fromLocation.siteName,
                        moveReport.move.fromLocation.locationType.name,
                        moveReport.move.toLocation?.siteName,
                        moveReport.move.toLocation?.locationType?.name,
                        pickUpDate?.format(dateFormatter),
                        pickUpDate?.format(timeFormatter),
                        dropOffDate?.format(dateFormatter),
                        dropOffDate?.format(timeFormatter),
                        moveReport.journeysWithEvents.withIndex().joinToString(separator = ", ") {
                            it.value.journey.vehicleRegistration ?: "NOT GIVEN"},
                        moveReport.person?.prisonNumber,
                        totalInPence()?.let{it.toDouble() / 100},
                        "",
                        moveReport.events.notes()
                )
            }
        }

        fun forJourneyPrice(journeyNumber: Int, price: JourneyPrice): RowValue {
            with(price) {
                val pickUpDate = Event.getLatestByType(price.journeyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
                val dropOffDate = Event.getLatestByType(price.journeyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt
                val cancelled = journeyWithEvents.journey.state == JourneyState.CANCELLED.value

                return RowValue(
                        "Journey $journeyNumber",
                        journeyWithEvents.journey.fromLocation.siteName,
                        journeyWithEvents.journey.fromLocation.locationType.name,
                        journeyWithEvents.journey.toLocation.siteName,
                        journeyWithEvents.journey.toLocation.locationType.name,
                        pickUpDate?.format(dateFormatter),
                        pickUpDate?.format(timeFormatter),
                        if(cancelled) "CANCELLED" else dropOffDate?.format(dateFormatter),
                        if(cancelled) "CANCELLED" else dropOffDate?.format(timeFormatter),
                        journeyWithEvents.journey.vehicleRegistration,
                        null,
                        priceInPence?.let { it.toDouble() / 100 },
                        if(journeyWithEvents.journey.billable) "YES" else "NO",
                        journeyWithEvents.events.notes()
                )
            }
        }
    }
}

fun List<Event>.notes() = this.filter { !it.notes.isNullOrBlank() }.joinToString(separator = "\n") {"* " + it.notes}