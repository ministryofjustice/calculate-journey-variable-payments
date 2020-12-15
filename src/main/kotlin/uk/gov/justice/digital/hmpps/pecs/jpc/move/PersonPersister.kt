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
        people.forEach { person ->
            Result.runCatching { personRepository.save(person) }
        }
    }

    fun persistProfiles(profiles: List<Profile>) {
        logger.info("Persisting ${profiles.size} people")
        profiles.forEach { profile ->
            Result.runCatching { profileRepository.save(profile) }
        }
    }
}

