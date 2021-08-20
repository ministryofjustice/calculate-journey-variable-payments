package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.service.spreadsheet.inbound.report.Profile

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
    var saveCounter = 0
    var errorCounter = 0

    @Transactional
    fun persistPerson(person: Person) = personRepository.saveAndFlush(person)

    logger.info("Persisting ${people.size} people")
    people.forEach { person ->
      Result.runCatching { persistPerson(person) }.onSuccess {
        saveCounter++
        if (saveCounter % 50 == 0) logger.info("Persisted $saveCounter people out of ${people.size}.")
      }.onFailure {
        errorCounter++
        logger.warn("Error persisting person ${person.personId} - ${it.message}")
      }
    }

    logger.info("Persisted $saveCounter people out of ${people.size}, $errorCounter errors occurred.")

    return saveCounter
  }

  /**
   * Returns the total number of successfully persisted profiles.
   */
  fun persistProfiles(profiles: List<Profile>): Int {
    logger.info("Persisting ${profiles.size} profiles")
    var saveCounter = 0
    var errorCounter = 0
    profiles.forEach { profile ->
      Result.runCatching {
        profileRepository.saveAndFlush(profile)
      }.onSuccess {
        saveCounter++
        if (saveCounter % 50 == 0) logger.info("Persisted $saveCounter profiles out of ${profiles.size}.")
      }.onFailure {
        errorCounter++
        logger.warn("Error persisting profile ${profile.profileId} - ${it.message}")
      }
    }

    logger.info("Persisted $saveCounter profiles out of ${profiles.size}, $errorCounter errors occurred.")

    return saveCounter
  }
}
