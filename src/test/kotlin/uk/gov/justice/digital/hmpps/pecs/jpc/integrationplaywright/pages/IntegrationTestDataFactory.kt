package uk.gov.justice.digital.hmpps.pecs.jpc.integrationplaywright.pages

import uk.gov.justice.digital.hmpps.pecs.jpc.domain.event.Event
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.Journey
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.journey.JourneyState
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.effectiveYearForDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year

/**
 * The integration test data comes from the DEV Spring Profile data loaded via the database migration scripts at startup.
 */
private val billyTheKid =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER1",
    firstNames = "Billy the",
    lastName = "Kid",
    dateOfBirth = LocalDate.of(1980, 12, 25),
    gender = "male",
  )

private val jonnyLodger =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER45",
    firstNames = "Jonny",
    lastName = "Lodger",
    dateOfBirth = LocalDate.of(1970, 10, 1),
    gender = "male",
  )

private val bonnieElizabeth =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER3",
    firstNames = "Bonnie Elizabeth",
    lastName = "Parker",
    dateOfBirth = LocalDate.of(1910, 10, 1),
    gender = "female",
  )

private val ronnieBiggs =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER4",
    firstNames = "Ronnie",
    lastName = "Biggs",
    dateOfBirth = LocalDate.of(1929, 8, 8),
    gender = "male",
  )

private val fredBloggs =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER5",
    firstNames = "Fred",
    lastName = "Bloggs",
    dateOfBirth = LocalDate.of(1950, 4, 12),
    gender = "male",
  )

private val janeBloggs =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER6",
    firstNames = "Jane",
    lastName = "Bloggs",
    dateOfBirth = LocalDate.of(1961, 10, 22),
    gender = "female",
  )

private val donaldDuck =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER7",
    firstNames = "Donald",
    lastName = "Duck",
    dateOfBirth = LocalDate.of(1940, 9, 27),
    gender = "male",
  )

private val professorMoriarty =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER8",
    firstNames = "Professor",
    lastName = "Moriarty",
    dateOfBirth = LocalDate.of(1880, 7, 10),
    gender = "male",
  )

object Dec2020MoveData {

  fun standardMoveM4() = Move(
    moveId = "M4",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.STANDARD,
    status = MoveStatus.completed,
    reference = "STANDARDMOVE2",
    fromNomisAgencyId = "PRISON3",
    fromSiteName = "PRISON THREE",
    toNomisAgencyId = "PRISON4",
    toSiteName = "PRISON FOUR",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = LocalDateTime.of(2020, 12, 1, 10, 20),
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 1, 12, 20),
    person = Person(
      personId = "_",
      updatedAt = LocalDateTime.now(),
      prisonNumber = "PRISONER2",
      firstNames = "Clyde Chestnut",
      lastName = "Barrow",
      dateOfBirth = LocalDate.of(1909, 3, 24),
      gender = "male",
    ),
  )

  fun longHaulMoveM30() = Move(
    moveId = "M30",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.LONG_HAUL,
    status = MoveStatus.completed,
    reference = "LONGMOVE",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "BOG",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "BOG",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = LocalDateTime.of(2020, 12, 3, 12, 0),
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 4, 11, 20),
    person = billyTheKid,
  )

  fun redirectMoveM20() = Move(
    moveId = "M20",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.REDIRECTION,
    status = MoveStatus.completed,
    reference = "REDIRMOVE",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "GNI",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "GNI",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = LocalDateTime.of(2020, 12, 2, 10, 20),
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 2, 14, 20),
    person = bonnieElizabeth,
  )

  fun lockoutMoveM40() = Move(
    moveId = "M40",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.LOCKOUT,
    status = MoveStatus.completed,
    reference = "LOCKMOVE",
    fromNomisAgencyId = "COURT1",
    fromSiteName = "COURT ONE",
    toNomisAgencyId = "PRISON1",
    toSiteName = "PRISON ONE",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = LocalDateTime.of(2020, 12, 4, 10, 20),
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 5, 10, 20),
    person = bonnieElizabeth,
  )

  fun multiMoveM50() = Move(
    moveId = "M50",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.MULTI,
    status = MoveStatus.completed,
    reference = "MULTIMOVE",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "PRISON3",
    toSiteName = "PRISON THREE",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = LocalDateTime.of(2020, 12, 5, 10, 20),
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 6, 10, 20),
    person = billyTheKid,
  )

  fun cancelledMoveM60() = Move(
    moveId = "M60",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.CANCELLED,
    status = MoveStatus.completed,
    reference = "CANCELLED_BEFORE_3PM",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "GNI",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "GNI",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = null,
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 6, 14, 59),
    person = billyTheKid,
  )
}

object SercoPreviousMonthMoveData {

  private val startOfPreviousMonth = LocalDate.now().minusMonths(1).withDayOfMonth(1)

