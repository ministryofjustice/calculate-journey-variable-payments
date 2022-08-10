package uk.gov.justice.digital.hmpps.pecs.jpc.service.pricing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.auditing.AuditEventType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Money
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier

/**
 * This test imports the Serco prices spreadsheet from the test resources folder which in turn is referenced in the TestConfig class.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@ContextConfiguration(classes = [TestConfig::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ImportPricesServiceIntegrationTest(
  @Autowired private val locationRepository: LocationRepository,
  @Autowired private val priceRepository: PriceRepository,
  @Autowired private val auditEventRepository: AuditEventRepository,
  @Autowired private val service: ImportPricesService,
) {

  private val prison = Location(
    LocationType.PR,
    "PRISON",
    "HMP FRED"
  )

  private val court = Location(
    LocationType.CC,
    "COURT",
    "FREDS COUNTY COURT"
  )

  @BeforeAll
  fun `set up locations needed for journey price import`() {
    locationRepository.saveAll(mutableListOf(prison, court))
  }

  @AfterEach
  fun `clean up`() {
    priceRepository.deleteAll()
    auditEventRepository.deleteAll()
  }

  @Test
  fun `Serco court to prison journey price imported and audit trail is created`() {
    assertJourneyPriceImported(Supplier.SERCO, 2021, Expected("COURT", "PRISON", Money(600)))
  }

  @Test
  fun `GEOAmey prison to court journey price imported and audit trail is created`() {
    assertJourneyPriceImported(Supplier.GEOAMEY, 2020, Expected("PRISON", "COURT", Money(4300)))
  }

  private fun assertJourneyPriceImported(supplier: Supplier, year: Int, expected: Expected) {
    assertThat(priceRepository.findAll()).isEmpty()
    assertThat(auditEventRepository.findAll()).isEmpty()

    service.importPricesFor(supplier, year)

    val journeyPrices = priceRepository.findAll()

    assertThat(journeyPrices).hasSize(1)

    with(journeyPrices.first()) {
      assertThat(supplier).isEqualTo(supplier)
      assertThat(priceInPence).isEqualTo(expected.price.pence)
      assertThat(effectiveYear).isEqualTo(year)
      assertThat(fromLocation.nomisAgencyId).isEqualTo(expected.fromAgency)
      assertThat(toLocation.nomisAgencyId).isEqualTo(expected.toAgency)
    }

    val auditEvents = auditEventRepository.findAll()

    assertThat(auditEvents).hasSize(1)

    with(auditEvents.first()) {
      assertThat(eventType).isEqualTo(AuditEventType.JOURNEY_PRICE)
      assertThat(metadataKey).isEqualTo("$supplier-${expected.fromAgency}-${expected.toAgency}")
      assertThat(metadata).isEqualTo("""{"supplier" : "$supplier", "from_nomis_id" : "${expected.fromAgency}", "to_nomis_id" : "${expected.toAgency}", "effective_year" : $year, "new_price" : "${expected.price}", "exception_month" : null, "exception_deleted" : null}""")
    }
  }

  private data class Expected(val fromAgency: String, val toAgency: String, val price: Money)
}
