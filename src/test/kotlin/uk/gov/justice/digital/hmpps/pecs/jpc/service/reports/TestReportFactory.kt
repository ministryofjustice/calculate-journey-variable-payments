package uk.gov.justice.digital.hmpps.pecs.jpc.service.reports

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

val defaultDateTime = LocalDateTime.parse("2020-06-16T10:20:30+01:00", DateTimeFormatter.ISO_DATE_TIME)
val defaultDate = LocalDate.parse("2021-02-28", DateTimeFormatter.ISO_LOCAL_DATE)

const val defaultMoveId = "M1"
const val defaultJourneyId = "J1"
const val defaultMoveEventId = "ME1"
const val defaultJourneyEventId = "JE1"
const val defaultProfileId = "PR1"
const val defaultPersonId = "PE1"

val defaultSupplierSerco = Supplier.SERCO
val defaultMoveTypeStandard = MoveType.STANDARD

fun fromPrisonNomisAgencyId() = "WYI"
fun WYIPrisonLocation() =
  Location(id = UUID.randomUUID(), locationType = LocationType.PR, nomisAgencyId = "WYI", siteName = "from")

fun toCourtNomisAgencyId() = "GNI"
fun GNICourtLocation() =
  Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "GNI", siteName = "to")

fun notMappedNomisAgencyId() = "NOT_MAPPED_AGENCY_ID"

fun reportMoveFactory(
  moveId: String = defaultMoveId,
  supplier: Supplier = defaultSupplierSerco,
  profileId: String = defaultProfileId,
  status: MoveStatus = MoveStatus.completed,
  fromLocation: String = fromPrisonNomisAgencyId(),
  fromLocationType: String = "prison",
  toLocation: String = toCourtNomisAgencyId(),
  toLocationType: String = "court",
  cancellationReason: String = "",
  date: LocalDate = defaultDate,
  events: List<Event> = listOf(),
  journeys: List<Journey> = listOf(),
): Move {
  val move = Move(
    moveId = moveId,
    updatedAt = defaultDateTime,
    supplier = supplier,
    profileId = profileId,
    reference = "UKW4591N",
    moveDate = date,
    status = status,
    fromNomisAgencyId = fromLocation,
    reportFromLocationType = fromLocationType,
    toNomisAgencyId = toLocation,
    reportToLocationType = toLocationType,
    cancellationReason = cancellationReason,
    notes = "",
    dropOffOrCancelledDateTime = null,
    moveType = null,
    events = events,
    journeys = journeys,
  )
  return move
}

fun reportPersonFactory(personId: String = defaultPersonId): Person {
  return Person(
    personId = personId,
    updatedAt = defaultDateTime,
    prisonNumber = "PRISON1",
    latestNomisBookingId = null,
    firstNames = "Billy the",
    lastName = "Kid",
    dateOfBirth = LocalDate.of(1980, 12, 25),
    ethnicity = "White American",
    gender = "male",
  )
}

fun profileFactory(
  profileId: String = defaultProfileId,
  personId: String = defaultPersonId,
): Profile {
  return Profile(profileId, defaultDateTime, personId)
}

fun moveEventFactory(
  eventId: String = defaultMoveEventId,
  moveId: String = defaultMoveId,
  type: String = EventType.MOVE_CANCEL.value,
  supplier: Supplier = defaultSupplierSerco,
  occurredAt: LocalDateTime = defaultDateTime,
  notes: String = "",
): Event {
  val event = Event(
    eventId = eventId,
    updatedAt = defaultDateTime,
    type = type,
    supplier = supplier,
    eventableType = "move",
    eventableId = moveId,
    details = mapOf("string_key" to "string_val", "int_key" to 3, "bool_key" to true),
    occurredAt = occurredAt,
    recordedAt = defaultDateTime,
    notes = notes,
  )
  return event
}

fun reportJourneyFactory(
  journeyId: String = defaultJourneyId,
  moveId: String = defaultMoveId,
  state: JourneyState = JourneyState.completed,
  supplier: Supplier = defaultSupplierSerco,
  billable: Boolean = false,
  fromLocation: String = fromPrisonNomisAgencyId(),
  toLocation: String = toCourtNomisAgencyId(),
  vehicleRegistration: String? = "UHE-92",
  effectiveYear: Int? = null,
  events: List<Event> = listOf(),

): Journey {
  val journey = Journey(
    journeyId = journeyId,
    updatedAt = defaultDateTime,
    moveId = moveId,
    clientTimeStamp = defaultDateTime,
    billable = billable,
    state = state,
    supplier = supplier,
    vehicleRegistration = vehicleRegistration,
    fromNomisAgencyId = fromLocation,
    toNomisAgencyId = toLocation,
    effectiveYear = effectiveYear,
    events = events,
  )
  return journey
}

fun journeyEventFactory(
  journeyEventId: String = defaultJourneyEventId,
  journeyId: String = defaultJourneyId,
  type: String = EventType.JOURNEY_START.value,
  supplier: Supplier = defaultSupplierSerco,
  occurredAt: LocalDateTime = defaultDateTime,
  notes: String = "",
): Event {
  val event = Event(
    eventId = journeyEventId,
    updatedAt = defaultDateTime,
    type = type,
    supplier = supplier,
    eventableType = "journey",
    eventableId = journeyId,
    details = null,
    occurredAt = occurredAt,
    recordedAt = defaultDateTime,
    notes = notes,
  )
  return event
}
