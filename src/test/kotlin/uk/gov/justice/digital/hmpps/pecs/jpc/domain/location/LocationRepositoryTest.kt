package uk.gov.justice.digital.hmpps.pecs.jpc.domain.location

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import javax.validation.ConstraintViolationException

@ActiveProfiles("test")
@DataJpaTest
internal class LocationRepositoryTest {

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

  @Test
  fun `can find location by agency id or site name`() {
    val location = repository.save(Location(LocationType.PR, "agency id", "site name"))

    entityManager.flush()

    assertThat(repository.findByNomisAgencyIdOrSiteName("agency id", "different site name")).containsExactly(location)
    assertThat(repository.findByNomisAgencyIdOrSiteName("different agency id", "site name")).containsExactly(location)
  }

  @Test
  fun `can find multiple location by agency id or site name`() {
    val location1 = repository.save(Location(LocationType.PR, "agency id", "site name"))
    val location2 = repository.save(Location(LocationType.PR, "other agency id", "other site name"))

    entityManager.flush()

    assertThat(repository.findByNomisAgencyIdOrSiteName("agency id", "other site name")).containsExactly(location1, location2)
  }
}
