package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
internal class PriceAdjustmentRepositoryTest {

  @Autowired
  lateinit var repository: PriceAdjustmentRepository

  @Test
  fun `can create price adjustment for Serco and retrieve by supplier`() {
    assertThat(repository.findBySupplier(Supplier.SERCO)).isNull()

    val persisted = repository.saveAndFlush(PriceAdjustment(supplier = Supplier.SERCO, multiplier = 1.5, effectiveYear = 2020))

    assertThat(repository.findBySupplier(Supplier.SERCO)).isEqualTo(persisted)
  }

  @Test
  fun `price adjustment is in progress for Serco and exists by supplier`() {
    assertThat(repository.existsPriceAdjustmentBySupplier(Supplier.SERCO)).isFalse

    repository.saveAndFlush(PriceAdjustment(supplier = Supplier.SERCO, multiplier = 1.2, effectiveYear = 2021))

    assertThat(repository.existsPriceAdjustmentBySupplier(Supplier.SERCO)).isTrue
  }

  @Test
  fun `can create price adjustment for GEOAmey and retrieve by supplier`() {
    assertThat(repository.findBySupplier(Supplier.GEOAMEY)).isNull()

    val persisted = repository.saveAndFlush(PriceAdjustment(supplier = Supplier.GEOAMEY, multiplier = 2.0, effectiveYear = 2022))

    assertThat(repository.findBySupplier(Supplier.GEOAMEY)).isEqualTo(persisted)
  }

  @Test
  fun `can only have one price adjustment for a supplier at any given point in time`() {
    assertThat(repository.findBySupplier(Supplier.SERCO)).isNull()

    val priceAdjustment = repository.saveAndFlush(PriceAdjustment(supplier = Supplier.SERCO, multiplier = 1.5, effectiveYear = 2020))

    assertThatThrownBy { repository.saveAndFlush(priceAdjustment.copy(id = UUID.randomUUID())) }
      .isInstanceOf(DataIntegrityViolationException::class.java)
  }
}
