package uk.gov.justice.digital.hmpps.pecs.jpc.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
internal class SupplierSupplierPriceUpliftRepositoryTest {

  @Autowired
  lateinit var repository: SupplierPriceUpliftRepository

  @Autowired
  lateinit var entityManager: TestEntityManager

  @Test
  fun `can create price uplift for Serco and retrieve by supplier`() {
    assertThat(repository.findBySupplier(Supplier.SERCO)).isNull()

    val persisted = repository.save(SupplierPriceUplift(supplier = Supplier.SERCO, effectiveYear = 2020, multiplier = 1.0))

    entityManager.flush()

    assertThat(repository.findBySupplier(Supplier.SERCO)).isEqualTo(persisted)
  }

  @Test
  fun `can create price uplift for GEOAmey and retrieve by supplier`() {
    assertThat(repository.findBySupplier(Supplier.GEOAMEY)).isNull()

    val persisted = repository.save(SupplierPriceUplift(supplier = Supplier.GEOAMEY, effectiveYear = 2021, multiplier = 2.0))

    entityManager.flush()

    assertThat(repository.findBySupplier(Supplier.GEOAMEY)).isEqualTo(persisted)
  }
}
