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

const val DEFAULT_MOVE_ID = "M1"
const val DEFAULT_JOURNEY_ID = "J1"
const val DEFAULT_MOVE_EVENT_ID = "ME1"
const val DEFAULT_JOURNEY_EVENT_ID = "JE1"
const val DEFAULT_PROFILE_ID = "PR1"
const val DEFAULT_PERSON_ID = "PE1"

val defaultSupplierSerco = Supplier.SERCO
val defaultMoveTypeStandard = MoveType.STANDARD

fun fromPrisonNomisAgencyId() = "WYI"

@Suppress("ktlint:standard:function-naming")
fun WYIPrisonLocation() =
  Location(id = UUID.randomUUID(), locationType = LocationType.PR, nomisAgencyId = "WYI", siteName = "from")

fun toCourtNomisAgencyId() = "GNI"

@Suppress("ktlint:standard:function-naming")
fun GNICourtLocation() =
  Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "GNI", siteName = "to")

fun notMappedNomisAgencyId() = "NOT_MAPPED_AGENCY_ID"

fun reportMoveFactory(
  moveId: String = DEFAULT_MOVE_ID,
  supplier: Supplier = defaultSupplierSerco,
  profileId: String = DEFAULT_PROFILE_ID,
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

fun reportPersonFactory(personId: String = DEFAULT_PERSON_ID): Person {
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
  profileId: String = DEFAULT_PROFILE_ID,
  personId: String = DEFAULT_PERSON_ID,
): Profile {
  return Profile(profileId, defaultDateTime, personId)
}

fun moveEventFactory(
  eventId: String = DEFAULT_MOVE_EVENT_ID,
  moveId: String = DEFAULT_MOVE_ID,
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
  journeyId: String = DEFAULT_JOURNEY_ID,
  moveId: String = DEFAULT_MOVE_ID,
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
  journeyEventId: String = DEFAULT_JOURNEY_EVENT_ID,
  journeyId: String = DEFAULT_JOURNEY_ID,
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
