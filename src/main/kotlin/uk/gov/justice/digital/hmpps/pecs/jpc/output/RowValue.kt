package uk.gov.justice.digital.hmpps.pecs.jpc.output

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.JourneyState
import java.time.LocalDate
import java.time.LocalDateTime
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
        val notes: String,
        val moveDate: String? = null,
        val cancelledDate: String? = null,
        val cancelledTime: String? = null
){

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun forMovePrice(price: MovePrice): RowValue {
            val pickUpDate = Event.getLatestByType(price.report.events, EventType.MOVE_START)?.occurredAt
            val dropOffDate = Event.getLatestByType(price.report.events, EventType.MOVE_COMPLETE)?.occurredAt
            val cancelledDate = Event.getLatestByType(price.report.events, EventType.MOVE_CANCEL)?.occurredAt

            return with(price) {
                RowValue(
                        report.move.reference,
                        report.move.fromLocation.siteName,
                        report.move.fromLocation.locationType.name,
                        report.move.toLocation?.siteName,
                        report.move.toLocation?.locationType?.name,
                        pickUpDate?.format(dateFormatter),
                        pickUpDate?.format(timeFormatter),
                        dropOffDate?.format(dateFormatter),
                        dropOffDate?.format(timeFormatter),
                        report.journeysWithEvents.withIndex().joinToString(separator = ", ") {
                            it.value.journey.vehicleRegistration ?: "NOT GIVEN"},
                        report.person?.prisonNumber,
                        totalInPence()?.let{it.toDouble() / 100},
                        "",
                        report.events.notes(),
                        report.move.date?.format(dateFormatter),
                        cancelledDate?.format(dateFormatter),
                        cancelledDate?.format(timeFormatter)
                )
            }
        }

        fun forJourneyPrice(journeyNumber: Int, price: JourneyPrice): RowValue {
            with(price) {
                val pickUpDate = Event.getLatestByType(price.journeyWithEvents.events, EventType.JOURNEY_START)?.occurredAt
                val dropOffDate = Event.getLatestByType(price.journeyWithEvents.events, EventType.JOURNEY_COMPLETE)?.occurredAt
                val isCancelled = journeyWithEvents.journey.state == JourneyState.CANCELLED.value

                return RowValue(
                        "Journey $journeyNumber",
                        journeyWithEvents.journey.fromLocation.siteName,
                        journeyWithEvents.journey.fromLocation.locationType.name,
                        journeyWithEvents.journey.toLocation.siteName,
                        journeyWithEvents.journey.toLocation.locationType.name,
                        pickUpDate?.format(dateFormatter),
                        pickUpDate?.format(timeFormatter),
                        if(isCancelled) "CANCELLED" else dropOffDate?.format(dateFormatter),
                        if(isCancelled) "CANCELLED" else dropOffDate?.format(timeFormatter),
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

private val noteworthyEvents = listOf(
        EventType.MOVE_REDIRECT, EventType.JOURNEY_LOCKOUT, EventType.MOVE_LOCKOUT, EventType.JOURNEY_CANCEL,
        EventType.MOVE_LODGING_START, EventType.MOVE_LODGING_END, EventType.JOURNEY_LODGING).map { it.value }

fun List<Event>.notes() = filter { it.type in noteworthyEvents && !it.notes.isNullOrBlank() }.
    joinToString() {"${it.type}: ${it.notes}"}