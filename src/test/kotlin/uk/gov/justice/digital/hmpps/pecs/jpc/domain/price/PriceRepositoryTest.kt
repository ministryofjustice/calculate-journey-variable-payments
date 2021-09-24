package uk.gov.justice.digital.hmpps.pecs.jpc.domain.price

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import java.time.Month

@ActiveProfiles("test")
@DataJpaTest
internal class PriceRepositoryTest {

  @Autowired
  lateinit var locationRepo: LocationRepository

  @Autowired
  lateinit var priceRepo: PriceRepository

  private val fromLocation = Location(
    nomisAgencyId = "FROM_AGENCY",
    locationType = LocationType.PR,
    siteName = "FROM AGENCY"
  )

  private val toLocation = Location(
    nomisAgencyId = "TO_AGENCY",
    locationType = LocationType.PR,
    siteName = "TO AGENCY"
  )

  @BeforeEach
  fun prepareLocations() {
    locationRepo.saveAndFlush(fromLocation)
    locationRepo.saveAndFlush(toLocation)
  }

  @Test
  fun `can save and retrieve price`() {
    val price = Price(
      supplier = Supplier.SERCO,
      fromLocation = fromLocation,
      toLocation = toLocation,
      priceInPence = 1000,
      effectiveYear = 2021
    )

    assertThat(priceRepo.findById(priceRepo.saveAndFlush(price).id)).hasValue(price)
  }

  @Test
  fun `can add and retrieve price exceptions for each month of the contractual year`() {
    val priceWithoutException = Price(
      supplier = Supplier.SERCO,
      fromLocation = fromLocation,
      toLocation = toLocation,
      priceInPence = 1000,
      effectiveYear = 2021
    )

    assertThat(priceRepo.saveAndFlush(priceWithoutException).exceptions()).isEmpty()

    val priceWithExceptions = priceWithoutException.copy().apply {
      Month.values().forEach {
        addException(it, Money(it.value))
      }
    }

    assertThat(
      priceRepo.saveAndFlush(priceWithExceptions).exceptions().map { Pair(Month.of(it.month), it.priceInPence) }
    ).containsExactlyInAnyOrder(
      Pair(Month.SEPTEMBER, 9),
      Pair(Month.OCTOBER, 10),
      Pair(Month.NOVEMBER, 11),
      Pair(Month.DECEMBER, 12),
      Pair(Month.JANUARY, 1),
      Pair(Month.FEBRUARY, 2),
      Pair(Month.MARCH, 3),
      Pair(Month.APRIL, 4),
      Pair(Month.MAY, 5),
      Pair(Month.JUNE, 6),
      Pair(Month.JULY, 7),
      Pair(Month.AUGUST, 8)
    )
  }

  @Test
  fun `can remove price exception from price`() {
    val priceWithException = Price(
      supplier = Supplier.SERCO,
      fromLocation = fromLocation,
      toLocation = toLocation,
      priceInPence = 1000,
      effectiveYear = 2021
    ).apply { addException(Month.JANUARY, Money(1)) }

    assertThat(priceRepo.saveAndFlush(priceWithException).exceptions()).isNotEmpty

    val priceWithoutException = priceRepo.findById(priceWithException.id).get().apply { removeException(Month.JANUARY) }

    assertThat(priceRepo.saveAndFlush(priceWithoutException).exceptions()).isEmpty()
  }
}