  private const val START_HOURS_OFFSET = 10L

  private const val END_HOURS_OFFSET = 12L

  fun standardMoveSM1() = Move(
    moveId = "SM1",
    updatedAt = startOfPreviousMonth.atStartOfDay(),
    supplier = Supplier.SERCO,
    moveType = MoveType.STANDARD,
    status = MoveStatus.completed,
    reference = "STANDARDSM1",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "PRISON2",
    toSiteName = "PRISON TWO",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = startOfPreviousMonth.atStartOfDay().plusHours(START_HOURS_OFFSET),
    dropOffOrCancelledDateTime = startOfPreviousMonth.atStartOfDay().plusHours(END_HOURS_OFFSET),
    person = billyTheKid,
  )

  fun redirectMoveRM1() = Move(
    moveId = "RM1",
    updatedAt = startOfPreviousMonth.atStartOfDay(),
    supplier = Supplier.SERCO,
    moveType = MoveType.REDIRECTION,
    status = MoveStatus.completed,
    reference = "REDIRECTIONRM1",
    fromNomisAgencyId = "FROM_AGENCY",
    fromSiteName = "FROM_AGENCY",
    toNomisAgencyId = "TO_AGENCY",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "TO_AGENCY",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = startOfPreviousMonth.atStartOfDay().plusHours(START_HOURS_OFFSET),
    dropOffOrCancelledDateTime = startOfPreviousMonth.atStartOfDay().plusHours(END_HOURS_OFFSET),
    person = ronnieBiggs,
  )

  fun longHaulMoveLHM1() = Move(
    moveId = "LHM1",
    updatedAt = startOfPreviousMonth.atStartOfDay(),
    supplier = Supplier.SERCO,
    moveType = MoveType.LONG_HAUL,
    status = MoveStatus.completed,
    reference = "LONG_HAULLHM1",
    fromNomisAgencyId = "FROM_AGENCY",
    fromSiteName = "FROM_AGENCY",
    toNomisAgencyId = "TO_AGENCY",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "TO_AGENCY",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = startOfPreviousMonth.atStartOfDay().plusHours(START_HOURS_OFFSET),
    dropOffOrCancelledDateTime = startOfPreviousMonth.atStartOfDay().plusHours(END_HOURS_OFFSET).plusDays(1),
    person = fredBloggs,
  )

  fun lockoutMoveLM1() = Move(
    moveId = "LM1",
    updatedAt = startOfPreviousMonth.atStartOfDay(),
    supplier = Supplier.SERCO,
    moveType = MoveType.LOCKOUT,
    status = MoveStatus.completed,
    reference = "LOCKOUTLM1",
    fromNomisAgencyId = "FROM_AGENCY",
    fromSiteName = "FROM_AGENCY",
    toNomisAgencyId = "TO_AGENCY",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "TO_AGENCY",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = startOfPreviousMonth.atStartOfDay().plusHours(START_HOURS_OFFSET),
    dropOffOrCancelledDateTime = startOfPreviousMonth.atStartOfDay().plusHours(END_HOURS_OFFSET).plusDays(1),
    person = janeBloggs,
  )

  fun multiMoveMM1() = Move(
    moveId = "MM1",
    updatedAt = startOfPreviousMonth.atStartOfDay(),
    supplier = Supplier.SERCO,
    moveType = MoveType.MULTI,
    status = MoveStatus.completed,
    reference = "MULTIMM1",
    fromNomisAgencyId = "FROM_AGENCY",
    fromSiteName = "FROM_AGENCY",
    toNomisAgencyId = "TO_AGENCY4",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "TO_AGENCY4",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = startOfPreviousMonth.atStartOfDay().plusHours(START_HOURS_OFFSET),
    dropOffOrCancelledDateTime = startOfPreviousMonth.atStartOfDay().plusHours(END_HOURS_OFFSET).plusDays(1),
    person = donaldDuck,
  )

  fun cancelledMoveCM1() = Move(
    moveId = "CM1",
    updatedAt = startOfPreviousMonth.atStartOfDay(),
    supplier = Supplier.SERCO,
    moveType = MoveType.CANCELLED,
    status = MoveStatus.completed,
    reference = "CANCELLEDCM1",
    fromNomisAgencyId = "FROM_AGENCY",
    fromSiteName = "FROM_AGENCY",
    toNomisAgencyId = "TO_AGENCY",
    // Test data is not mapped so will default to the agency ID
    toSiteName = "TO_AGENCY",
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = startOfPreviousMonth.atStartOfDay().plusHours(START_HOURS_OFFSET),
    dropOffOrCancelledDateTime = startOfPreviousMonth.atStartOfDay().plusHours(END_HOURS_OFFSET),
    person = professorMoriarty,
  )

