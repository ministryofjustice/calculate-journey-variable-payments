package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person

@Component
class PersonPersister(private val moveRepository: MoveRepository, private val timeSource: TimeSource) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persist(people: List<Person>) {

        logger.info("Persisting ${people.size} people")

        people.forEach { person ->
            Result.runCatching {

                val maybeMove = moveRepository.findByProfileId(person.profileId!!)
                maybeMove.ifPresent {
                    val moveWithPII = it.copy(
                        dateOfBirth = person.dateOfBirth,
                        firstNames = person.firstNames,
                        lastName = person.lastName,
                        prisonNumber = person.prisonNumber,
                        gender = person.gender,
                        ethnicity = person.ethnicity,
                        latestNomisBookingId = person.latestNomisBookingId
                    )
                    moveRepository.save(moveWithPII)
                }
            }
        }
    }
}

