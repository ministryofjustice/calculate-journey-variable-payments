package uk.gov.justice.digital.hmpps.pecs.jpc.move

import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.import.report.MoveStatus
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