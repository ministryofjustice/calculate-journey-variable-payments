package uk.gov.justice.digital.hmpps.pecs.jpc.move

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.pecs.jpc.TestConfig
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.GNICourtLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.WYIPrisonLocation
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.defaultMoveTypeStandard
import uk.gov.justice.digital.hmpps.pecs.jpc.importer.report.defaultSupplierSerco
import uk.gov.justice.digital.hmpps.pecs.jpc.location.LocationRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Price
import uk.gov.justice.digital.hmpps.pecs.jpc.price.PriceRepository
import uk.gov.justice.digital.hmpps.pecs.jpc.price.Supplier
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

  private val standardMove = moveM1(
    dropOffOrCancelledDateTime = defaultMoveDate10Sep2020.atStartOfDay().plusHours(5)
  ) // should appear before the one above
  private val journeyModel1 = journeyJ1()
  private val journeyModel2 = journeyJ1(journeyId = "J2")
  private val moveWithMissingType = standardMove.copy(moveId = "MWMT", moveType = null)

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

    moveRepository.save(standardMove)

    journeyRepository.save(journeyModel1)
    journeyRepository.save(journeyModel2)

    moveRepository.save(moveWithMissingType)

    entityManager.flush()
  }

  @Test
  fun `moveWithPersonAndJourneys with invalid moveId for supplier`() {
    val move = moveQueryRepository.moveWithPersonAndJourneys(standardMove.moveId, Supplier.GEOAMEY)
    assertThat(move).isNull()
  }

  @Test
  fun `moveWithPersonAndJourneys with missing move_type is null`() {
    assertThat(moveRepository.findById(moveWithMissingType.moveId)).isPresent
    assertThat(moveQueryRepository.moveWithPersonAndJourneys(moveWithMissingType.moveId, moveWithMissingType.supplier)).isNull()
  }

  @Test
  fun `move should be priced if all journeys are billable`() {
    val move = moveQueryRepository.movesForMoveTypeInDateRange(
      defaultSupplierSerco,
      defaultMoveTypeStandard,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020
    )[0]
    // Move should be priced
    assertTrue(move.hasPrice())
  }

  @Test
  fun `move PII data should be present`() {
    personRepository.save(personPE1())
    profileRepository.save(profilePR1())

    entityManager.flush()

    val move = moveQueryRepository.moveWithPersonAndJourneys(standardMove.moveId, defaultSupplierSerco)

    // Move should be priced
    assertTrue(move!!.hasPrice())

    assertThat(move.person?.firstNames).isEqualTo("Billy the")
    assertThat(move.person?.dateOfBirth).isEqualTo(LocalDate.of(1980, 12, 25))
  }

  @Test
  fun `findAllForSupplierAndMovePriceTypeInDateRange a with non billable journey`() {

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
    assertTrue(move.journeys.find { it.journeyId == "J1" }!!.hasPrice())

    // Journey 3 should not have a price because it's not billable
    assertFalse(move.journeys.find { it.journeyId == "J3" }!!.hasPrice())

    // Move should not be priced if one or more journeys is not priced
    assertFalse(move.hasPrice())
  }

  @Test
  fun `all summaries`() {

    val moveWithUnbillableJourney = standardMove.copy(moveId = "M2")
    val journey3 = journeyJ1(moveId = "M2", journeyId = "J3", billable = false)

    val moveWithUnpricedJourney = standardMove.copy(moveId = "M3")
    val journey4 = journeyJ1(moveId = "M3", journeyId = "J4", billable = true, fromNomisAgencyId = "UNPRICED")

    val moveWithoutJourneys = standardMove.copy(moveId = "M4")

    moveRepository.save(moveWithUnbillableJourney)
    moveRepository.save(moveWithUnpricedJourney)
    moveRepository.save(moveWithoutJourneys)

    journeyRepository.save(journey3)
    journeyRepository.save(journey4)

    entityManager.flush()

    // The moves with no journeys, unbillable journey and unpriced journey should come out as unpried
    val summaries = moveQueryRepository.summariesInDateRange(
      defaultSupplierSerco,
      defaultMoveDate10Sep2020,
      defaultMoveDate10Sep2020,
      4
    )
    assertThat(summaries).containsExactly(MovesSummary(defaultMoveTypeStandard, 1.0, 4, 2, 1998))
  }

  @Test
  fun `moves count`() {
    val movesCount =
      moveQueryRepository.moveCountInDateRange(defaultSupplierSerco, defaultMoveDate10Sep2020, defaultMoveDate10Sep2020)
    assertThat(movesCount).isEqualTo(1)
  }
}
