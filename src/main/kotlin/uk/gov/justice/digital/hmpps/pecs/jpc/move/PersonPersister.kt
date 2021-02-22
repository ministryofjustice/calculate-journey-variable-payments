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

  /**
   * Returns the total number of successfully persisted people.
   */
  fun persistPeople(people: List<Person>): Int {
    logger.info("Persisting ${people.size} people")
    var counter = 0
    val peopleToSave = mutableListOf<Person>()
    people.forEach { person ->
      peopleToSave += person
      if (++counter % 50 == 0) {
        savePeople(peopleToSave) { logger.info("Persisted $counter people out of ${people.size} (flushing people to the database).") }
      }
    }

    if (peopleToSave.isNotEmpty()) savePeople(peopleToSave) { logger.info("Persisted $counter people out of ${people.size} (flushing people to the database).") }

    return counter
  }

  private fun savePeople(people: MutableList<Person>, success: () -> Unit) {
    Result.runCatching {
      saveFlushAndClear(personRepository, people)
    }
      .onSuccess { success() }
      .onFailure {
        logger.warn("Error inserting people batch ${people.map { p -> p.personId }} - ${it.stackTraceToString()}")
        people.clear()
      }
  }

  /**
   * Returns the total number of successfully persisted profiles.
   */
  fun persistProfiles(profiles: List<Profile>): Int {
    logger.info("Persisting ${profiles.size} profiles")
    var counter = 0
    val profilesToSave = mutableListOf<Profile>()
    profiles.forEach { profile ->
      profilesToSave += profile

      if (++counter % 50 == 0) {
        saveProfiles(profilesToSave) { logger.info("Persisted $counter profiles out of ${profiles.size} (flushing profiles to the database).") }
      }
    }

    if (profilesToSave.isNotEmpty()) saveProfiles(profilesToSave) { logger.info("Persisted $counter profiles out of ${profiles.size} (flushing profiles to the database).") }

    return counter
  }

  private fun saveProfiles(profiles: MutableList<Profile>, success: () -> Unit) {
    Result.runCatching {
      saveFlushAndClear(profileRepository, profiles)
    }
      .onSuccess { success() }
      .onFailure {
        logger.warn("Error inserting profiles batch ${profiles.map { p -> p.profileId }} - ${it.message}")
        profiles.clear()
      }
  }
}
