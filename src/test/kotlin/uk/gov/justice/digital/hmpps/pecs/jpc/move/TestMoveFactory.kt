package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime

val moveDate = LocalDate.of(2020, 9, 10)

fun move(
        moveId: String = "M1",
        fromNomisAgencyId: String = "WYI",
        toNomisAgencyId: String = "GNI",
        dropOffOrCancelledDateTime: LocalDateTime = moveDate.atStartOfDay().plusHours(10),
        journeys: MutableList<Journey> = mutableListOf()
) = Move(
        moveId = moveId,
        supplier = Supplier.SERCO,
        moveType = MoveType.STANDARD,
        status = MoveStatus.COMPLETED,
        reference = "REF1",
        moveDate = moveDate,
        fromNomisAgencyId = fromNomisAgencyId,
        fromSiteName = "from",
        fromLocationType = LocationType.PR,
        toNomisAgencyId = toNomisAgencyId,
        toSiteName = "to",
        toLocationType = LocationType.PR,
        pickUpDateTime = moveDate.atStartOfDay(),
        dropOffOrCancelledDateTime = dropOffOrCancelledDateTime,
        notes = "some notes",
        prisonNumber = "PR101",
        vehicleRegistration = "reg100",
        journeys = journeys)

fun journey(
        moveId: String = move().moveId,
        journeyId: String = "J1",
        fromNomisAgencyId: String = "WYI",
        toNomisAgencyId: String = "GNI",
        state: JourneyState = JourneyState.COMPLETED,
        billable: Boolean = true,
        pickUpDateTime: LocalDateTime? = moveDate.atStartOfDay(),
        dropOffDateTime: LocalDateTime? = moveDate.atStartOfDay().plusHours(10)
) = Journey(
        journeyId = journeyId,
        state = state,
        moveId = moveId,
        fromNomisAgencyId = fromNomisAgencyId,
        fromSiteName = "from",
        fromLocationType = LocationType.PR,
        toNomisAgencyId = toNomisAgencyId,
        toSiteName = "to",
        toLocationType = LocationType.PR,
        pickUpDateTime = pickUpDateTime,
        dropOffDateTime = dropOffDateTime,
        billable = billable,
        priceInPence = 100,
        vehicleRegistration = "REG200",
        notes = "some notes"
)

fun event(eventId: String = "E1", eventType: EventType = EventType.MOVE_START, eventableId: String = move().moveId) = Event(
        id = eventId,
        type = eventType.value,
        eventableType = "move",
        eventableId = eventableId,
        occurredAt = moveDate.atStartOfDay(),
        recordedAt = moveDate.atStartOfDay(),
        notes = null,
        details = null
)