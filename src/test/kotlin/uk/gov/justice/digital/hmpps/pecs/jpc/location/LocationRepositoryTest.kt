package uk.gov.justice.digital.hmpps.pecs.jpc.location

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
class LocationRepositoryTest {

    @Autowired
    lateinit var repository: LocationRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Test
    fun `can save location`() {
        val location = repository.save(Location(LocationType.PR, "agency id", "site name"))

        entityManager.flush()

        assertThat(repository.findById(location.id).orElseThrow()).isEqualTo(location)
    }

    @Test
    fun `should throw constraint violation if fields empty`() {
        assertThatThrownBy {
            repository.save(Location(LocationType.PR, "", ""))
            entityManager.flush()
        }.isInstanceOf(ConstraintViolationException::class.java)
    }
}
