package uk.gov.justice.digital.hmpps.pecs.jpc.output

import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.JourneyPrice
import uk.gov.justice.digital.hmpps.pecs.jpc.calculator.MovePrice
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.reporting.EventType
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
        val priceInPence: Int?
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
                        totalInPence()
                )
            }
        }

        fun forJourneyPrices(journeyNumber: Int, price: JourneyPrice): RowValue {
            with(price) {
                val pickUpDate = Event.getLatestByType(price.journeyWithEvents.events, EventType.MOVE_START)?.occurredAt
                val dropOffDate = Event.getLatestByType(price.journeyWithEvents.events, EventType.MOVE_COMPLETE)?.occurredAt

                return RowValue(
                        "Journey $journeyNumber ",
                        journeyWithEvents.journey.fromLocation.siteName,
                        journeyWithEvents.journey.fromLocation.locationType.name,
                        journeyWithEvents.journey.toLocation.siteName,
                        journeyWithEvents.journey.toLocation.locationType.name,
                        pickUpDate?.format(dateFormatter),
                        pickUpDate?.format(timeFormatter),
                        dropOffDate?.format(dateFormatter),
                        dropOffDate?.format(timeFormatter),
                        journeyWithEvents.journey.vehicleRegistration,
                        null,
                        priceInPence
                )
            }
        }
    }
}