  fun standardMoveSM4() = startOfPreviousMonth.minusMonths(1).let { moveDate ->
    Move(
      moveId = "SM4",
      updatedAt = moveDate.atStartOfDay(),
      supplier = Supplier.SERCO,
      moveType = MoveType.STANDARD,
      status = MoveStatus.completed,
      reference = "STANDARDSM4",
      fromNomisAgencyId = "PRISON1",
      fromSiteName = "PRISON ONE",
      toNomisAgencyId = "POLICE1",
      toSiteName = "POLICE ONE",
      reportFromLocationType = "prison",
      reportToLocationType = "police",
      pickUpDateTime = moveDate.atStartOfDay().plusHours(START_HOURS_OFFSET),
      dropOffOrCancelledDateTime = moveDate.atStartOfDay().plusHours(END_HOURS_OFFSET),
      person = billyTheKid,
    )
  }

  fun journeyLDGJ1(
    moveId: String = "LDGM1",
    journeyId: String = "LDGJ1",
    fromNomisAgencyId: String = "PRISON1L",
    toNomisAgencyId: String = "POLICE1L",
    state: JourneyState = JourneyState.completed,
    billable: Boolean = true,
    events: List<Event> = listOf(),
    vehicleRegistration: String = "REG100",
  ) = startOfPreviousMonth.minusMonths(1).let { moveDate ->
    Journey(
      journeyId = journeyId,
      supplier = Supplier.SERCO,
      clientTimeStamp = moveDate.atStartOfDay(),
      updatedAt = moveDate.atTime(10, 0),
      state = state,
      moveId = moveId,
      fromNomisAgencyId = fromNomisAgencyId,
      fromSiteName = "PRISON ONE L",
      fromLocationType = LocationType.PR,
      toNomisAgencyId = toNomisAgencyId,
      toSiteName = "POLICE ONE L",
      toLocationType = LocationType.PS,
      pickUpDateTime = moveDate.atStartOfDay(),
      dropOffDateTime = moveDate.atTime(10, 0),
      billable = billable,
      priceInPence = 100,
      vehicleRegistration = vehicleRegistration,
      notes = "some notes",
      effectiveYear = effectiveYearForDate(
        moveDate,
      ),
    )
  }

  fun journeyLDGJ2(
    moveId: String = "LDGM1",
    journeyId: String = "LDGJ2",
    fromNomisAgencyId: String = "POLICE1L",
    toNomisAgencyId: String = "POLICE2L",
    state: JourneyState = JourneyState.completed,
    billable: Boolean = true,
    events: List<Event> = listOf(),
    vehicleRegistration: String = "REG100",
  ) = startOfPreviousMonth.minusMonths(1).plusDays(1).let { moveDate ->
    Journey(
      journeyId = journeyId,
      supplier = Supplier.SERCO,
      clientTimeStamp = moveDate.atStartOfDay(),
      updatedAt = moveDate.atTime(10, 0),
      state = state,
      moveId = moveId,
      fromNomisAgencyId = fromNomisAgencyId,
      fromSiteName = "POLICE ONE L",
      fromLocationType = LocationType.PS,
      toNomisAgencyId = toNomisAgencyId,
      toSiteName = "POLICE TWO L",
      toLocationType = LocationType.PS,
      pickUpDateTime = moveDate.atStartOfDay(),
      dropOffDateTime = moveDate.atTime(10, 0),
      billable = billable,
      priceInPence = 100,
      vehicleRegistration = vehicleRegistration,
      notes = "some notes",
      events = events,
      effectiveYear = effectiveYearForDate(
        moveDate,
      ),
    )
  }

  fun lodgingMoveLDGM1() = startOfPreviousMonth.minusMonths(1).let { moveDate ->
    Move(
      moveId = "LDGM1",
      updatedAt = moveDate.atStartOfDay(),
      supplier = Supplier.SERCO,
      moveType = MoveType.LONG_HAUL,
      status = MoveStatus.completed,
      reference = "LONG_HAULLDGM1",
      fromNomisAgencyId = "PRISON1L",
      fromSiteName = "PRISON ONE L",
      toNomisAgencyId = "POLICE2L",
      toSiteName = "POLICE TWO L",
      reportFromLocationType = "prison",
      reportToLocationType = "police",
      pickUpDateTime = moveDate.atStartOfDay().plusHours(START_HOURS_OFFSET),
      dropOffOrCancelledDateTime = moveDate.plusDays(1).atStartOfDay().plusHours(END_HOURS_OFFSET),
      person = jonnyLodger,
      journeys = listOf(
        journeyLDGJ1(),
        journeyLDGJ2(),
      ),
    )
  }
}

internal fun LocalDate.previousMonth() = this.minusMonths(1).month

internal fun LocalDate.previousMonthYear() = Year.of(this.minusMonths(1).year)
