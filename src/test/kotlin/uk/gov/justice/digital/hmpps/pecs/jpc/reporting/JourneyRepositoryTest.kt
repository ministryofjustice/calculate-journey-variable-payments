package uk.gov.justice.digital.hmpps.pecs.jpc.reporting

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import javax.validation.ConstraintViolationException

@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
    @DataJpaTest
    class JourneyRepositoryTest {

    @Autowired
    lateinit var repository: JourneyRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can save Journey`() {
        val cannedJourney = cannedJourney()
        val journey = repository.save(cannedJourney)

        entityManager.flush()

        assertThat(repository.findById(cannedJourney.id).orElseThrow()).isEqualTo(cannedJourney)
    }

    @Test
    fun `should throw constraint violation if supplier is empty`() {
        val journeyToSave = cannedJourney().copy(supplier = "")
        assertThatThrownBy {
            repository.save(journeyToSave)
            entityManager.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }
}
