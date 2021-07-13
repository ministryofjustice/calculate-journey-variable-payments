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

fun moveM4() =
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
