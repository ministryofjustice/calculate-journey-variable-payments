package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.util.loggerFor

private val logger = loggerFor<PersonPersister>()

@Component
class PersonPersister(
  private val personRepository: PersonRepository,
  private val profileRepository: ProfileRepository
) {

  /**
   * Returns the total number of successfully persisted people.
   */
  fun persistPeople(people: Sequence<Person>): PersistenceResult {
    var saveCounter = 0
    var errorCounter = 0

    @Transactional
    fun persistPerson(person: Person) = personRepository.saveAndFlush(person)

    people.forEach { person ->
      Result.runCatching { persistPerson(person) }.onSuccess {
        saveCounter++
        if (saveCounter % 500 == 0) logger.info("Persisted $saveCounter people...")
      }.onFailure {
        errorCounter++
        logger.warn("Error persisting person ${person.personId} - ${it.message}")
      }
    }

    logger.info("Persisted $saveCounter people, $errorCounter errors occurred.")

    return PersistenceResult(saveCounter, errorCounter)
  }

  fun persistProfiles(profiles: Sequence<Profile>): PersistenceResult {
    logger.info("Persisting profiles")

    var saveCounter = 0
    var errorCounter = 0

    profiles.forEach { profile ->
      Result.runCatching {
        profileRepository.saveAndFlush(profile)
      }.onSuccess {
        saveCounter++
        if (saveCounter % 500 == 0) logger.info("Persisted $saveCounter profiles...")
      }.onFailure {
        errorCounter++
        logger.warn("Error persisting profile ${profile.profileId} - ${it.message}")
      }
    }

    logger.info("Persisted $saveCounter profiles, $errorCounter errors occurred.")

    return PersistenceResult(saveCounter, errorCounter)
  }
}
