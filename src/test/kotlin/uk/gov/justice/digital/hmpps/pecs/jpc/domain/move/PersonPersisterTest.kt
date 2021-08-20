package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.profileFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportPersonFactory
import java.time.LocalDate
import java.time.LocalDateTime

private class PersonMatcher(var person: Person) : ArgumentMatcher<Person> {
  override fun matches(otherPerson: Person): Boolean {
    return person.personId == otherPerson.personId
  }
}

private class ProfileMatcher(var profile: Profile) : ArgumentMatcher<Profile> {
  override fun matches(otherProfile: Profile): Boolean {
    return profile.profileId == otherProfile.profileId &&
      profile.personId == profile.personId
  }
}

@ActiveProfiles("test")
@DataJpaTest
internal class PersonPersisterTest(
  @Autowired private val personRepository: PersonRepository,
  @Autowired private val profileRepository: ProfileRepository,
  @Autowired private val entityManager: TestEntityManager
) {
  private val personRepositorySpy: PersonRepository = mock { spy(personRepository) }
  private val profileRepositorySpy: ProfileRepository = mock { spy(profileRepository) }

  @Test
  fun `Persist PII data`() {
    val reportPerson = reportPersonFactory()
    PersonPersister(personRepository, profileRepository).persistPeople(listOf(reportPerson))

    entityManager.flush()

    val retrievedPerson = personRepository.findById(reportPerson.personId).get()

    // PII data should be populated
    assertThat(retrievedPerson.ethnicity).isEqualTo("White American")
    assertThat(retrievedPerson.gender).isEqualTo("male")
    assertThat(retrievedPerson.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
    assertThat(retrievedPerson.firstNames).isEqualTo("Billy the")
    assertThat(retrievedPerson.lastName).isEqualTo("Kid")
  }

  @Test
  fun `save invoked once for 1 person`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistPeople(entities(1) { id -> reportPersonFactory().copy(personId = id) })

    verify(personRepositorySpy).saveAndFlush(any())
  }

  @Test
  fun `save invoked 50 times for 50 people`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistPeople(entities(50) { id -> reportPersonFactory().copy(personId = id) })

    verify(personRepositorySpy, times(50)).saveAndFlush(any())
  }

  @Test
  fun `save invoked 51 times for 51 people`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistPeople(entities(51) { id -> reportPersonFactory().copy(personId = id) })

    verify(personRepositorySpy, times(51)).saveAndFlush(any())
  }

  @Test
  fun `persistPeople returns 4 when 2 of 6 people are invalid`() {
    whenever(
      personRepositorySpy.saveAndFlush(
        argThat(
          PersonMatcher(
            Person(
              "invalid",
              LocalDateTime.now(),
              "invalid"
            )
          )
        )
      )
    ).thenThrow(
      RuntimeException("An error message")
    )

    assertThat(
      PersonPersister(
        personRepositorySpy,
        profileRepositorySpy
      ).persistPeople(
        entities(2) { reportPersonFactory().copy(personId = "invalid") } +
          entities(4) { id -> reportPersonFactory().copy(personId = id) }
      )
    ).isEqualTo(4)

    verify(personRepositorySpy, times(6)).saveAndFlush(any())
  }

  @Test
  fun `save invoked once for 1 profile`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistProfiles(entities(1) { id -> profileFactory().copy(profileId = id, personId = id) })

    verify(profileRepositorySpy, times(1)).saveAndFlush(any())
  }

  @Test
  fun `save invoked 50 times for 50 profiles`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistProfiles(entities(50) { id -> profileFactory().copy(profileId = id, personId = id) })

    verify(profileRepositorySpy, times(50)).saveAndFlush(any())
  }

  @Test
  fun `save invoked 51 times for 51 profiles`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistProfiles(entities(51) { id -> profileFactory().copy(profileId = id, personId = id) })

    verify(profileRepositorySpy, times(51)).saveAndFlush(any())
  }

  @Test
  fun `persistProfiles returns 4 when 2 of 6 profiles are invalid`() {
    whenever(
      profileRepositorySpy.saveAndFlush(
        argThat(
          ProfileMatcher(
            Profile(
              "invalid",
              LocalDateTime.now(),
              "invalid"
            )
          )
        )
      )
    ).thenThrow(
      RuntimeException("An error message")
    )

    assertThat(
      PersonPersister(
        personRepositorySpy,
        profileRepositorySpy
      ).persistProfiles(
        entities(2) { profileFactory().copy(profileId = "invalid", personId = "invalid") } +
          entities(4) { id -> profileFactory().copy(profileId = id, personId = id) }
      )
    ).isEqualTo(4)

    verify(profileRepositorySpy, times(6)).saveAndFlush(any())
  }

  fun <T> entities(numberOf: Int, f: (id: String) -> T): List<T> =
    MutableList(numberOf) { index -> f(index.toString()) }
}
