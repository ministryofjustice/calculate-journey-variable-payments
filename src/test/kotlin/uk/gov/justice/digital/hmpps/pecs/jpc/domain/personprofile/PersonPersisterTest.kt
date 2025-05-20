package uk.gov.justice.digital.hmpps.pecs.jpc.domain.personprofile

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.profileFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.service.reports.reportPersonFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

private class PersonMatcher(var person: Person) : ArgumentMatcher<Person> {
  override fun matches(otherPerson: Person): Boolean = person.personId == otherPerson.personId
}

private class ProfileMatcher(var profile: Profile) : ArgumentMatcher<Profile> {
  override fun matches(otherProfile: Profile): Boolean = profile.profileId == otherProfile.profileId &&
    profile.personId == profile.personId
}

@ActiveProfiles("test")
@DataJpaTest
internal class PersonPersisterTest(
  @Autowired private val personRepository: PersonRepository,
  @Autowired private val profileRepository: ProfileRepository,
  @Autowired private val entityManager: TestEntityManager,
) {
  private val personRepositorySpy: PersonRepository = mock { spy(personRepository) }
  private val profileRepositorySpy: ProfileRepository = mock { spy(profileRepository) }

  @Test
  fun `Persist PII data`() {
    val reportPerson = reportPersonFactory()
    PersonPersister(personRepository, profileRepository).persistPerson(reportPerson, {}, {})

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
      profileRepositorySpy,
    ).persistPerson(entities(1) { id -> reportPersonFactory().copy(personId = id) }.first(), {}, {})

    verify(personRepositorySpy).saveAndFlush(any())
  }

  @Test
  fun `save invoked 50 times for 50 people`() {
    entities(50) { id -> reportPersonFactory().copy(personId = id) }.forEach {
      PersonPersister(
        personRepositorySpy,
        profileRepositorySpy,
      ).persistPerson(it, {}, {})
    }

    verify(personRepositorySpy, times(50)).saveAndFlush(any())
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
              "invalid",
            ),
          ),
        ),
      ),
    ).thenThrow(
      RuntimeException("An error message"),
    )

    val (persisted, errors) = AtomicInteger(0) to AtomicInteger(0)

    (
      entities(2) { reportPersonFactory().copy(personId = "invalid") } +
        entities(4) { id -> reportPersonFactory().copy(personId = id) }
      ).forEach {
      PersonPersister(
        personRepositorySpy,
        profileRepositorySpy,
      ).persistPerson(it, { persisted.incrementAndGet() }, { errors.incrementAndGet() })
    }

    assertThat(persisted.get()).isEqualTo(4)
    assertThat(errors.get()).isEqualTo(2)

    verify(personRepositorySpy, times(6)).saveAndFlush(any())
  }

  @Test
  fun `save invoked once for 1 profile`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy,
    ).persistProfile(entities(1) { id -> profileFactory().copy(profileId = id, personId = id) }.first(), {}, {})

    verify(profileRepositorySpy, times(1)).saveAndFlush(any())
  }

  @Test
  fun `save invoked 50 times for 50 profiles`() {
    entities(50) { id -> profileFactory().copy(profileId = id, personId = id) }.forEach {
      PersonPersister(
        personRepositorySpy,
        profileRepositorySpy,
      ).persistProfile(it, {}, {})
    }

    verify(profileRepositorySpy, times(50)).saveAndFlush(any())
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
              "invalid",
            ),
          ),
        ),
      ),
    ).thenThrow(
      RuntimeException("An error message"),
    )

    val (persisted, errors) = AtomicInteger(0) to AtomicInteger(0)

    (
      entities(2) { profileFactory().copy(profileId = "invalid", personId = "invalid") } +
        entities(4) { id -> profileFactory().copy(profileId = id, personId = id) }
      ).forEach {
      PersonPersister(
        personRepositorySpy,
        profileRepositorySpy,
      ).persistProfile(it, { persisted.incrementAndGet() }, { errors.incrementAndGet() })
    }

    assertThat(persisted.get()).isEqualTo(4)
    assertThat(errors.get()).isEqualTo(2)

    verify(profileRepositorySpy, times(6)).saveAndFlush(any())
  }

  fun <T> entities(numberOf: Int, f: (id: String) -> T): Sequence<T> = MutableList(numberOf) { index -> f(index.toString()) }.asSequence()
}
