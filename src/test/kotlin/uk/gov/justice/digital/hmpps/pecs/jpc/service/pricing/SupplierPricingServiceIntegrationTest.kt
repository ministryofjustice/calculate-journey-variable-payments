package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SupplierPricingServiceIntegrationTest(
  @Autowired private val locationRepository: LocationRepository,
  @Autowired private val service: SupplierPricingService
) {

  @BeforeAll
  internal fun beforeAll() {
    locationRepository.save(
      Location(
        LocationType.PR,
        "PRISON1",
        "PRISON ONE"
      )
    )

    locationRepository.save(
      Location(
        LocationType.PR,
        "PRISON2",
        "PRISON TWO"
      )
    )
  }

  @Test
  @WithMockUser(roles = ["PECS_MAINTAIN_PRICE"])
  fun `can maintain price with maintenance role`() {
    assertDoesNotThrow {
      service.addPriceForSupplier(Supplier.SERCO, "PRISON1", "PRISON2", Money.valueOf("10.00"), 2021)
      service.updatePriceForSupplier(Supplier.SERCO, "PRISON1", "PRISON2", Money.valueOf("11.00"), 2021)
    }
  }

  @Test
  @WithMockUser(roles = ["PECS_JPC"])
  fun `cannot maintain price without maintenance role`() {
    assertThatThrownBy {
      service.addPriceForSupplier(Supplier.SERCO, "PRISON1", "PRISON2", Money.valueOf("10.00"), 2021)
    }.isInstanceOf(AccessDeniedException::class.java)

    assertThatThrownBy {
      service.updatePriceForSupplier(Supplier.SERCO, "PRISON1", "PRISON2", Money.valueOf("10.00"), 2021)
    }.isInstanceOf(AccessDeniedException::class.java)
  }
}
