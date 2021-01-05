package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.reportPersonFactory
import java.time.LocalDate

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class PersonPersisterTest {

  @Autowired
  lateinit var personRepository: PersonRepository

  @Autowired
  lateinit var profileRepository: ProfileRepository

  @Autowired
  lateinit var entityManager: TestEntityManager

  lateinit var personPersister: PersonPersister

  @Test
  fun `Persist PII data`() {
    personPersister = PersonPersister(personRepository, profileRepository)
    val reportPerson = reportPersonFactory()
    personPersister.persistPeople(listOf(reportPerson))

    entityManager.flush()
    val retrievedPerson = personRepository.findById(reportPerson.personId).get()

    // PII data should be populated
    assertThat(retrievedPerson.ethnicity).isEqualTo("White American")
    assertThat(retrievedPerson.gender).isEqualTo("male")
    assertThat(retrievedPerson.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
    assertThat(retrievedPerson.firstNames).isEqualTo("Billy the")
    assertThat(retrievedPerson.lastName).isEqualTo("Kid")
  }
}
