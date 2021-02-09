package uk.gov.justice.digital.hmpps.pecs.jpc.move

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Person
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.Profile
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.profileFactory
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportPersonFactory
import java.time.LocalDate

@ActiveProfiles("test")
@DataJpaTest
internal class PersonPersisterTest(
  @Autowired private val personRepository: PersonRepository,
  @Autowired private val profileRepository: ProfileRepository,
  @Autowired private val entityManager: TestEntityManager
) {
  private val personRepositorySpy: PersonRepository = mock { spy(personRepository) }
  private val profileRepositorySpy: ProfileRepository = mock { spy(profileRepository) }
  private val personCaptor = argumentCaptor<List<Person>>()
  private val profileCaptor = argumentCaptor<List<Profile>>()

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

    verify(personRepositorySpy).saveAll(personCaptor.capture())
  }

  @Test
  fun `save invoked once for 500 people`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistPeople(entities(500) { id -> reportPersonFactory().copy(personId = id) })

    verify(personRepositorySpy).saveAll(personCaptor.capture())
  }

  @Test
  fun `save invoked twice for 501 people`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistPeople(entities(501) { id -> reportPersonFactory().copy(personId = id) })

    verify(personRepositorySpy, times(2)).saveAll(personCaptor.capture())
  }

  @Test
  fun `save invoked once for 1 profile`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistProfiles(entities(1) { id -> profileFactory().copy(profileId = id, personId = id) })

    verify(profileRepositorySpy).saveAll(profileCaptor.capture())
  }

  @Test
  fun `save invoked once for 500 profiles`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistProfiles(entities(500) { id -> profileFactory().copy(profileId = id, personId = id) })

    verify(profileRepositorySpy).saveAll(profileCaptor.capture())
  }

  @Test
  fun `save invoked twice for 501 profiles`() {
    PersonPersister(
      personRepositorySpy,
      profileRepositorySpy
    ).persistProfiles(entities(501) { id -> profileFactory().copy(profileId = id, personId = id) })

    verify(profileRepositorySpy, times(2)).saveAll(profileCaptor.capture())
  }

  fun <T> entities(numberOf: Int, f: (id: String) -> T): List<T> =
    MutableList(numberOf) { index -> f(index.toString()) }
}
