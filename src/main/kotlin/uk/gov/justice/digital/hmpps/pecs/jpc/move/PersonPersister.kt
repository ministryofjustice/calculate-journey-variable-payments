package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Profile

@Component
class PersonPersister(private val personRepository: PersonRepository,
                      private val profileRepository: ProfileRepository,
                      private val timeSource: TimeSource) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun persistPeople(people: List<Person>) {
        logger.info("Persisting ${people.size} people")
        var counter = 1
        people.forEach { person ->
            Result.runCatching { personRepository.save(person)
                if (counter++ % 1000 == 0) {
                    logger.info("Persisted $counter moves out of ${people.size} (flushing moves to the database).")
                    personRepository.flush()
                }
            }.onFailure { logger.warn("Error inserting person id ${person.personId}" + it.message) }
        }
    }

    fun persistProfiles(profiles: List<Profile>) {
        logger.info("Persisting ${profiles.size} profiles")
        var counter = 1
        profiles.forEach { profile ->
            Result.runCatching { profileRepository.save(profile)
                if (counter++ % 1000 == 0) {
                    logger.info("Persisted $counter moves out of ${profiles.size} (flushing moves to the database).")
                    profileRepository.flush()
                }
            }.onFailure { logger.warn("Error inserting $profile" + it.message) }
        }
    }
}

