package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
internal class PriceAdjustmentRepositoryTest {

  @Autowired
  lateinit var repository: PriceAdjustmentRepository

  @Autowired
  lateinit var entityManager: TestEntityManager

  @Test
  fun `can create price adjustment for Serco and retrieve by supplier`() {
    assertThat(repository.findBySupplier(Supplier.SERCO)).isNull()

    val persisted = repository.save(PriceAdjustment(supplier = Supplier.SERCO))

    entityManager.flush()

    assertThat(repository.findBySupplier(Supplier.SERCO)).isEqualTo(persisted)
  }

  @Test
  fun `can create price adjustment for GEOAmey and retrieve by supplier`() {
    assertThat(repository.findBySupplier(Supplier.GEOAMEY)).isNull()

    val persisted = repository.save(PriceAdjustment(supplier = Supplier.GEOAMEY))

    entityManager.flush()

    assertThat(repository.findBySupplier(Supplier.GEOAMEY)).isEqualTo(persisted)
  }
}
