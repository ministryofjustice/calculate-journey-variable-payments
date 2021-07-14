package uk.gov.justice.digital.hmpps.pecs.jpc.integration.pages

import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.move.Move
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveStatus
import uk.gov.justice.digital.hmpps.pecs.jpc.move.MoveType
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.time.LocalDateTime

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
    gender = "male"
  )

private val bonnieElizabeth =
  Person(
    personId = "_",
    updatedAt = LocalDateTime.now(),
    prisonNumber = "PRISONER3",
    firstNames = "Bonnie Elizabeth",
    lastName = "Parker",
    dateOfBirth = LocalDate.of(1910, 10, 1),
    gender = "female"
  )

fun standardMoveM4() =
  Move(
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
      gender = "male"
    )
  )

fun longHaulMoveM30() =
  Move(
    moveId = "M30",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.LONG_HAUL,
    status = MoveStatus.completed,
    reference = "LONGMOVE",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "BOG",
    toSiteName = "BOG", // Test data is not mapped so will default to the agency ID
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = LocalDateTime.of(2020, 12, 3, 12, 0),
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 4, 11, 20),
    person = billyTheKid
  )

fun redirectMoveM20() =
  Move(
    moveId = "M20",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.REDIRECTION,
    status = MoveStatus.completed,
    reference = "REDIRMOVE",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "GNI",
    toSiteName = "GNI", // Test data is not mapped so will default to the agency ID
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = LocalDateTime.of(2020, 12, 2, 10, 20),
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 2, 14, 20),
    person = bonnieElizabeth
  )

fun lockoutMoveM40() =
  Move(
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
    person = bonnieElizabeth
  )

fun multiMoveM50() =
  Move(
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
    person = billyTheKid
  )

fun cancelledMoveM60() =
  Move(
    moveId = "M60",
    updatedAt = LocalDateTime.now(),
    supplier = Supplier.GEOAMEY,
    moveType = MoveType.CANCELLED,
    status = MoveStatus.completed,
    reference = "CANCELLED_BEFORE_3PM",
    fromNomisAgencyId = "PRISON1",
    fromSiteName = "PRISON ONE",
    toNomisAgencyId = "GNI",
    toSiteName = "GNI", // Test data is not mapped so will default to the agency ID
    reportFromLocationType = "prison",
    reportToLocationType = "prison",
    pickUpDateTime = null,
    dropOffOrCancelledDateTime = LocalDateTime.of(2020, 12, 6, 14, 59),
    person = billyTheKid
  )
