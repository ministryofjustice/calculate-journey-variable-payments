package uk.gov.justice.digital.hmpps.pecs.jpc.domain.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.domain.price.Supplier
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.GNICourtLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.WYIPrisonLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.defaultMoveTypeStandard
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.defaultSupplierSerco
import java.time.LocalDate
import java.util.UUID

@ActiveProfiles("test")
@DataJpaTest
@Import(TestConfig::class)
internal class MoveQueryRepositoryTest {

  @Autowired
  private lateinit var locationRepository: LocationRepository

  @Autowired
  private lateinit var priceRepository: PriceRepository

  @Autowired
  private lateinit var moveRepository: MoveRepository

  @Autowired
  private lateinit var moveQueryRepository: MoveQueryRepository

  @Autowired
  private lateinit var journeyRepository: JourneyRepository

  @Autowired
  private lateinit var personRepository: PersonRepository

  @Autowired
  private lateinit var profileRepository: ProfileRepository

  @Autowired
  private lateinit var entityManager: TestEntityManager

  private val wyi = WYIPrisonLocation()
  private val gni = GNICourtLocation()

  private val standardMoveSerco = moveM1(
    dropOffOrCancelledDateTime = defaultMoveDate10Sep2020.atStartOfDay().plusHours(5)
  ) // should appear before the one above

  private val journeyModel1Serco = journeyJ1()
  private val journeyModel2Serco = journeyJ1(journeyId = "J2")
  private val sercoMoveWithMissingType = standardMoveSerco.copy(moveId = "MWMT", moveType = null)

  private val standardMoveGeoamey = standardMoveSerco.copy(moveId = "M2", supplier = Supplier.GEOAMEY)
  private val journeyModelGeoamey =
    journeyModel1Serco.copy(journeyId = "J3", supplier = Supplier.GEOAMEY, moveId = standardMoveGeoamey.moveId)

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
        supplier = defaultSupplierSerco,
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

    moveRepository.save(standardMoveSerco)

    journeyRepository.save(journeyModel1Serco)
    journeyRepository.save(journeyModel2Serco)

    moveRepository.save(sercoMoveWithMissingType)

    moveRepository.save(standardMoveGeoamey)
    journeyRepository.save(journeyModelGeoamey)

    entityManager.flush()
  }

  @Test
  fun `move with person and journeys with invalid move id for supplier`() {
    assertThat(moveQueryRepository.moveWithPersonAndJourneys(standardMoveSerco.moveId, Supplier.GEOAMEY)).isNull()
  }

  @Test
  fun `Serco move with person and journeys with missing move_type is null`() {
    assertThat(moveRepository.findById(sercoMoveWithMissingType.moveId)).isPresent
    assertThat(
      moveQueryRepository.moveWithPersonAndJourneys(
        sercoMoveWithMissingType.moveId,
        sercoMoveWithMissingType.supplier
      )
    ).isNull()
  }

  @Test
  fun `Serco move should be priced if all journeys are billable`() {
    val move = moveQueryRepository.movesForMoveTypeInDateRange(
      defaultSupplierSerco,
      defaultMoveTypeStandard,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020
    )[0]
    // Move should be priced
    assertThat(move.hasPrice()).isTrue
  }

  @Test
  fun `Serco move PII data should be present and journey count and pricing is correct`() {
    personRepository.save(personPE1())
    profileRepository.save(profilePR1())

    entityManager.flush()

    val move = moveQueryRepository.moveWithPersonAndJourneys(standardMoveSerco.moveId, defaultSupplierSerco)

    // Move should be priced
    assertThat(move!!.hasPrice()).isTrue

    assertThat(move.person?.firstNames).isEqualTo("Billy the")
    assertThat(move.person?.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
    assertThat(move.journeys.size).isEqualTo(2)
    assertThat(move.journeys.map { j -> j.priceInPence }).containsExactly(999, 999)
  }

  @Test
  fun `GEOAmey move PII data should be present and journey count and pricing is correct`() {
    personRepository.save(personPE1())
    profileRepository.save(profilePR1())

    entityManager.flush()

    val move = moveQueryRepository.moveWithPersonAndJourneys(standardMoveGeoamey.moveId, Supplier.GEOAMEY)

    // Move should be priced
    assertThat(move!!.hasPrice()).isTrue

    assertThat(move.person?.firstNames).isEqualTo("Billy the")
    assertThat(move.person?.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
    assertThat(move.journeys.size).isEqualTo(1)
    assertThat(move.journeys.first().priceInPence).isEqualTo(666)
  }

  @Test
  fun `find all moves for Serco for move type in date range with non billable journey`() {

    val nonBillableJourney = journeyJ1(journeyId = "J3", billable = false)
    val journeyWithoutDropOffDate = journeyJ1(journeyId = "J4", pickUpDateTime = null, dropOffDateTime = null)

    journeyRepository.save(nonBillableJourney)
    journeyRepository.save(journeyWithoutDropOffDate)

    entityManager.flush()

    val move = moveQueryRepository.movesForMoveTypeInDateRange(
      defaultSupplierSerco,
      defaultMoveTypeStandard,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020
    )[0]

    assertThat(move.journeys.size).isEqualTo(4)

    // Journey 1 should have a price because it's billable
    assertThat(move.journeys.find { it.journeyId == "J1" }!!.hasPrice()).isTrue

    // Journey 3 should not have a price because it's not billable
    assertThat(move.journeys.find { it.journeyId == "J3" }!!.hasPrice()).isFalse

    // Move should not be priced if one or more journeys is not priced
    assertThat(move.hasPrice()).isFalse
  }

  @Test
  fun `all summaries for Serco`() {

    val moveWithUnbillableJourney = standardMoveSerco.copy(moveId = "M2")
    val journey3 = journeyJ1(moveId = "M2", journeyId = "J3", billable = false)

    val moveWithUnpricedJourney = standardMoveSerco.copy(moveId = "M3")
    val journey4 = journeyJ1(moveId = "M3", journeyId = "J4", billable = true, fromNomisAgencyId = "UNPRICED")

    val moveWithoutJourneys = standardMoveSerco.copy(moveId = "M4")

    moveRepository.save(moveWithUnbillableJourney)
    moveRepository.save(moveWithUnpricedJourney)
    moveRepository.save(moveWithoutJourneys)

    journeyRepository.save(journey3)
    journeyRepository.save(journey4)

    entityManager.flush()

    // The moves with no journeys, un-billable journey and un-priced journey should come out as un-priced
    val summaries = moveQueryRepository.summariesInDateRange(
      defaultSupplierSerco,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020,
      4
    )
    assertThat(summaries).containsExactly(MovesSummary(defaultMoveTypeStandard, 1.0, 4, 2, 1998))
  }

  @Test
  fun `Serco moves count`() {
    assertThat(
      moveQueryRepository.moveCountInDateRange(
        defaultSupplierSerco,
        defaultMoveDate10Sep2020,
        defaultMoveDate10Sep2020
      )
    ).isEqualTo(1)
  }

  @Test
  fun `GEOAMey moves count`() {
    assertThat(
      moveQueryRepository.moveCountInDateRange(
        Supplier.GEOAMEY,
        defaultMoveDate10Sep2020,
        defaultMoveDate10Sep2020
      )
    ).isEqualTo(1)
  }
}
