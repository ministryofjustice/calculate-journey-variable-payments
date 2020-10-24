package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import com.beust.klaxon.Klaxon
import uk.gov.justice.digital.hmpps.pecs.jpc.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.pricing.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val defaultDateTime = LocalDateTime.parse("2020-06-16T10:20:30+01:00", DateTimeFormatter.ISO_DATE_TIME)
val defaultDate = LocalDate.parse("2021-02-28", DateTimeFormatter.ISO_LOCAL_DATE)

const val defaultMoveId="M1"
const val defaultJourneyId="J1"
const val defaultMoveEventId="ME1"
const val defaultJourneyEventId="JE1"
const val defaultProfileId="PR1"
const val defaultPersonId="PE1"

val defaultSupplier =  Supplier.SERCO.reportingName()

fun fromPrisonNomisAgencyId() = "WYI"
fun fromPrisonLocationFactory() = Location(id = UUID.randomUUID(), locationType = LocationType.PR, nomisAgencyId = "WYI", siteName = "from")

fun toCourtNomisAgencyId() = "GNI"
fun toCourtLocationFactory() = Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "GNI", siteName = "to")

fun testAgencyId2Location(agencyId: String) : Location?{
    return when(agencyId){
        "WYI" -> fromPrisonLocationFactory()
        "GNI" -> toCourtLocationFactory()
        else -> Location(id = UUID.randomUUID(), locationType = LocationType.UNKNOWN, nomisAgencyId = agencyId, siteName = agencyId)
    }
}


fun notMappedNomisAgencyId() =  "NOT_MAPPED_AGENCY_ID"

fun priceFactory(
        supplier: Supplier = Supplier.SERCO,
        priceInPence: Int = 101): Price {

    return Price(
            supplier = supplier,
            fromLocation = fromPrisonLocationFactory(),
            toLocation = toCourtLocationFactory(),
            priceInPence = priceInPence
    )
}

fun moveFactory(
        moveId: String = defaultMoveId,
        supplier: String = defaultSupplier,
        profileId: String = defaultProfileId,
        status: String = MoveStatus.COMPLETED.value,
        fromLocation: String = fromPrisonNomisAgencyId(),
        fromLocationType: String = "prison",
        toLocation : String = toCourtNomisAgencyId(),
        toLocationType: String = "court",
        cancellationReason: String = "",
        date: LocalDate = defaultDate
    ): Move {
    val move = Move(
            id = moveId,
            supplier = supplier,
            profileId = profileId,
            reference = "UKW4591N",
            date = date,
            status = status,
            fromLocation = fromLocation,
            fromLocationType = fromLocationType,
            toLocation = toLocation,
            toLocationType = toLocationType,
            cancellationReason = cancellationReason
    )
    return move
}

fun personFactory(personId: String = defaultPersonId): Person{
    return Person(personId, "PRISON1")
}

fun profileFactory(
        profileId: String = defaultProfileId,
        personId: String = defaultPersonId): Profile{
    return Profile(profileId, personId)
}

fun moveEventFactory(
        eventId : String = defaultMoveEventId,
        moveId: String = defaultMoveId,
        type: String = EventType.MOVE_CANCEL.value,
        supplier: String = defaultSupplier,
        occurredAt: LocalDateTime = defaultDateTime,
        notes: String = ""
): Event{
    val event = Event(
            id=eventId,
            type=type,
            supplier= supplier,
            eventableType="move",
            eventableId= moveId,
            details= mapOf("string_key" to "string_val", "int_key" to 3, "bool_key" to true),
            occurredAt=occurredAt,
            recordedAt=defaultDateTime,
            notes=notes)
    return event
}

fun journeyFactory(
        journeyId: String = defaultJourneyId,
        moveId: String = defaultMoveId,
        state: String = JourneyState.COMPLETED.value,
        supplier: String = defaultSupplier,
        billable: Boolean = false,
        fromLocation: String = fromPrisonNomisAgencyId(),
        toLocation : String = toCourtNomisAgencyId(),
        vehicleRegistration: String? ="UHE-92"

): Journey{
    val journey = Journey(
            id= journeyId,
            moveId= moveId,
            clientTimestamp = defaultDateTime,
            billable=billable,
            state=state,
            supplier= supplier,
            vehicleRegistration=vehicleRegistration,
            fromLocation=fromLocation,
            toLocation=toLocation)
    return journey
}

fun journeyEventFactory(
        journeyEventId: String = defaultJourneyEventId,
        journeyId: String = defaultJourneyId,
        type: String = EventType.JOURNEY_START.value,
        supplier: String = defaultSupplier,
        occurredAt: LocalDateTime = defaultDateTime,
        notes: String = ""
): Event{
    val event = Event(
            id=journeyEventId,
            type=type,
            supplier= supplier,
            eventableType="journey",
            eventableId= journeyId,
            details= mapOf(),
            occurredAt=occurredAt,
            recordedAt=defaultDateTime,
            notes=notes)
    return event
}





