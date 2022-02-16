package uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile

import org.springframework.stereotype.Component

@Component
open class PersonPersister(
  private val personRepository: PersonRepository,
  private val profileRepository: ProfileRepository
) {

  open fun persistPerson(person: Person, success: () -> Unit, failure: (Throwable) -> Unit) {
    Result.runCatching {
      personRepository.saveAndFlush(person)
    }.onSuccess {
      success()
    }.onFailure {
      failure(it)
    }
  }

  open fun persistProfile(profile: Profile, success: () -> Unit, failure: (Throwable) -> Unit) {
    Result.runCatching {
      profileRepository.saveAndFlush(profile)
    }.onSuccess {
      success()
    }.onFailure {
      failure(it)
    }
  }
}
