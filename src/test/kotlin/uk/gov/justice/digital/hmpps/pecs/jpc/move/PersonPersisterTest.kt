package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.config.TimeSource
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.*
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
import java.time.LocalDate
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class PersonPersisterTest {

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var timeSource: TimeSource

    @Autowired
    lateinit var entityManager: TestEntityManager

    lateinit var personPersister: PersonPersister

    @Test
    fun `Persist PII data`() {
        personPersister = PersonPersister(personRepository, timeSource)
        val reportPerson = reportPersonFactory().copy(profileId = defaultProfileId)
        personPersister.persist(listOf(reportPerson))

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

