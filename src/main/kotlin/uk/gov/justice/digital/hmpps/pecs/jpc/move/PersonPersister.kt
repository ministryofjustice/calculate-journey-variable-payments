package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Profile

@Component
class PersonPersister(
  private val personRepository: PersonRepository,
  private val profileRepository: ProfileRepository
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  fun persistPeople(people: List<Person>) {
    logger.info("Persisting ${people.size} people")
    var counter = 1
    val peopleToSave = mutableListOf<Person>()
    people.forEach { person ->
      Result.runCatching {
        peopleToSave += person
        if (counter++ % 1000 == 0) {
          logger.info("Persisted $counter people out of ${people.size} (flushing people to the database).")
          saveFlushAndClear(personRepository, peopleToSave)
        }
        saveFlushAndClear(personRepository, peopleToSave)
      }.onFailure { logger.warn("Error inserting person id ${person.personId}" + it.message) }
    }
  }

  fun persistProfiles(profiles: List<Profile>) {
    logger.info("Persisting ${profiles.size} profiles")
    var counter = 1
    val profilesToSave = mutableListOf<Profile>()
    profiles.forEach { profile ->
      Result.runCatching {
        profilesToSave += profile
        if (counter++ % 1000 == 0) {
          logger.info("Persisted $counter profiles out of ${profiles.size} (flushing profiles to the database).")
          saveFlushAndClear(profileRepository, profilesToSave)
        }
        saveFlushAndClear(profileRepository, profilesToSave)
      }.onFailure { logger.warn("Error inserting $profile" + it.message) }
    }
  }
}
