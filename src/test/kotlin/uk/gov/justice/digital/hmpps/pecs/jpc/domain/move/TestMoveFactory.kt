package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.EventType
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.defaultDate
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.defaultDateTime
import java.time.LocalDate
import java.time.LocalDateTime

val defaultMoveDate10Sep2020 = LocalDate.of(2020, 9, 10)

fun personPE1() = Person(
  personId = "PE1",
  updatedAt = defaultDateTime,
  prisonNumber = "PR101",
  firstNames = "Billy the",
  lastName = "Kid",
  gender = "male",
  ethnicity = "White",
  dateOfBirth = LocalDate.of(1980, 12, 25),
)

fun profilePR1() = Profile(
  profileId = "PR1",
  personId = "PE1",
  updatedAt = defaultDateTime
)

fun moveM1(
  moveId: String = "M1",
  fromNomisAgencyId: String = "WYI",
  toNomisAgencyId: String = "GNI",
  dropOffOrCancelledDateTime: LocalDateTime = defaultMoveDate10Sep2020.atStartOfDay().plusHours(10),
  person: Person? = personPE1(),
  journeys: List<Journey> = listOf()
) = Move(
  moveId = moveId,
  profileId = "PR1",
  updatedAt = defaultDateTime,
  supplier = Supplier.SERCO,
  moveType = MoveType.STANDARD,
  status = MoveStatus.completed,
  reference = "REF1",
  moveDate = defaultMoveDate10Sep2020,
  fromNomisAgencyId = fromNomisAgencyId,
  fromSiteName = "from",
  fromLocationType = LocationType.PR,
  toNomisAgencyId = toNomisAgencyId,
  toSiteName = "to",
  toLocationType = LocationType.PR,
  pickUpDateTime = defaultMoveDate10Sep2020.atStartOfDay(),
  dropOffOrCancelledDateTime = dropOffOrCancelledDateTime,
  reportFromLocationType = "prison",
  reportToLocationType = null,
  notes = "some notes",
  vehicleRegistration = "reg100",
  person = person,
  journeys = journeys
)

fun journeyJ1(
  moveId: String = moveM1().moveId,
  journeyId: String = "J1",
  fromNomisAgencyId: String = "WYI",
  toNomisAgencyId: String = "GNI",
  state: JourneyState = JourneyState.completed,
  billable: Boolean = true,
  pickUpDateTime: LocalDateTime? = defaultMoveDate10Sep2020.atStartOfDay(),
  dropOffDateTime: LocalDateTime? = defaultMoveDate10Sep2020.atStartOfDay().plusHours(10),
  events: List<Event> = listOf()
) = Journey(
  journeyId = journeyId,
  supplier = moveM1().supplier,
  clientTimeStamp = defaultDateTime,
  updatedAt = defaultDateTime,
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
  notes = "some notes",
  events = events,
  effectiveYear = effectiveYearForDate(
    defaultDate
  )
)

fun eventE1(
  eventId: String = "E1",
  eventType: EventType = EventType.MOVE_START,
  eventableId: String = moveM1().moveId
) = Event(
  eventId = eventId,
  updatedAt = defaultDateTime,
  type = eventType.value,
  eventableType = "move",
  eventableId = eventableId,
  occurredAt = defaultMoveDate10Sep2020.atStartOfDay(),
  recordedAt = defaultMoveDate10Sep2020.atStartOfDay(),
  notes = null,
  details = null,
  supplier = Supplier.SERCO
)

fun journeyEventJE1(
  eventId: String = "JE1",
  eventType: EventType = EventType.JOURNEY_START,
  eventableId: String = journeyJ1().journeyId
) = Event(
  details = null,
  eventableType = "journey",
  eventableId = eventableId,
  eventId = eventId,
  notes = null,
  occurredAt = journeyJ1().pickUpDateTime!!,
  recordedAt = journeyJ1().pickUpDateTime!!,
  supplier = Supplier.SERCO,
  type = eventType.value,
  updatedAt = defaultDateTime,
)
