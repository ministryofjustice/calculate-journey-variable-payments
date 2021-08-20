package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.Location
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationType
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.GNICourtLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.WYIPrisonLocation
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class JourneyQueryRepositoryTest {

  @Autowired
  lateinit var locationRepository: LocationRepository

  @Autowired
  lateinit var priceRepository: PriceRepository

  @Autowired
  lateinit var moveRepository: MoveRepository

  @Autowired
  lateinit var journeyQueryRepository: JourneyQueryRepository

  @Autowired
  lateinit var journeyRepository: JourneyRepository

  @Autowired
  lateinit var entityManager: TestEntityManager

  val wyi = WYIPrisonLocation()
  val gni = GNICourtLocation()

  val standardMove = moveM1(
    dropOffOrCancelledDateTime = defaultMoveDate10Sep2020.atStartOfDay().plusHours(5)
  ) // should appear before the one above
  val journeyModel1 = journeyJ1()
  val journeyModel2 = journeyJ1(journeyId = "J2")

  @BeforeEach
  fun beforeEach() {
    locationRepository.save(wyi)
    locationRepository.save(gni)

    priceRepository.save(
      Price(
        id = UUID.randomUUID(),
        fromLocation = wyi,
        toLocation = gni,
        priceInPence = 999,
        supplier = Supplier.SERCO,
        effectiveYear = 2020
      )
    )

    priceRepository.save(
      Price(
        id = UUID.randomUUID(),
        fromLocation = wyi,
        toLocation = gni,
        priceInPence = 666,
        supplier = Supplier.GEOAMEY,
        effectiveYear = 2020
      )
    )

    moveRepository.save(standardMove)
    journeyRepository.save(journeyModel1)
    journeyRepository.save(journeyModel2)

    entityManager.flush()
  }

  @Test
  fun `unique journeys and journey summaries`() {

    val locationX =
      Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "locationX", siteName = "banana")
    val locationY =
      Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "locationY", siteName = "apple")

    locationRepository.save(locationX)
    locationRepository.save(locationY)

    priceRepository.save(
      Price(
        id = UUID.randomUUID(),
        fromLocation = wyi,
        toLocation = locationX,
        priceInPence = 201,
        supplier = Supplier.SERCO,
        effectiveYear = 2020
      )
    )

    val moveWithNonBillableJourney = standardMove.copy(moveId = "M2")
    val journey3 =
      journeyJ1(moveId = "M2", journeyId = "J3", billable = false, toNomisAgencyId = locationX.nomisAgencyId)

    val moveWithUnmappedLocation = standardMove.copy(moveId = "M3")
    val journey4 =
      journeyJ1(moveId = "M3", journeyId = "J4", billable = true, fromNomisAgencyId = "unmappedNomisAgencyId")

    val moveWithUpricedJourney = standardMove.copy(moveId = "M4")
    val journey5 =
      journeyJ1(moveId = "M4", journeyId = "J5", billable = true, fromNomisAgencyId = locationY.nomisAgencyId)

    moveRepository.save(moveWithNonBillableJourney) // not unpriced just because journey is not billable
    moveRepository.save(moveWithUpricedJourney) // unpriced in 2020, but priced in 2021
    moveRepository.save(moveWithUnmappedLocation) // unpriced since it has no mapped location

    journeyRepository.save(journey3)
    journeyRepository.save(journey4)
    journeyRepository.save(journey5)

    entityManager.flush()

    val summaries = journeyQueryRepository.journeysSummaryInDateRange(
      Supplier.SERCO,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020
    )
    assertThat(summaries).isEqualTo(JourneysSummary(4, 1998, 1, 2, Supplier.SERCO))

    val unpricedUniqueJourneys = journeyQueryRepository.distinctJourneysAndPriceInDateRange(
      Supplier.SERCO,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020
    )
    assertThat(unpricedUniqueJourneys.size).isEqualTo(2)

    // Ordered by unmapped from locations first
    assertThat(unpricedUniqueJourneys[0].fromNomisAgencyId).isEqualTo("unmappedNomisAgencyId")
  }

  @Test
  fun `2021 prices don't appear in 2020 journeys`() {

    val locationY =
      Location(id = UUID.randomUUID(), locationType = LocationType.CO, nomisAgencyId = "locationY", siteName = "apple")
    locationRepository.save(locationY)
    priceRepository.save(
      Price(
        id = UUID.randomUUID(),
        fromLocation = locationY,
        toLocation = gni,
        priceInPence = 999,
        supplier = Supplier.SERCO,
        effectiveYear = 2021
      )
    )

    val moveWithUpriced2020Journey = standardMove.copy(moveId = "M2")
    val journeyNotPricedFor2020 =
      journeyJ1(moveId = "M2", journeyId = "J2", billable = true, fromNomisAgencyId = locationY.nomisAgencyId)

    moveRepository.save(moveWithUpriced2020Journey)
    journeyRepository.save(journeyNotPricedFor2020)

    entityManager.flush()

    val summaries = journeyQueryRepository.journeysSummaryInDateRange(
      Supplier.SERCO,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020
    )
    assertThat(summaries).isEqualTo(JourneysSummary(2, 999, 0, 1, Supplier.SERCO))

    val unpricedUniqueJourneys = journeyQueryRepository.distinctJourneysAndPriceInDateRange(
      Supplier.SERCO,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020
    )
    assertThat(unpricedUniqueJourneys.size).isEqualTo(1)
    assertThat(unpricedUniqueJourneys[0].totalPriceInPence).isEqualTo(0)
  }

  @Test
  fun `journey prices filtered on from location with empty to location`() {
    val prices = journeyQueryRepository.prices(Supplier.SERCO, "from", "", 2020)
    assertThat(prices).containsExactlyInAnyOrder(
      JourneyWithPrice(
        fromNomisAgencyId = "WYI",
        LocationType.PR,
        fromSiteName = "from",
        toNomisAgencyId = "GNI",
        LocationType.CO,
        toSiteName = "to",
        volume = null,
        unitPriceInPence = 999,
        totalPriceInPence = null
      ),
    )
  }

  @Test
  fun `distinct journeys filtered on to location with whitespace from location`() {
    val prices = journeyQueryRepository.prices(Supplier.SERCO, " ", "to", 2020)
    assertThat(prices).containsExactlyInAnyOrder(
      JourneyWithPrice(
        fromNomisAgencyId = "WYI",
        LocationType.PR,
        fromSiteName = "from",
        toNomisAgencyId = "GNI",
        LocationType.CO,
        toSiteName = "to",
        volume = null,
        unitPriceInPence = 999,
        totalPriceInPence = null
      ),
    )
  }
}
